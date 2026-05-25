package com.example.domain.usecase

import com.example.domain.entity.ScanSession
import com.example.domain.repository.ScanRepository
import kotlinx.coroutines.flow.Flow

class GetAllSessionsUseCase(private val repository: ScanRepository) {
    operator fun invoke(): Flow<List<ScanSession>> {
        return repository.getAllSessions()
    }
}
