package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import com.example.domain.entity.ErrorCorrectionLevel
import com.example.domain.entity.QrGenerationConfig
import com.example.domain.repository.QrRepository
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel as ZxingErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class QrRepositoryImpl(private val context: Context) : QrRepository {

    override suspend fun generateQrCode(config: QrGenerationConfig): Result<String> =
        withContext(Dispatchers.Default) {
            runCatching {
                val hints = mutableMapOf<EncodeHintType, Any>(
                    EncodeHintType.ERROR_CORRECTION to config.errorCorrectionLevel.toZxing(),
                    EncodeHintType.MARGIN to 1
                )
                
                // Set character encoding to handle multi-byte characters
                hints[EncodeHintType.CHARACTER_SET] = "UTF-8"

                val bitMatrix = MultiFormatWriter().encode(
                    config.content,
                    com.google.zxing.BarcodeFormat.QR_CODE,
                    config.sizePx,
                    config.sizePx,
                    hints
                )

                val width = bitMatrix.width
                val height = bitMatrix.height
                val pixels = IntArray(width * height) { i ->
                    val x = i % width
                    val y = i / width
                    if (bitMatrix[x, y]) config.foregroundColor else config.backgroundColor
                }

                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                    setPixels(pixels, 0, width, 0, 0, width, height)
                }

                // If logo is defined, overlay it in the center
                val finalBitmap = if (!config.logoUri.isNullOrEmpty()) {
                    overlayLogo(bitmap, config.logoUri, config.sizePx)
                } else {
                    bitmap
                }

                saveBitmapToCache(finalBitmap)
            }
        }

    private fun ErrorCorrectionLevel.toZxing(): ZxingErrorCorrectionLevel {
        return when (this) {
            ErrorCorrectionLevel.L -> ZxingErrorCorrectionLevel.L
            ErrorCorrectionLevel.M -> ZxingErrorCorrectionLevel.M
            ErrorCorrectionLevel.Q -> ZxingErrorCorrectionLevel.Q
            ErrorCorrectionLevel.H -> ZxingErrorCorrectionLevel.H
        }
    }

    private suspend fun overlayLogo(qr: Bitmap, logoUri: String, qrSizePx: Int): Bitmap =
        withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(Uri.parse(logoUri))
                    ?: return@withContext qr
                val logoBitmap = inputStream.use { BitmapFactory.decodeStream(it) }
                    ?: return@withContext qr

                val logoSize = (qrSizePx * 0.22f).toInt() // Standard size is 20-25% of the QR code
                val scaled = Bitmap.createScaledBitmap(logoBitmap, logoSize, logoSize, true)
                val result = qr.copy(Bitmap.Config.ARGB_8888, true)
                val canvas = Canvas(result)
                val left = (qrSizePx - logoSize) / 2f
                val top = (qrSizePx - logoSize) / 2f

                // Draw solid background container with rounded border for logo breathing room
                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { 
                    color = android.graphics.Color.WHITE 
                }
                val padding = 10f
                canvas.drawRoundRect(
                    left - padding, 
                    top - padding, 
                    left + logoSize + padding, 
                    top + logoSize + padding, 
                    18f, 18f, 
                    bgPaint
                )

                // Render scaled logo inside
                canvas.drawBitmap(scaled, left, top, null)
                result
            } catch (e: Exception) {
                e.printStackTrace()
                qr // Return original on error
            }
        }

    private suspend fun saveBitmapToCache(bitmap: Bitmap): String =
        withContext(Dispatchers.IO) {
            val cacheDirectory = File(context.cacheDir, "generated_qr").apply {
                if (!exists()) mkdirs()
            }
            // Overwrite single preview file or generate name to avoid accumulation
            val file = File(cacheDirectory, "qr_preview_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            file.absolutePath
        }
}
