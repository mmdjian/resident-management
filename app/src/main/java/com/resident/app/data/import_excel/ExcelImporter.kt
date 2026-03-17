package com.resident.app.data.import_excel

import android.content.Context
import android.net.Uri
import com.resident.app.data.entity.Resident
import dagger.hilt.android.qualifiers.ApplicationContext
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.w3c.dom.Element
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton
import javax.xml.parsers.DocumentBuilderFactory

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
        "受教育水平", "学历", "政治面貌", "电话", "手机", "地址", "住址", "id", "ID", "录入时间"
    )

    fun importFromExcel(uri: Uri): ImportResult {
        return try {
            val fileName = uri.lastPathSegment ?: "未知文件"
            val fileNameLower = fileName.lowercase()

            android.util.Log.d("ExcelImport", "文件名: $fileName, 小写: $fileNameLower")

            when {
                fileNameLower.endsWith(".csv") -> {
                    android.util.Log.d("ExcelImport", "识别为 CSV 文件")
                    importCsv(uri)
                }
                fileNameLower.endsWith(".xls") && !fileNameLower.endsWith(".xlsx") -> {
                    android.util.Log.d("ExcelImport", "识别为 XLS 文件")
                    importXls(uri)
                }
                fileNameLower.endsWith(".xlsx") -> {
                    android.util.Log.d("ExcelImport", "识别为 XLSX 文件")
                    importXlsx(uri)
                }
                else -> {
                    android.util.Log.w("ExcelImport", "无法从文件名判断类型，尝试多种格式")
                    // 文件名不可靠时（部分文件管理器返回编码路径），先尝试 xlsx，失败再试 xls
                    try {
                        android.util.Log.d("ExcelImport", "尝试作为 XLSX 解析")
                        importXlsx(uri)
                    }
                    catch (e: Exception) {
                        android.util.Log.d("ExcelImport", "XLSX 解析失败，尝试 XLS: ${e.message}")
                        try {
                            importXls(uri)
                        }
                        catch (e2: Exception) {
                            android.util.Log.d("ExcelImport", "XLS 解析失败，尝试 CSV: ${e2.message}")
                            importCsv(uri)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ExcelImport", "导入失败", e)
            ImportResult(0, 0, emptyList(), "解析失败：${e.message}")
        }
    }

    // ──────────────── .xls（HSSFWorkbook，POI 3.17）────────────────

    private fun importXls(uri: Uri): ImportResult {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: return ImportResult(0, 0, emptyList(), "无法读取文件")
        return try {
            android.util.Log.d("ExcelImport", "开始解析 XLS 文件")
            val workbook = HSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)

            if (sheet.physicalNumberOfRows < 2)
                return ImportResult(0, 0, emptyList(), "文件为空或只有表头，没有数据行")

            android.util.Log.d("ExcelImport", "XLS 文件总行数: ${sheet.physicalNumberOfRows}")

            // 自动找到包含"姓名"的行作为表头
            var headerRowIndex = -1
            var headers = listOf<String>()

            for (i in 0 until minOf(10, sheet.physicalNumberOfRows)) {
                val row = sheet.getRow(i) ?: continue
                val rowHeaders = (0 until row.lastCellNum).map { j ->
                    row.getCell(j)?.toString()?.trim() ?: ""
                }

                android.util.Log.d("ExcelImport", "XLS 第${i + 1}行: ${rowHeaders.take(5)}...")

                if (rowHeaders.contains("姓名")) {
                    headerRowIndex = i
                    headers = rowHeaders
                    android.util.Log.d("ExcelImport", "XLS 找到表头在第 ${i + 1} 行")
                    break
                }
            }

            if (headerRowIndex == -1) {
                val row = sheet.getRow(0)
                if (row != null) {
                    headers = (0 until row.lastCellNum).map { i ->
                        row.getCell(i)?.toString()?.trim() ?: ""
                    }
                }
                android.util.Log.d("ExcelImport", "XLS 未找到包含'姓名'的行，使用第一行作为表头")
            }

            if (!headers.contains("姓名"))
                return ImportResult(0, 0, emptyList(), "未找到「姓名」列，请检查表头是否正确\n实际表头: ${headers.joinToString(", ")}")

            val nameIdx  = headers.indexOf("姓名")
            val genderIdx = headers.indexOf("性别")
            val birthIdx  = maxOf(headers.indexOf("出生年月日"), headers.indexOf("出生日期"))
            val ageIdx    = headers.indexOf("年龄")
            val eduIdx    = maxOf(headers.indexOf("受教育水平"), headers.indexOf("学历"))
            val occupIdx  = headers.indexOf("政治面貌")
            val phoneIdx  = maxOf(headers.indexOf("电话"), headers.indexOf("手机"))
            val addrIdx   = maxOf(headers.indexOf("地址"), headers.indexOf("住址"))
            val customFieldIndices = headers.mapIndexedNotNull { i, h ->
                if (h.isNotEmpty() && h !in knownHeaders) i to h else null
            }

            val residents = mutableListOf<Resident>()
            var failedCount = 0

            android.util.Log.d("ExcelImport", "XLS 表头在第 ${headerRowIndex + 1} 行, 开始解析数据行")

            for (rowIndex in (headerRowIndex + 1) until sheet.physicalNumberOfRows) {
                val row = sheet.getRow(rowIndex) ?: continue

                fun cellStr(idx: Int): String {
                    if (idx < 0) return ""
                    val cell = row.getCell(idx) ?: return ""
                    @Suppress("DEPRECATION")
                    return when (cell.cellType) {
                        0 -> { // CELL_TYPE_NUMERIC
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

                // 未识别字段归入备注
                val notesParts = mutableListOf<String>()
                customFieldIndices.forEach { (i, key) ->
                    val v = cellStr(i)
                    if (v.isNotEmpty()) notesParts.add("$key: $v")
                }
                val notes = notesParts.joinToString("；")

                residents.add(Resident(
                    name = name,
                    gender = cellStr(genderIdx),
                    birthDate = birthDate,
                    age = age,
                    education = cellStr(eduIdx),
                    occupation = cellStr(occupIdx),
                    phone = cellStr(phoneIdx),
                    address = cellStr(addrIdx),
                    notes = notes
                ))
            }

            workbook.close()
            ImportResult(residents.size, failedCount, residents)
        } finally {
            inputStream.close()
        }
    }

    // ──────────────── .xlsx（原生 ZIP + XML，无第三方依赖）────────────────

    private fun importXlsx(uri: Uri): ImportResult {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: return ImportResult(0, 0, emptyList(), "无法读取文件")

        return try {
            android.util.Log.d("ExcelImport", "开始解析 XLSX 文件")
            // xlsx 本质是 ZIP，解压获取 sharedStrings 和 sheet1
            val entries = mutableMapOf<String, ByteArray>()
            ZipInputStream(inputStream).use { zip ->
                var entry = zip.nextEntry
                var entryCount = 0
                while (entry != null) {
                    entryCount++
                    if (entry.name in listOf(
                            "xl/sharedStrings.xml",
                            "xl/worksheets/sheet1.xml"
                        )
                    ) {
                        entries[entry.name] = zip.readBytes()
                        android.util.Log.d("ExcelImport", "找到条目: ${entry.name}, 大小: ${entries[entry.name]?.size} bytes")
                    }
                    entry = zip.nextEntry
                }
                android.util.Log.d("ExcelImport", "ZIP 包共 $entryCount 个条目")
            }

            android.util.Log.d("ExcelImport", "已提取的条目: ${entries.keys}")

            val sheetBytes = entries["xl/worksheets/sheet1.xml"]
                ?: return ImportResult(0, 0, emptyList(), "无法读取工作表，文件可能已损坏")

            // 解析共享字符串表（sharedStrings.xml）
            val sharedStrings = mutableListOf<String>()
            entries["xl/sharedStrings.xml"]?.let { ssBytes ->
                android.util.Log.d("ExcelImport", "开始解析共享字符串表")
                val doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(ssBytes.inputStream())
                val siList = doc.getElementsByTagName("si")
                android.util.Log.d("ExcelImport", "共享字符串表共 ${siList.length} 个字符串")
                for (i in 0 until siList.length) {
                    val si = siList.item(i) as Element
                    // 拼接 <t> 标签内所有文字（处理富文本 <r><t>）
                    val tNodes = si.getElementsByTagName("t")
                    val sb = StringBuilder()
                    for (j in 0 until tNodes.length) {
                        sb.append(tNodes.item(j).textContent)
                    }
                    sharedStrings.add(sb.toString())
                }
                android.util.Log.d("ExcelImport", "解析完成，前5个字符串: ${sharedStrings.take(5)}")
            }

            // 解析 sheet1.xml
            val doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(sheetBytes.inputStream())

            val rowList = doc.getElementsByTagName("row")
            if (rowList.length < 2)
                return ImportResult(0, 0, emptyList(), "文件为空或只有表头，没有数据行")

            android.util.Log.d("ExcelImport", "总共 ${rowList.length} 行")

            // 读取表头（自动找到包含"姓名"的行）
            fun getCellValue(cell: Element): String {
                val t = cell.getAttribute("t")
                val v = cell.getElementsByTagName("v").item(0)?.textContent ?: ""
                return when (t) {
                    "s" -> sharedStrings.getOrNull(v.toIntOrNull() ?: -1) ?: ""
                    "inlineStr" -> cell.getElementsByTagName("t").item(0)?.textContent ?: ""
                    else -> {
                        // 数字：去掉多余小数点（如 123.0 → 123）
                        val d = v.toDoubleOrNull()
                        if (d != null && d == d.toLong().toDouble()) d.toLong().toString() else v
                    }
                }
            }

            // 将列号字母转为 0-based 索引（A→0, B→1, AA→26…）
            fun colIndex(ref: String): Int {
                val letters = ref.takeWhile { it.isLetter() }
                return letters.fold(0) { acc, c -> acc * 26 + (c - 'A' + 1) } - 1
            }

            // 自动找到包含"姓名"的行作为表头
            var headerRowIndex = -1
            var headerMap = mapOf<Int, String>()  // 改用 Map 保存表头，key 是实际列索引

            for (i in 0 until minOf(10, rowList.length)) {  // 只检查前10行，避免遍历整个大文件
                val row = rowList.item(i) as Element
                val cells = row.getElementsByTagName("c")

                val cellMap = mutableMapOf<Int, String>()
                for (j in 0 until cells.length) {
                    val cell = cells.item(j) as Element
                    val col = colIndex(cell.getAttribute("r").takeWhile { it.isLetter() })
                    cellMap[col] = getCellValue(cell).trim()
                }

                android.util.Log.d("ExcelImport", "第${i + 1}行内容: ${cellMap.values.take(5)}, cellMap keys: ${cellMap.keys.toList()}")

                if (cellMap.values.contains("姓名")) {
                    headerRowIndex = i
                    headerMap = cellMap.toMap()
                    android.util.Log.d("ExcelImport", "找到表头在第 ${i + 1} 行, headerMap: $headerMap")
                    break
                }
            }

            if (headerRowIndex == -1) {
                // 如果没找到，尝试用第一行
                val firstRow = rowList.item(0) as Element
                val cells = firstRow.getElementsByTagName("c")
                val cellMap = mutableMapOf<Int, String>()
                for (j in 0 until cells.length) {
                    val cell = cells.item(j) as Element
                    val col = colIndex(cell.getAttribute("r").takeWhile { it.isLetter() })
                    cellMap[col] = getCellValue(cell).trim()
                }
                headerMap = cellMap.toMap()
                android.util.Log.d("ExcelImport", "未找到包含'姓名'的行，使用第一行作为表头, headerMap: $headerMap")
            }

            android.util.Log.d("ExcelImport", "最终表头映射: $headerMap")
            if (!headerMap.values.contains("姓名"))
                return ImportResult(0, 0, emptyList(), "未找到「姓名」列，请检查表头是否正确\n实际表头: ${headerMap.values.joinToString(", ")}")

            // 使用列名查找实际列索引
            val nameIdx   = headerMap.entries.find { it.value == "姓名" }?.key ?: -1
            val genderIdx = headerMap.entries.find { it.value == "性别" }?.key ?: -1
            val birthIdx  = headerMap.entries.find { it.value in listOf("出生年月日", "出生日期") }?.key ?: -1
            val ageIdx    = headerMap.entries.find { it.value == "年龄" }?.key ?: -1
            val eduIdx    = headerMap.entries.find { it.value in listOf("受教育水平", "学历") }?.key ?: -1
            val occupIdx  = headerMap.entries.find { it.value == "政治面貌" }?.key ?: -1
            val phoneIdx  = headerMap.entries.find { it.value in listOf("电话", "手机") }?.key ?: -1
            val addrIdx   = headerMap.entries.find { it.value in listOf("地址", "住址") }?.key ?: -1

            android.util.Log.d("ExcelImport", "列索引映射 - 姓名:$nameIdx, 性别:$genderIdx, 出生:$birthIdx, 年龄:$ageIdx, 学历:$eduIdx, 政治面貌:$occupIdx, 电话:$phoneIdx, 地址:$addrIdx")

            val customFieldIndices = headerMap.mapNotNull { (colIdx, headerName) ->
                if (headerName.isNotEmpty() && headerName !in knownHeaders) colIdx to headerName else null
            }

            val residents = mutableListOf<Resident>()
            var failedCount = 0

            android.util.Log.d("ExcelImport", "总行数: ${rowList.length}, 表头在第 ${headerRowIndex + 1} 行, 开始解析数据行")

            // 从表头行的下一行开始解析数据
            for (rowIndex in (headerRowIndex + 1) until rowList.length) {
                val row = rowList.item(rowIndex) as Element
                val cells = row.getElementsByTagName("c")

                // 按列索引建立 Map
                val cellMap = mutableMapOf<Int, String>()
                for (i in 0 until cells.length) {
                    val cell = cells.item(i) as Element
                    val col = colIndex(cell.getAttribute("r").takeWhile { it.isLetter() })
                    cellMap[col] = getCellValue(cell).trim()
                }

                fun col(idx: Int) = if (idx < 0) "" else cellMap[idx] ?: ""

                val name = col(nameIdx)
                val phone = col(phoneIdx)

                if (rowIndex <= 3 || phone.isEmpty()) {
                    android.util.Log.d("ExcelImport", "第${rowIndex + 1}行数据 - 姓名: $name, 性别: ${col(genderIdx)}, 电话: $phone (phoneIdx=$phoneIdx), 地址: ${col(addrIdx)}")
                    android.util.Log.d("ExcelImport", "第${rowIndex + 1}行 cellMap 键值: ${cellMap.keys.toList()}")
                }

                if (name.isEmpty()) {
                    android.util.Log.d("ExcelImport", "第${rowIndex + 1}行跳过（姓名为空）")
                    failedCount++; continue
                }

                val birthDateRaw = col(birthIdx)
                val birthDate = normalizeDateStr(birthDateRaw)
                val ageStr = col(ageIdx)
                val age = if (birthDate.isNotEmpty()) calcAge(birthDate)
                else ageStr.toDoubleOrNull()?.toInt() ?: 0

                val notesParts2 = mutableListOf<String>()
                customFieldIndices.forEach { (i, key) ->
                    val v = col(i)
                    if (v.isNotEmpty()) notesParts2.add("$key: $v")
                }

                residents.add(Resident(
                    name = name,
                    gender = col(genderIdx),
                    birthDate = birthDate,
                    age = age,
                    education = col(eduIdx),
                    occupation = col(occupIdx),
                    phone = col(phoneIdx),
                    address = col(addrIdx),
                    notes = notesParts2.joinToString("；")
                ))
            }

            android.util.Log.d("ExcelImport", "解析完成 - 成功: ${residents.size}, 失败: $failedCount")

            ImportResult(residents.size, failedCount, residents)
        } finally {
            inputStream.close()
        }
    }

    // ──────────────── CSV / TSV（原生解析，无依赖）────────────────

    private fun importCsv(uri: Uri): ImportResult {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: return ImportResult(0, 0, emptyList(), "无法读取文件")

        return try {
            android.util.Log.d("ExcelImport", "开始解析 CSV 文件")
            val lines = inputStream.bufferedReader(Charsets.UTF_8).readLines()
                .ifEmpty { return ImportResult(0, 0, emptyList(), "文件为空") }

            android.util.Log.d("ExcelImport", "CSV 文件总行数: ${lines.size}")

            // 自动检测分隔符：逗号 or 制表符
            val firstLine = lines.first()
            val delimiter = if (firstLine.contains('\t')) '\t' else ','

            fun parseLine(line: String): List<String> {
                // 简单 CSV 解析：处理带引号的字段
                val result = mutableListOf<String>()
                var inQuote = false
                val current = StringBuilder()
                for (ch in line) {
                    when {
                        ch == '"' -> inQuote = !inQuote
                        ch == delimiter && !inQuote -> { result.add(current.toString().trim()); current.clear() }
                        else -> current.append(ch)
                    }
                }
                result.add(current.toString().trim())
                return result
            }

            // 自动找到包含"姓名"的行作为表头
            var headerRowIndex = -1
            var headers = listOf<String>()

            for (i in 0 until minOf(10, lines.size)) {
                val rowHeaders = parseLine(lines[i])

                android.util.Log.d("ExcelImport", "CSV 第${i + 1}行: ${rowHeaders.take(5)}...")

                if (rowHeaders.contains("姓名")) {
                    headerRowIndex = i
                    headers = rowHeaders
                    android.util.Log.d("ExcelImport", "CSV 找到表头在第 ${i + 1} 行")
                    break
                }
            }

            if (headerRowIndex == -1) {
                headers = parseLine(lines.first())
                android.util.Log.d("ExcelImport", "CSV 未找到包含'姓名'的行，使用第一行作为表头")
            }

            if (!headers.contains("姓名"))
                return ImportResult(0, 0, emptyList(), "未找到「姓名」列，请检查表头是否正确\n实际表头: ${headers.joinToString(", ")}")

            val nameIdx   = headers.indexOf("姓名")
            val genderIdx = headers.indexOf("性别")
            val birthIdx  = maxOf(headers.indexOf("出生年月日"), headers.indexOf("出生日期"))
            val ageIdx    = headers.indexOf("年龄")
            val eduIdx    = maxOf(headers.indexOf("受教育水平"), headers.indexOf("学历"))
            val occupIdx  = headers.indexOf("政治面貌")
            val phoneIdx  = maxOf(headers.indexOf("电话"), headers.indexOf("手机"))
            val addrIdx   = maxOf(headers.indexOf("地址"), headers.indexOf("住址"))
            val customFieldIndices = headers.mapIndexedNotNull { i, h ->
                if (h.isNotEmpty() && h !in knownHeaders) i to h else null
            }

            val residents = mutableListOf<Resident>()
            var failedCount = 0

            android.util.Log.d("ExcelImport", "CSV 表头在第 ${headerRowIndex + 1} 行, 开始解析数据行")

            for (lineIndex in (headerRowIndex + 1) until lines.size) {
                val line = lines[lineIndex]
                if (line.isBlank()) continue
                val cols = parseLine(line)
                fun col(idx: Int) = if (idx < 0 || idx >= cols.size) "" else cols[idx]

                val name = col(nameIdx)
                if (name.isEmpty()) { failedCount++; continue }

                val birthDateRaw = col(birthIdx)
                val birthDate = normalizeDateStr(birthDateRaw)
                val ageStr = col(ageIdx)
                val age = if (birthDate.isNotEmpty()) calcAge(birthDate)
                          else ageStr.toDoubleOrNull()?.toInt() ?: 0

                val customFields = mutableMapOf<String, String>()
                customFieldIndices.forEach { (i, key) ->
                    val v = col(i)
                    if (v.isNotEmpty()) customFields[key] = v
                }

                residents.add(Resident(
                    name = name,
                    gender = col(genderIdx),
                    birthDate = birthDate,
                    age = age,
                    education = col(eduIdx),
                    occupation = col(occupIdx),
                    phone = col(phoneIdx),
                    address = col(addrIdx),
                    notes = customFields.entries.joinToString("；") { "${it.key}: ${it.value}" }
                ))
            }

            ImportResult(residents.size, failedCount, residents)
        } finally {
            inputStream.close()
        }
    }

    // ──────────────── 工具函数 ────────────────

    private fun normalizeDateStr(raw: String): String {
        if (raw.isEmpty()) return ""
        val formats = listOf("yyyy-MM-dd", "yyyy/MM/dd", "yyyy.MM.dd", "yyyyMMdd")
        for (fmt in formats) {
            try {
                val date = LocalDate.parse(raw, DateTimeFormatter.ofPattern(fmt))
                return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            } catch (e: Exception) { }
        }
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
