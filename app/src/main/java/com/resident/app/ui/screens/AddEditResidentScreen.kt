package com.resident.app.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.resident.app.data.entity.Resident
import com.resident.app.ui.viewmodel.ResidentViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

val EDUCATION_OPTIONS = listOf("", "初中及以下", "高中/中专", "大专", "本科", "硕士及以上")

val OCCUPATION_OPTIONS = listOf(
    "", "在职职工", "个体经营", "自由职业", "企业管理人员",
    "农民", "学生", "退休人员", "无业/待业", "其他"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditResidentScreen(
    viewModel: ResidentViewModel,
    resident: Resident?,
    onBack: () -> Unit
) {
    // 基本字段
    var name       by remember { mutableStateOf(resident?.name ?: "") }
    var gender     by remember { mutableStateOf(resident?.gender ?: "") }
    var birthDate  by remember { mutableStateOf(resident?.birthDate ?: "") }
    var age        by remember { mutableStateOf(
        if (resident?.birthDate?.isNotEmpty() == true) ""
        else resident?.age?.takeIf { it > 0 }?.toString() ?: ""
    ) }
    var education  by remember { mutableStateOf(resident?.education ?: "") }
    var occupation by remember { mutableStateOf(resident?.occupation ?: "") }
    var phone      by remember { mutableStateOf(resident?.phone ?: "") }
    var address    by remember { mutableStateOf(resident?.address ?: "") }
    var notes      by remember { mutableStateOf(resident?.notes ?: "") }

    // 自定义字段（已有 + 用户新增）
    var customFields by remember { mutableStateOf(
        resident?.customFields?.toMutableMap() ?: mutableMapOf()
    ) }

    // 下拉展开状态
    var genderExpanded    by remember { mutableStateOf(false) }
    var educationExpanded by remember { mutableStateOf(false) }
    var occupExpanded     by remember { mutableStateOf(false) }
    var showAddFieldDialog by remember { mutableStateOf(false) }
    var newFieldName       by remember { mutableStateOf("") }

    // 职业文字输入（当选"其他"时允许手动输入）
    var occupCustom  by remember { mutableStateOf(
        if (resident?.occupation != null && resident.occupation !in OCCUPATION_OPTIONS) resident.occupation else ""
    ) }
    val isCustomOccup = occupation == "其他"

    val genderOptions = listOf("男", "女")
    val isLoading by viewModel.isLoading.collectAsState()
    val message   by viewModel.message.collectAsState()
    val context   = LocalContext.current

    fun calcAge(dateStr: String): Int = try {
        ChronoUnit.YEARS.between(
            LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            LocalDate.now()
        ).toInt()
    } catch (e: Exception) { 0 }

    val displayAge = if (birthDate.isNotEmpty()) {
        val a = calcAge(birthDate); if (a > 0) "$a 岁" else ""
    } else age

    LaunchedEffect(message) {
        message?.let {
            if (it.contains("成功")) onBack()
            viewModel.clearMessage()
        }
    }

    // ── 添加自定义字段对话框 ──
    if (showAddFieldDialog) {
        AlertDialog(
            onDismissRequest = { showAddFieldDialog = false; newFieldName = "" },
            title = { Text("添加自定义字段") },
            text = {
                OutlinedTextField(
                    value = newFieldName,
                    onValueChange = { newFieldName = it },
                    label = { Text("字段名称，如：是否党员") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newFieldName.isNotBlank() && !customFields.containsKey(newFieldName)) {
                        customFields = (customFields + (newFieldName to "")).toMutableMap()
                    }
                    showAddFieldDialog = false; newFieldName = ""
                }) { Text("添加") }
            },
            dismissButton = {
                TextButton(onClick = { showAddFieldDialog = false; newFieldName = "" }) { Text("取消") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (resident == null) "添加居民" else "编辑居民信息") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── 章节标题 ──
            SectionTitle("基本信息")

            // 姓名
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("姓名 *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 性别下拉
            ExposedDropdownMenuBox(
                expanded = genderExpanded,
                onExpandedChange = { genderExpanded = !genderExpanded }
            ) {
                OutlinedTextField(
                    value = gender,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("性别") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(genderExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    placeholder = { Text("请选择") }
                )
                ExposedDropdownMenu(genderExpanded, { genderExpanded = false }) {
                    genderOptions.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt) },
                            onClick = { gender = opt; genderExpanded = false }
                        )
                    }
                }
            }

            // 出生日期
            OutlinedTextField(
                value = birthDate,
                onValueChange = {},
                readOnly = true,
                label = { Text("出生年月日") },
                placeholder = { Text("点击日历图标选择") },
                trailingIcon = {
                    IconButton(onClick = {
                        val today = LocalDate.now()
                        val initYear  = if (birthDate.isNotEmpty()) try { birthDate.substring(0,4).toInt() } catch (e: Exception) { today.year - 30 } else today.year - 30
                        val initMonth = if (birthDate.length >= 7)  try { birthDate.substring(5,7).toInt() - 1 } catch (e: Exception) { 0 } else 0
                        val initDay   = if (birthDate.length == 10) try { birthDate.substring(8,10).toInt() } catch (e: Exception) { 1 } else 1
                        DatePickerDialog(context, { _, y, m, d ->
                            birthDate = "%04d-%02d-%02d".format(y, m + 1, d)
                            age = ""
                        }, initYear, initMonth, initDay).show()
                    }) { Icon(Icons.Default.CalendarMonth, contentDescription = "选择日期") }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 年龄
            OutlinedTextField(
                value = displayAge,
                onValueChange = { if (birthDate.isEmpty()) age = it.filter { c -> c.isDigit() } },
                label = { Text(if (birthDate.isNotEmpty()) "年龄（自动计算）" else "年龄") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                readOnly = birthDate.isNotEmpty(),
                enabled = birthDate.isEmpty()
            )

            // ── 教育/职业 ──
            SectionTitle("教育与职业")

            // 学历下拉
            ExposedDropdownMenuBox(
                expanded = educationExpanded,
                onExpandedChange = { educationExpanded = !educationExpanded }
            ) {
                OutlinedTextField(
                    value = education,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("受教育水平") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(educationExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    placeholder = { Text("请选择") }
                )
                ExposedDropdownMenu(educationExpanded, { educationExpanded = false }) {
                    EDUCATION_OPTIONS.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(if (opt.isEmpty()) "不填写" else opt) },
                            onClick = { education = opt; educationExpanded = false }
                        )
                    }
                }
            }

            // 职业下拉（含手动输入）
            ExposedDropdownMenuBox(
                expanded = occupExpanded,
                onExpandedChange = { occupExpanded = !occupExpanded }
            ) {
                OutlinedTextField(
                    value = occupation,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("职业") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(occupExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    placeholder = { Text("请选择") }
                )
                ExposedDropdownMenu(occupExpanded, { occupExpanded = false }) {
                    OCCUPATION_OPTIONS.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(if (opt.isEmpty()) "不填写" else opt) },
                            onClick = { occupation = opt; occupExpanded = false }
                        )
                    }
                }
            }
            // 职业"其他"时可手动输入
            if (isCustomOccup) {
                OutlinedTextField(
                    value = occupCustom,
                    onValueChange = { occupCustom = it },
                    label = { Text("请填写具体职业") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // ── 联系方式 ──
            SectionTitle("联系方式")

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("电话") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("地址（如：2号楼1单元301）") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )

            // ── 备注 ──
            SectionTitle("备注")

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("备注") },
                placeholder = { Text("其他信息，导入时未能识别的字段会自动存入此处") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 5
            )

            // ── 自定义字段 ──
            if (customFields.isNotEmpty()) {
                SectionTitle("自定义字段")
                customFields.keys.toList().forEach { key ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customFields[key] ?: "",
                            onValueChange = { customFields = (customFields + (key to it)).toMutableMap() },
                            label = { Text(key) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(onClick = {
                            customFields = customFields.toMutableMap().also { it.remove(key) }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "删除字段",
                                tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = { showAddFieldDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("添加自定义字段")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── 保存按钮 ──
            Button(
                onClick = {
                    val finalAge = if (birthDate.isNotEmpty()) calcAge(birthDate)
                                   else age.toIntOrNull() ?: 0
                    val finalOccupation = if (isCustomOccup && occupCustom.isNotBlank())
                        occupCustom else if (occupation == "不填写") "" else occupation

                    val newResident = if (resident == null) {
                        Resident(
                            name = name, gender = gender, birthDate = birthDate,
                            age = finalAge, education = education,
                            occupation = finalOccupation, phone = phone,
                            address = address, notes = notes,
                            customFields = customFields
                        )
                    } else {
                        resident.copy(
                            name = name, gender = gender, birthDate = birthDate,
                            age = finalAge, education = education,
                            occupation = finalOccupation, phone = phone,
                            address = address, notes = notes,
                            customFields = customFields,
                            updatedAt = System.currentTimeMillis()
                        )
                    }
                    if (resident == null) viewModel.insertResident(newResident)
                    else viewModel.updateResident(newResident)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("保存")
                }
            }

            if (name.isBlank()) {
                Text("姓名为必填项", color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 4.dp)
    )
}
