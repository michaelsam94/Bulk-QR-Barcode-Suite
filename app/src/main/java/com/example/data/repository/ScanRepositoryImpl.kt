package com.example.data.repository

import com.example.data.local.dao.ScannedCodeDao
import com.example.data.local.dao.ScanSessionDao
import com.example.data.local.entity.ScanSessionEntity
import com.example.data.mapper.toDomain
import com.example.data.mapper.toEntity
import com.example.domain.entity.ScannedCode
import com.example.domain.entity.ScanSession
import com.example.domain.repository.ScanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class ScanRepositoryImpl(
    private val scannedCodeDao: ScannedCodeDao,
    private val scanSessionDao: ScanSessionDao
) : ScanRepository {

    override fun getSessionCodes(sessionId: String): Flow<List<ScannedCode>> {
        return scannedCodeDao.getBySession(sessionId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllSessions(): Flow<List<ScanSession>> {
        return scanSessionDao.getAllSessionsWithCounts().map { sessionWithCounts ->
            sessionWithCounts.map { it.toDomain() }
        }
    }

    override suspend fun saveCode(code: ScannedCode): Long {
        return scannedCodeDao.insert(code.toEntity())
    }

    override suspend fun deleteCode(codeId: Long) {
        scannedCodeDao.deleteById(codeId)
    }

    override suspend fun deleteSession(sessionId: String) {
        scannedCodeDao.deleteBySession(sessionId)
        scanSessionDao.deleteById(sessionId)
    }

    override suspend fun isDuplicate(sessionId: String, rawValue: String): Boolean {
        return scannedCodeDao.countDuplicates(sessionId, rawValue) > 0
    }

    override suspend fun createNewSession(name: String): String {
        val id = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val sessionName = name.ifBlank { "Session " + java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(timestamp)) }
        scanSessionDao.insert(ScanSessionEntity(id, sessionName, timestamp))
        return id
    }
}
