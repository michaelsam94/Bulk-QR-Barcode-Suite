package com.example.domain.usecase

import com.example.domain.entity.ScannedCode
import com.example.domain.repository.ScanRepository

class SaveScannedCodeUseCase(private val repository: ScanRepository) {
    suspend operator fun invoke(code: ScannedCode): Long {
        return repository.saveCode(code)
    }
}
