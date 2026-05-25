package com.example.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.local.dao.ScannedCodeDao
import com.example.data.local.dao.ScanSessionDao
import com.example.data.local.entity.ScannedCodeEntity
import com.example.data.local.entity.ScanSessionEntity

@Database(
    entities = [ScannedCodeEntity::class, ScanSessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scannedCodeDao(): ScannedCodeDao
    abstract fun scanSessionDao(): ScanSessionDao
}
