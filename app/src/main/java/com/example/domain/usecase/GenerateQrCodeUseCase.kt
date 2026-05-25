package com.example.domain.usecase

import com.example.domain.entity.QrGenerationConfig
import com.example.domain.repository.QrRepository

class GenerateQrCodeUseCase(private val repository: QrRepository) {
    suspend operator fun invoke(config: QrGenerationConfig): Result<String> {
        return repository.generateQrCode(config)
    }
}
