package com.example.presentation.sessions

import com.example.domain.entity.ScanSession
import com.example.domain.entity.ScannedCode
import com.example.presentation.scan.ExportState

data class SessionsUiState(
    val sessions: List<ScanSession> = emptyList(),
    val isLoading: Boolean = false,
    val selectedSessionId: String? = null,
    val selectedSessionCodes: List<ScannedCode> = emptyList(),
    val exportState: ExportState = ExportState.Idle
)
