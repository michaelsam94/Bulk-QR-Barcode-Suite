package com.example.presentation.sessions

import com.example.domain.entity.ExportFormat

sealed interface SessionsUiEvent {
    data class ToggleSessionSelection(val sessionId: String) : SessionsUiEvent
    data class DeleteSession(val sessionId: String) : SessionsUiEvent
    data class ExportSession(val sessionId: String, val format: ExportFormat, val destinationUri: String) : SessionsUiEvent
    object ClearExportState : SessionsUiEvent
}
