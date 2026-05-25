package com.example.domain.entity

data class QrGenerationConfig(
    val content: String,
    val sizePx: Int,
    val foregroundColor: Int, // ARGB Int color
    val backgroundColor: Int, // ARGB Int color
    val logoUri: String? = null, // String representation of URI for domain purity
    val errorCorrectionLevel: ErrorCorrectionLevel
)
