package com.resident.app.data.export

import android.content.Context
import android.os.Environment
import com.resident.app.data.entity.Resident
import dagger.hilt.android.qualifiers.ApplicationContext
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExcelExporter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun exportToExcel(residents: List<Resident>, filterDesc: String = "全部"): Result<String> {
        return try {
            val workbook = HSSFWorkbook()
            val sheet = workbook.createSheet("居民信息")

            // 收集所有自定义字段名
            val allCustomKeys = residents.flatMap { it.customFields.keys }.distinct()

            // 表头
            val headerRow = sheet.createRow(0)
            val baseHeaders = listOf("ID", "姓名", "性别", "出生年月日", "年龄", "受教育水平", "职业", "电话", "地址")
            val allHeaders = baseHeaders + allCustomKeys + listOf("录入时间")
            allHeaders.forEachIndexed { index, header ->
                headerRow.createCell(index).setCellValue(header)
            }

            // 数据行
            residents.forEachIndexed { rowIndex, resident ->
                val row = sheet.createRow(rowIndex + 1)
                row.createCell(0).setCellValue(resident.id.toDouble())
                row.createCell(1).setCellValue(resident.name)
                row.createCell(2).setCellValue(resident.gender)
                row.createCell(3).setCellValue(resident.birthDate)
                row.createCell(4).setCellValue(resident.age.toDouble())
                row.createCell(5).setCellValue(resident.education)
                row.createCell(6).setCellValue(resident.occupation)
                row.createCell(7).setCellValue(resident.phone)
                row.createCell(8).setCellValue(resident.address)
                allCustomKeys.forEachIndexed { i, key ->
                    row.createCell(9 + i).setCellValue(resident.customFields[key] ?: "")
                }
                val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    .format(Date(resident.createdAt))
                row.createCell(9 + allCustomKeys.size).setCellValue(timeStr)
            }

            // 自动列宽
            for (i in allHeaders.indices) { sheet.autoSizeColumn(i) }

            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            val safeFilter = filterDesc.replace("/", "_")
            val fileName = "润泽知园居民_${safeFilter}_$timestamp.xls"
            val file = File(downloadsDir, fileName)

            FileOutputStream(file).use { workbook.write(it) }
            workbook.close()

            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
