package com.example.data.mapper

import com.example.data.local.entity.ScannedCodeEntity
import com.example.data.local.entity.ScanSessionEntity
import com.example.data.local.dao.SessionWithCount
import com.example.domain.entity.BarcodeFormat
import com.example.domain.entity.ScannedCode
import com.example.domain.entity.ScanSession
import com.google.mlkit.vision.barcode.common.Barcode

fun ScannedCodeEntity.toDomain(): ScannedCode {
    val domainFormat = try {
        BarcodeFormat.valueOf(this.format)
    } catch (e: Exception) {
        BarcodeFormat.UNKNOWN
    }
    return ScannedCode(
        id = this.id,
        rawValue = this.rawValue,
        format = domainFormat,
        timestampMs = this.timestampMs,
        sessionId = this.sessionId
    )
}

fun ScannedCode.toEntity(): ScannedCodeEntity {
    return ScannedCodeEntity(
        id = this.id,
        rawValue = this.rawValue,
        format = this.format.name,
        timestampMs = this.timestampMs,
        sessionId = this.sessionId
    )
}

fun SessionWithCount.toDomain(): ScanSession {
    return ScanSession(
        id = this.id,
        name = this.name,
        timestampMs = this.timestampMs,
        codeCount = this.codeCount
    )
}

fun Int.toDomainFormat(): BarcodeFormat {
    return when (this) {
        Barcode.FORMAT_QR_CODE -> BarcodeFormat.QR_CODE
        Barcode.FORMAT_AZTEC -> BarcodeFormat.AZTEC
        Barcode.FORMAT_CODABAR -> BarcodeFormat.CODABAR
        Barcode.FORMAT_CODE_39 -> BarcodeFormat.CODE_39
        Barcode.FORMAT_CODE_93 -> BarcodeFormat.CODE_93
        Barcode.FORMAT_CODE_128 -> BarcodeFormat.CODE_128
        Barcode.FORMAT_DATA_MATRIX -> BarcodeFormat.DATA_MATRIX
        Barcode.FORMAT_EAN_8 -> BarcodeFormat.EAN_8
        Barcode.FORMAT_EAN_13 -> BarcodeFormat.EAN_13
        Barcode.FORMAT_ITF -> BarcodeFormat.ITF
        Barcode.FORMAT_PDF417 -> BarcodeFormat.PDF_417
        Barcode.FORMAT_UPC_A -> BarcodeFormat.UPC_A
        Barcode.FORMAT_UPC_E -> BarcodeFormat.UPC_E
        else -> BarcodeFormat.UNKNOWN
    }
}
