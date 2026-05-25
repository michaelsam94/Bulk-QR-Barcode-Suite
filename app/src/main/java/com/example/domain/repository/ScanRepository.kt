package com.example.domain.repository

import com.example.domain.entity.ScannedCode
import com.example.domain.entity.ScanSession
import kotlinx.coroutines.flow.Flow

interface ScanRepository {
    fun getSessionCodes(sessionId: String): Flow<List<ScannedCode>>
    fun getAllSessions(): Flow<List<ScanSession>>
    suspend fun saveCode(code: ScannedCode): Long
    suspend fun deleteCode(codeId: Long)
    suspend fun deleteSession(sessionId: String)
    suspend fun isDuplicate(sessionId: String, rawValue: String): Boolean
    suspend fun createNewSession(name: String): String
}
