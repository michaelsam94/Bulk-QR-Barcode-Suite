package com.example.presentation.generate

import com.example.domain.entity.ErrorCorrectionLevel

data class GenerateUiState(
    val content: String = "",
    val foregroundColor: Int = 0xFF00E5FF.toInt(), // Accent vibrant cyan
    val backgroundColor: Int = 0xFFFFFFFF.toInt(), // Default white background for high contrast
    val logoUri: String? = null,
    val errorCorrection: ErrorCorrectionLevel = ErrorCorrectionLevel.H, // Standard high error correction for logo tolerance
    val sizePx: Int = 512,
    val generatedQrUri: String? = null,
    val isGenerating: Boolean = false,
    val error: String? = null
)
