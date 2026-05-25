package com.example.presentation.scan

import com.example.domain.entity.ScannedCode

data class ScanUiState(
    val isScanning: Boolean = true,
    val currentSessionId: String? = null,
    val currentSessionName: String = "",
    val scannedCodes: List<ScannedCode> = emptyList(),
    val scanCount: Int = 0,
    val lastScannedValue: String? = null,
    val lastScannedFormat: String? = null,
    val isDuplicateFlash: Boolean = false,
    val isSuccessFlash: Boolean = false,
    val exportState: ExportState = ExportState.Idle
)

sealed interface ExportState {
    object Idle : ExportState
    object InProgress : ExportState
    data class Success(val message: String) : ExportState
    data class Error(val error: String) : ExportState
}
