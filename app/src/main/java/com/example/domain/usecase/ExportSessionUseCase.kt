package com.example.domain.usecase

import com.example.domain.entity.ExportFormat
import com.example.domain.repository.ExportRepository
import com.example.domain.repository.ScanRepository
import kotlinx.coroutines.flow.first

class ExportSessionUseCase(
    private val scanRepository: ScanRepository,
    private val exportRepository: ExportRepository
) {
    suspend operator fun invoke(
        sessionId: String,
        format: ExportFormat,
        destinationUri: String
    ): Result<Unit> {
        val codes = scanRepository.getSessionCodes(sessionId).first()
        if (codes.isEmpty()) {
            return Result.failure(Exception("No codes recorded in this session to export."))
        }
        return exportRepository.exportSession(codes, format, destinationUri)
    }
}
