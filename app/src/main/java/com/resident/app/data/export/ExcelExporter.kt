package com.resident.app.data.export

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import android.os.Environment
import com.resident.app.data.entity.Resident
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExcelExporter @Inject constructor(
    private val context: Context
) {
    fun exportToExcel(residents: List<Resident>): Result<String> {
        return try {
            val workbook = HSSFWorkbook()
            val sheet = workbook.createSheet("居民信息")

            // 创建表头
            val headerRow = sheet.createRow(0)
            val headers = arrayOf("ID", "姓名", "性别", "年龄", "职业", "电话", "地址", "创建时间")
            headers.forEachIndexed { index, header ->
                headerRow.createCell(index).setCellValue(header)
            }

            // 填充数据
            residents.forEachIndexed { rowIndex, resident ->
                val row = sheet.createRow(rowIndex + 1)
                row.createCell(0).setCellValue(resident.id.toDouble())
                row.createCell(1).setCellValue(resident.name)
                row.createCell(2).setCellValue(resident.gender)
                row.createCell(3).setCellValue(resident.age.toDouble())
                row.createCell(4).setCellValue(resident.occupation)
                row.createCell(5).setCellValue(resident.phone)
                row.createCell(6).setCellValue(resident.address)
                row.createCell(7).setCellValue(resident.createdAt.toString())
            }

            // 保存文件
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val timestamp = System.currentTimeMillis()
            val fileName = "居民信息_$timestamp.xls"
            val file = File(downloadsDir, fileName)

            FileOutputStream(file).use { workbook.write(it) }
            workbook.close()

            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


