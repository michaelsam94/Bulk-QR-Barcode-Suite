package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.ScanSessionEntity
import kotlinx.coroutines.flow.Flow

data class SessionWithCount(
    val id: String,
    val name: String,
    val timestampMs: Long,
    val codeCount: Int
)

@Dao
interface ScanSessionDao {
    @Query("""
        SELECT s.id, s.name, s.timestamp_ms as timestampMs, COUNT(c.id) as codeCount 
        FROM scan_sessions s 
        LEFT JOIN scanned_codes c ON s.id = c.session_id 
        GROUP BY s.id 
        ORDER BY s.timestamp_ms DESC
    """)
    fun getAllSessionsWithCounts(): Flow<List<SessionWithCount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: ScanSessionEntity)

    @Query("DELETE FROM scan_sessions WHERE id = :sessionId")
    suspend fun deleteById(sessionId: String)
}
