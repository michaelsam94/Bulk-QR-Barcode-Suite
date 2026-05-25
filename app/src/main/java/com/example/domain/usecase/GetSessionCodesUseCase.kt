package com.example.domain.usecase

import com.example.domain.entity.ScannedCode
import com.example.domain.repository.ScanRepository
import kotlinx.coroutines.flow.Flow

class GetSessionCodesUseCase(private val repository: ScanRepository) {
    operator fun invoke(sessionId: String): Flow<List<ScannedCode>> {
        return repository.getSessionCodes(sessionId)
    }
}
