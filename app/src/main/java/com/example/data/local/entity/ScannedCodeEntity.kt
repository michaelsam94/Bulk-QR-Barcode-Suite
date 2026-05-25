package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "scanned_codes",
    indices = [
        Index(value = ["session_id"]),
        Index(value = ["raw_value", "session_id"], unique = false)
    ]
)
data class ScannedCodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "raw_value") val rawValue: String,
    @ColumnInfo(name = "format") val format: String,
    @ColumnInfo(name = "timestamp_ms") val timestampMs: Long,
    @ColumnInfo(name = "session_id") val sessionId: String
)
