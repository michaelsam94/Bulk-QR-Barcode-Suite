package com.example.presentation.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.entity.ExportFormat
import com.example.domain.repository.ScanRepository
import com.example.domain.usecase.DeleteSessionUseCase
import com.example.domain.usecase.ExportSessionUseCase
import com.example.domain.usecase.GetAllSessionsUseCase
import com.example.domain.usecase.GetSessionCodesUseCase
import com.example.presentation.scan.ExportState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SessionsViewModel(
    private val getAllSessionsUseCase: GetAllSessionsUseCase,
    private val getSessionCodesUseCase: GetSessionCodesUseCase,
    private val deleteSessionUseCase: DeleteSessionUseCase,
    private val exportSessionUseCase: ExportSessionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SessionsUiState())
    val state: StateFlow<SessionsUiState> = _state.asStateFlow()

    private var activeCodesJob: Job? = null

    init {
        // Collect sessions reactively as they are altered from the scan interface
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getAllSessionsUseCase().collect { sessionList ->
                _state.update { it.copy(sessions = sessionList, isLoading = false) }
            }
        }
    }

    fun onEvent(event: SessionsUiEvent) {
        when (event) {
            is SessionsUiEvent.ToggleSessionSelection -> {
                toggleSessionSelection(event.sessionId)
            }
            is SessionsUiEvent.DeleteSession -> {
                viewModelScope.launch {
                    if (_state.value.selectedSessionId == event.sessionId) {
                        activeCodesJob?.cancel()
                        _state.update { it.copy(selectedSessionId = null, selectedSessionCodes = emptyList()) }
                    }
                    deleteSessionUseCase(event.sessionId)
                }
            }
            is SessionsUiEvent.ExportSession -> {
                viewModelScope.launch {
                    _state.update { it.copy(exportState = ExportState.InProgress) }
                    exportSessionUseCase(event.sessionId, event.format, event.destinationUri)
                        .onSuccess {
                            _state.update { it.copy(exportState = ExportState.Success("Export fully completed!")) }
                        }
                        .onFailure { error ->
                            _state.update { it.copy(exportState = ExportState.Error(error.localizedMessage ?: "Failed to export.")) }
                        }
                }
            }
            is SessionsUiEvent.ClearExportState -> {
                _state.update { it.copy(exportState = ExportState.Idle) }
            }
        }
    }

    private fun toggleSessionSelection(sessionId: String) {
        activeCodesJob?.cancel()
        if (_state.value.selectedSessionId == sessionId) {
            _state.update { it.copy(selectedSessionId = null, selectedSessionCodes = emptyList()) }
        } else {
            _state.update { it.copy(selectedSessionId = sessionId) }
            activeCodesJob = viewModelScope.launch {
                getSessionCodesUseCase(sessionId).collect { codes ->
                    _state.update { it.copy(selectedSessionCodes = codes) }
                }
            }
        }
    }

    companion object {
        fun provideFactory(
            getAllSessionsUseCase: GetAllSessionsUseCase,
            getSessionCodesUseCase: GetSessionCodesUseCase,
            deleteSessionUseCase: DeleteSessionUseCase,
            exportSessionUseCase: ExportSessionUseCase
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SessionsViewModel(
                    getAllSessionsUseCase,
                    getSessionCodesUseCase,
                    deleteSessionUseCase,
                    exportSessionUseCase
                ) as T
            }
        }
    }
}
