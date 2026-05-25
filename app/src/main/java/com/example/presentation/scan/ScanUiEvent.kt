package com.example.presentation.scan

import com.example.domain.entity.BarcodeFormat
import com.example.domain.entity.ExportFormat

sealed interface ScanUiEvent {
    object StartNewSession : ScanUiEvent
    object ToggleScanState : ScanUiEvent
    data class BarcodeDetected(val rawValue: String, val format: BarcodeFormat) : ScanUiEvent
    data class DeleteCode(val codeId: Long) : ScanUiEvent
    data class ExportSession(val format: ExportFormat, val destinationUri: String) : ScanUiEvent
    object ClearExportState : ScanUiEvent
}
