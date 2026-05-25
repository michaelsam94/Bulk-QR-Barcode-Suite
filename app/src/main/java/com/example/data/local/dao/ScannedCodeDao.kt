package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.ScannedCodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScannedCodeDao {
    @Query("SELECT * FROM scanned_codes WHERE session_id = :sessionId ORDER BY timestamp_ms DESC")
    fun getBySession(sessionId: String): Flow<List<ScannedCodeEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(code: ScannedCodeEntity): Long

    @Query("DELETE FROM scanned_codes WHERE id = :codeId")
    suspend fun deleteById(codeId: Long)

    @Query("DELETE FROM scanned_codes WHERE session_id = :sessionId")
    suspend fun deleteBySession(sessionId: String)

    @Query("SELECT COUNT(*) FROM scanned_codes WHERE session_id = :sessionId AND raw_value = :value")
    suspend fun countDuplicates(sessionId: String, value: String): Int
}
