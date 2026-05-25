package com.example.presentation.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.entity.BarcodeFormat
import com.example.domain.entity.ExportFormat
import com.example.domain.entity.ScannedCode
import com.example.domain.repository.ScanRepository
import com.example.domain.usecase.ExportSessionUseCase
import com.example.domain.usecase.GetSessionCodesUseCase
import com.example.domain.usecase.SaveScannedCodeUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScanViewModel(
    private val saveScannedCodeUseCase: SaveScannedCodeUseCase,
    private val getSessionCodesUseCase: GetSessionCodesUseCase,
    private val exportSessionUseCase: ExportSessionUseCase,
    private val scanRepository: ScanRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ScanUiState())
    val state: StateFlow<ScanUiState> = _state.asStateFlow()

    private var codesJob: Job? = null

    init {
        // Automatically provision a live batch session on launch
        startNewSession()
    }

    fun onEvent(event: ScanUiEvent) {
        when (event) {
            is ScanUiEvent.StartNewSession -> startNewSession()
            is ScanUiEvent.ToggleScanState -> {
                _state.update { it.copy(isScanning = !it.isScanning) }
            }
            is ScanUiEvent.BarcodeDetected -> {
                if (_state.value.isScanning) {
                    processDetectedBarcode(event.rawValue, event.format)
                }
            }
            is ScanUiEvent.DeleteCode -> {
                viewModelScope.launch {
                    scanRepository.deleteCode(event.codeId)
                }
            }
            is ScanUiEvent.ExportSession -> {
                viewModelScope.launch {
                    val sessionId = _state.value.currentSessionId ?: return@launch
                    _state.update { it.copy(exportState = ExportState.InProgress) }
                    exportSessionUseCase(sessionId, event.format, event.destinationUri)
                        .onSuccess {
                            _state.update { it.copy(exportState = ExportState.Success("Session successfuly exported!")) }
                        }
                        .onFailure { error ->
                            _state.update { it.copy(exportState = ExportState.Error(error.localizedMessage ?: "Failed to export.")) }
                        }
                }
            }
            is ScanUiEvent.ClearExportState -> {
                _state.update { it.copy(exportState = ExportState.Idle) }
            }
        }
    }

    private fun startNewSession() {
        viewModelScope.launch {
            val nextId = scanRepository.createNewSession("")
            _state.update {
                it.copy(
                    currentSessionId = nextId,
                    lastScannedValue = null,
                    lastScannedFormat = null,
                    isSuccessFlash = false,
                    isDuplicateFlash = false,
                    exportState = ExportState.Idle
                )
            }
            observeSessionCodes(nextId)
        }
    }

    private fun observeSessionCodes(sessionId: String) {
        codesJob?.cancel()
        codesJob = viewModelScope.launch {
            getSessionCodesUseCase(sessionId).collect { codes ->
                _state.update {
                    it.copy(
                        scannedCodes = codes,
                        scanCount = codes.size
                    )
                }
            }
        }
    }

    // Lock to prevent multi-threading frame overlap inserts
    private var isInserting = false

    private fun processDetectedBarcode(rawValue: String, format: BarcodeFormat) {
        val sessionId = _state.value.currentSessionId ?: return
        if (isInserting) return
        
        // De-duplicate in working cache list to prevent microsecond bursts
        val matchesRecent = _state.value.scannedCodes.any { it.rawValue == rawValue }
        
        viewModelScope.launch {
            isInserting = true
            if (matchesRecent || scanRepository.isDuplicate(sessionId, rawValue)) {
                _state.update { it.copy(isDuplicateFlash = true) }
                delay(700)
                _state.update { it.copy(isDuplicateFlash = false) }
            } else {
                val code = ScannedCode(
                    rawValue = rawValue,
                    format = format,
                    timestampMs = System.currentTimeMillis(),
                    sessionId = sessionId
                )
                val insertedId = saveScannedCodeUseCase(code)
                if (insertedId > 0) {
                    _state.update {
                        it.copy(
                            lastScannedValue = rawValue,
                            lastScannedFormat = format.name,
                            isSuccessFlash = true
                        )
                    }
                    delay(500)
                    _state.update { it.copy(isSuccessFlash = false) }
                }
            }
            isInserting = false
        }
    }

    companion object {
        fun provideFactory(
            saveScannedCodeUseCase: SaveScannedCodeUseCase,
            getSessionCodesUseCase: GetSessionCodesUseCase,
            exportSessionUseCase: ExportSessionUseCase,
            scanRepository: ScanRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ScanViewModel(
                    saveScannedCodeUseCase,
                    getSessionCodesUseCase,
                    exportSessionUseCase,
                    scanRepository
                ) as T
            }
        }
    }
}
