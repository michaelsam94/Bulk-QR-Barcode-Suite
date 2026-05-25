package com.example.domain.entity

data class ScannedCode(
    val id: Long = 0,
    val rawValue: String,
    val format: BarcodeFormat,
    val timestampMs: Long,
    val sessionId: String
)
