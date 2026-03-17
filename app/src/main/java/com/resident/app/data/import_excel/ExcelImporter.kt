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
            val fileName = uri.lastPathSegment?.lowercase() ?: ""
            when {
                fileName.endsWith(".csv") -> importCsv(uri)
                fileName.endsWith(".xls") && !fileName.endsWith(".xlsx") -> importXls(uri)
                fileName.endsWith(".xlsx") -> importXlsx(uri)
                else -> {
                    // 文件名不可靠时（部分文件管理器返回编码路径），先尝试 xlsx，失败再试 xls
                    try { importXlsx(uri) }
                    catch (e: Exception) {
                        try { importXls(uri) }
                        catch (e2: Exception) { importCsv(uri) }
                    }
                }
            }
        } catch (e: Exception) {
            ImportResult(0, 0, emptyList(), "解析失败：${e.message}")
        }
    }

    // ──────────────── .xls（HSSFWorkbook，POI 3.17）────────────────

    private fun importXls(uri: Uri): ImportResult {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: return ImportResult(0, 0, emptyList(), "无法读取文件")
        return try {
            val workbook = HSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)

            if (sheet.physicalNumberOfRows < 2)
                return ImportResult(0, 0, emptyList(), "文件为空或只有表头，没有数据行")

            val headerRow = sheet.getRow(0)
                ?: return ImportResult(0, 0, emptyList(), "读取表头失败")
            val headers = (0 until headerRow.lastCellNum).map { i ->
                headerRow.getCell(i)?.toString()?.trim() ?: ""
            }

            if (!headers.contains("姓名"))
                return ImportResult(0, 0, emptyList(), "未找到「姓名」列，请检查表头是否正确")

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

            for (rowIndex in 1 until sheet.physicalNumberOfRows) {
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
            // xlsx 本质是 ZIP，解压获取 sharedStrings 和 sheet1
            val entries = mutableMapOf<String, ByteArray>()
            ZipInputStream(inputStream).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (entry.name in listOf(
                            "xl/sharedStrings.xml",
                            "xl/worksheets/sheet1.xml"
                        )
                    ) {
                        entries[entry.name] = zip.readBytes()
                    }
                    entry = zip.nextEntry
                }
            }

            val sheetBytes = entries["xl/worksheets/sheet1.xml"]
                ?: return ImportResult(0, 0, emptyList(), "无法读取工作表，文件可能已损坏")

            // 解析共享字符串表（sharedStrings.xml）
            val sharedStrings = mutableListOf<String>()
            entries["xl/sharedStrings.xml"]?.let { ssBytes ->
                val doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(ssBytes.inputStream())
                val siList = doc.getElementsByTagName("si")
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
            }

            // 解析 sheet1.xml
            val doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(sheetBytes.inputStream())

            val rowList = doc.getElementsByTagName("row")
            if (rowList.length < 2)
                return ImportResult(0, 0, emptyList(), "文件为空或只有表头，没有数据行")

            // 读取表头（第一行）
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

            // 解析第一行为 headers
            val headerRowElem = rowList.item(0) as Element
            val headerCells = headerRowElem.getElementsByTagName("c")
            val maxCol = (0 until headerCells.length).maxOfOrNull { i ->
                colIndex((headerCells.item(i) as Element).getAttribute("r").takeWhile { it.isLetter() })
            } ?: 0
            val headers = Array(maxCol + 1) { "" }
            for (i in 0 until headerCells.length) {
                val cell = headerCells.item(i) as Element
                val col = colIndex(cell.getAttribute("r").takeWhile { it.isLetter() })
                headers[col] = getCellValue(cell).trim()
            }

            val headerList = headers.toList()
            if (!headerList.contains("姓名"))
                return ImportResult(0, 0, emptyList(), "未找到「姓名」列，请检查表头是否正确")

            val nameIdx   = headerList.indexOf("姓名")
            val genderIdx = headerList.indexOf("性别")
            val birthIdx  = maxOf(headerList.indexOf("出生年月日"), headerList.indexOf("出生日期"))
            val ageIdx    = headerList.indexOf("年龄")
            val eduIdx    = maxOf(headerList.indexOf("受教育水平"), headerList.indexOf("学历"))
            val occupIdx  = headerList.indexOf("政治面貌")
            val phoneIdx  = maxOf(headerList.indexOf("电话"), headerList.indexOf("手机"))
            val addrIdx   = maxOf(headerList.indexOf("地址"), headerList.indexOf("住址"))
            val customFieldIndices = headerList.mapIndexedNotNull { i, h ->
                if (h.isNotEmpty() && h !in knownHeaders) i to h else null
            }

            val residents = mutableListOf<Resident>()
            var failedCount = 0

            for (rowIndex in 1 until rowList.length) {
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
                if (name.isEmpty()) { failedCount++; continue }

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
            val lines = inputStream.bufferedReader(Charsets.UTF_8).readLines()
                .ifEmpty { return ImportResult(0, 0, emptyList(), "文件为空") }

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

            val headers = parseLine(firstLine)
            if (!headers.contains("姓名"))
                return ImportResult(0, 0, emptyList(), "未找到「姓名」列，请检查表头是否正确")

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

            for (lineIndex in 1 until lines.size) {
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
