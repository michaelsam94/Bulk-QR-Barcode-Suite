package com.example.domain.repository

import com.example.domain.entity.ExportFormat
import com.example.domain.entity.ScannedCode

interface ExportRepository {
    suspend fun exportSession(
        codes: List<ScannedCode>,
        format: ExportFormat,
        destinationUri: String
    ): Result<Unit>
}
