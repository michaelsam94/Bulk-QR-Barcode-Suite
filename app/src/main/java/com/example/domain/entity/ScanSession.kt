package com.example.domain.entity

data class ScanSession(
    val id: String,
    val name: String,
    val timestampMs: Long,
    val codeCount: Int
)
