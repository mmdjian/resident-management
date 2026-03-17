package com.resident.app.data.import_excel

import android.content.Context
import android.net.Uri
import com.resident.app.data.entity.Resident
import dagger.hilt.android.qualifiers.ApplicationContext
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExcelImporter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    data class ImportResult(
        val success: Int,
        val failed: Int,
        val residents: List<Resident>,
        val errorMsg: String = ""
    )

    // 已知列名映射
    private val knownHeaders = setOf(
        "姓名", "性别", "出生年月日", "出生日期", "年龄",
        "受教育水平", "学历", "职业", "电话", "手机", "地址", "住址", "id", "ID", "录入时间"
    )

    fun importFromExcel(uri: Uri): ImportResult {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return ImportResult(0, 0, emptyList(), "无法读取文件")

            val workbook = HSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)
            val residents = mutableListOf<Resident>()
            var failedCount = 0

            if (sheet.physicalNumberOfRows < 2) {
                return ImportResult(0, 0, emptyList(), "文件为空或只有表头，没有数据行")
            }

            // 读取表头（第一行）
            val headerRow = sheet.getRow(0) ?: return ImportResult(0, 0, emptyList(), "读取表头失败")
            val headers = (0 until headerRow.lastCellNum).map { i ->
                headerRow.getCell(i)?.toString()?.trim() ?: ""
            }

            if (!headers.contains("姓名")) {
                return ImportResult(0, 0, emptyList(), "未找到「姓名」列，请检查表头是否正确")
            }

            // 找到各列的索引
            val nameIdx = headers.indexOf("姓名")
            val genderIdx = headers.indexOf("性别")
            val birthIdx = maxOf(headers.indexOf("出生年月日"), headers.indexOf("出生日期"))
            val ageIdx = headers.indexOf("年龄")
            val eduIdx = maxOf(headers.indexOf("受教育水平"), headers.indexOf("学历"))
            val occupIdx = headers.indexOf("职业")
            val phoneIdx = maxOf(headers.indexOf("电话"), headers.indexOf("手机"))
            val addrIdx = maxOf(headers.indexOf("地址"), headers.indexOf("住址"))

            // 自定义字段列（不在已知列名中的）
            val customFieldIndices = headers.mapIndexedNotNull { i, h ->
                if (h.isNotEmpty() && h !in knownHeaders) i to h else null
            }

            // 从第二行开始读数据
            for (rowIndex in 1 until sheet.physicalNumberOfRows) {
                val row = sheet.getRow(rowIndex) ?: continue

                fun cellStr(idx: Int): String {
                    if (idx < 0) return ""
                    val cell = row.getCell(idx) ?: return ""
                    return when (cell.cellType) {
                        org.apache.poi.ss.usermodel.CellType.NUMERIC -> {
                            val num = cell.numericCellValue
                            if (num == num.toLong().toDouble()) num.toLong().toString()
                            else num.toString()
                        }
                        else -> cell.toString().trim()
                    }
                }

                val name = cellStr(nameIdx)
                if (name.isEmpty()) { failedCount++; continue }

                val birthDateRaw = cellStr(birthIdx)
                val birthDate = normalizeDateStr(birthDateRaw)
                val ageStr = cellStr(ageIdx)
                val age = if (birthDate.isNotEmpty()) calcAge(birthDate)
                          else ageStr.toDoubleOrNull()?.toInt() ?: 0

                // 自定义字段
                val customFields = mutableMapOf<String, String>()
                customFieldIndices.forEach { (i, key) ->
                    val v = cellStr(i)
                    if (v.isNotEmpty()) customFields[key] = v
                }

                residents.add(
                    Resident(
                        name = name,
                        gender = cellStr(genderIdx),
                        birthDate = birthDate,
                        age = age,
                        education = cellStr(eduIdx),
                        occupation = cellStr(occupIdx),
                        phone = cellStr(phoneIdx),
                        address = cellStr(addrIdx),
                        customFields = customFields
                    )
                )
            }

            workbook.close()
            inputStream.close()

            ImportResult(residents.size, failedCount, residents)
        } catch (e: Exception) {
            ImportResult(0, 0, emptyList(), "解析失败：${e.message}")
        }
    }

    private fun normalizeDateStr(raw: String): String {
        if (raw.isEmpty()) return ""
        // 尝试常见日期格式
        val formats = listOf("yyyy-MM-dd", "yyyy/MM/dd", "yyyy.MM.dd", "yyyyMMdd")
        for (fmt in formats) {
            try {
                val date = LocalDate.parse(raw, DateTimeFormatter.ofPattern(fmt))
                return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            } catch (e: Exception) { }
        }
        // 如果是纯数字年份（如 1980）
        if (raw.matches(Regex("\\d{4}"))) return ""
        return raw
    }

    private fun calcAge(dateStr: String): Int {
        return try {
            val birth = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            ChronoUnit.YEARS.between(birth, LocalDate.now()).toInt()
        } catch (e: Exception) { 0 }
    }
}
