package com.example.domain.usecase

import com.example.domain.repository.ScanRepository

class DeleteSessionUseCase(private val repository: ScanRepository) {
    suspend operator fun invoke(sessionId: String) {
        repository.deleteSession(sessionId)
    }
}
