package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.local.db.AppDatabase
import com.example.data.repository.ExportRepositoryImpl
import com.example.data.repository.QrRepositoryImpl
import com.example.data.repository.ScanRepositoryImpl
import com.example.domain.repository.ExportRepository
import com.example.domain.repository.QrRepository
import com.example.domain.repository.ScanRepository
import com.example.domain.usecase.DeleteSessionUseCase
import com.example.domain.usecase.ExportSessionUseCase
import com.example.domain.usecase.GenerateQrCodeUseCase
import com.example.domain.usecase.GetAllSessionsUseCase
import com.example.domain.usecase.GetSessionCodesUseCase
import com.example.domain.usecase.SaveScannedCodeUseCase

class AppContainer(private val context: Context) {

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "qr_suite.db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    val scanRepository: ScanRepository by lazy {
        ScanRepositoryImpl(
            database.scannedCodeDao(),
            database.scanSessionDao()
        )
    }

    val exportRepository: ExportRepository by lazy {
        ExportRepositoryImpl(context.applicationContext)
    }

    val qrRepository: QrRepository by lazy {
        QrRepositoryImpl(context.applicationContext)
    }

    // Use cases
    val saveScannedCodeUseCase: SaveScannedCodeUseCase by lazy {
        SaveScannedCodeUseCase(scanRepository)
    }

    val getSessionCodesUseCase: GetSessionCodesUseCase by lazy {
        GetSessionCodesUseCase(scanRepository)
    }

    val getAllSessionsUseCase: GetAllSessionsUseCase by lazy {
        GetAllSessionsUseCase(scanRepository)
    }

    val deleteSessionUseCase: DeleteSessionUseCase by lazy {
        DeleteSessionUseCase(scanRepository)
    }

    val exportSessionUseCase: ExportSessionUseCase by lazy {
        ExportSessionUseCase(scanRepository, exportRepository)
    }

    val generateQrCodeUseCase: GenerateQrCodeUseCase by lazy {
        GenerateQrCodeUseCase(qrRepository)
    }
}
