package com.example.presentation.generate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.entity.QrGenerationConfig
import com.example.domain.usecase.GenerateQrCodeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GenerateViewModel(
    private val generateQrCodeUseCase: GenerateQrCodeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(GenerateUiState())
    val state: StateFlow<GenerateUiState> = _state.asStateFlow()

    fun onEvent(event: GenerateUiEvent) {
        when (event) {
            is GenerateUiEvent.ContentChanged -> {
                _state.update { it.copy(content = event.text) }
            }
            is GenerateUiEvent.FgColorChanged -> {
                _state.update { it.copy(foregroundColor = event.color) }
            }
            is GenerateUiEvent.BgColorChanged -> {
                _state.update { it.copy(backgroundColor = event.color) }
            }
            is GenerateUiEvent.LogoPicked -> {
                _state.update { it.copy(logoUri = event.uriStr) }
            }
            is GenerateUiEvent.RemoveLogo -> {
                _state.update { it.copy(logoUri = null) }
            }
            is GenerateUiEvent.ErrorCorrectionChanged -> {
                _state.update { it.copy(errorCorrection = event.level) }
            }
            is GenerateUiEvent.Generate -> {
                generateQrCode()
            }
            is GenerateUiEvent.ClearState -> {
                _state.update {
                    it.copy(
                        generatedQrUri = null,
                        error = null
                    )
                }
            }
        }
    }

    private fun generateQrCode() {
        val currentContent = _state.value.content
        if (currentContent.isBlank()) {
            _state.update { it.copy(error = "Content field cannot be empty.") }
            return
        }

        _state.update { it.copy(isGenerating = true, error = null) }
        viewModelScope.launch {
            val config = QrGenerationConfig(
                content = currentContent,
                sizePx = _state.value.sizePx,
                foregroundColor = _state.value.foregroundColor,
                backgroundColor = _state.value.backgroundColor,
                logoUri = _state.value.logoUri,
                errorCorrectionLevel = _state.value.errorCorrection
            )

            generateQrCodeUseCase(config)
                .onSuccess { cachedFilePath ->
                    _state.update {
                        it.copy(
                            generatedQrUri = cachedFilePath,
                            isGenerating = false
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            error = exception.localizedMessage ?: "Failed to generate QR Code.",
                            isGenerating = false
                        )
                    }
                }
        }
    }

    companion object {
        fun provideFactory(
            generateQrCodeUseCase: GenerateQrCodeUseCase
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return GenerateViewModel(generateQrCodeUseCase) as T
            }
        }
    }
}
