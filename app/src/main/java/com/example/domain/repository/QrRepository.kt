package com.example.domain.repository

import com.example.domain.entity.QrGenerationConfig

interface QrRepository {
    suspend fun generateQrCode(config: QrGenerationConfig): Result<String> // returns cached file Uri path
}
