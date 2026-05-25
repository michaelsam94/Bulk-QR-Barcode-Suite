package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.domain.entity.ExportFormat
import com.example.domain.entity.ScannedCode
import com.example.domain.repository.ExportRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportRepositoryImpl(private val context: Context) : ExportRepository {

    override suspend fun exportSession(
        codes: List<ScannedCode>,
        format: ExportFormat,
        destinationUri: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val uri = Uri.parse(destinationUri)
            val outputStream = context.contentResolver.openOutputStream(uri) 
                ?: throw Exception("Could not open destination output stream.")

            when (format) {
                ExportFormat.CSV -> {
                    outputStream.use { stream ->
                        BufferedWriter(OutputStreamWriter(stream)).use { writer ->
                            writer.write("ID,Value,Format,Timestamp,Readable_Date\n")
                            codes.forEach { code ->
                                val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                                    .format(Date(code.timestampMs))
                                writer.write("${code.id},\"${code.rawValue.escapeCsv()}\",${code.format.name},${code.timestampMs},\"${dateStr}\"\n")
                            }
                        }
                    }
                }
                ExportFormat.XLSX -> {
                    outputStream.use { stream ->
                        val workbook = XSSFWorkbook()
                        val sheet = workbook.createSheet("Scanned Session Codes")

                        // Styled Column Header
                        val headerFont = workbook.createFont().apply {
                            bold = true
                            color = IndexedColors.WHITE.index
                        }
                        val headerStyle = workbook.createCellStyle().apply {
                            setFont(headerFont)
                            fillForegroundColor = IndexedColors.TEAL.index
                            fillPattern = FillPatternType.SOLID_FOREGROUND
                        }

                        // Create Headers
                        val headers = listOf("ID", "Scanned Raw Value", "Format", "Timestamp", "Readable Local Date")
                        val headerRow = sheet.createRow(0)
                        headers.forEachIndexed { i, title ->
                            val cell = headerRow.createCell(i)
                            cell.setCellValue(title)
                            cell.cellStyle = headerStyle
                        }

                        // Populate rows
                        codes.forEachIndexed { idx, code ->
                            val row = sheet.createRow(idx + 1)
                            row.createCell(0).setCellValue(code.id.toDouble())
                            row.createCell(1).setCellValue(code.rawValue)
                            row.createCell(2).setCellValue(code.format.name)
                            row.createCell(3).setCellValue(code.timestampMs.toDouble())
                            
                            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                                .format(Date(code.timestampMs))
                            row.createCell(4).setCellValue(dateStr)
                        }

                        // Autoresize columns to keep spreadsheet highly readable
                        repeat(headers.size) { colIndex ->
                            try {
                                sheet.autoSizeColumn(colIndex)
                            } catch (e: Exception) {
                                // Fallback if autoSize fails in headless env
                            }
                        }

                        workbook.write(stream)
                        workbook.close()
                    }
                }
            }
        }
    }

    private fun String.escapeCsv(): String {
        return this.replace("\"", "\"\"")
    }
}
