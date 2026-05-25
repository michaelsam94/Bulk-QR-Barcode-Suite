package com.example.presentation.generate

import com.example.domain.entity.ErrorCorrectionLevel

sealed interface GenerateUiEvent {
    data class ContentChanged(val text: String) : GenerateUiEvent
    data class FgColorChanged(val color: Int) : GenerateUiEvent
    data class BgColorChanged(val color: Int) : GenerateUiEvent
    data class LogoPicked(val uriStr: String) : GenerateUiEvent
    object RemoveLogo : GenerateUiEvent
    data class ErrorCorrectionChanged(val level: ErrorCorrectionLevel) : GenerateUiEvent
    object Generate : GenerateUiEvent
    object ClearState : GenerateUiEvent
}
