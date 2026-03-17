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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditResidentScreen(
    viewModel: ResidentViewModel,
    resident: Resident?,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(resident?.name ?: "") }
    var gender by remember { mutableStateOf(resident?.gender ?: "") }
    var birthDate by remember { mutableStateOf(resident?.birthDate ?: "") }
    var age by remember { mutableStateOf(
        if (resident?.birthDate?.isNotEmpty() == true) ""
        else resident?.age?.takeIf { it > 0 }?.toString() ?: ""
    ) }
    var education by remember { mutableStateOf(resident?.education ?: "") }
    var occupation by remember { mutableStateOf(resident?.occupation ?: "") }
    var phone by remember { mutableStateOf(resident?.phone ?: "") }
    var address by remember { mutableStateOf(resident?.address ?: "") }
    var customFields by remember { mutableStateOf(
        resident?.customFields?.toMutableMap() ?: mutableMapOf()
    ) }

    var genderExpanded by remember { mutableStateOf(false) }
    var educationExpanded by remember { mutableStateOf(false) }
    var showAddFieldDialog by remember { mutableStateOf(false) }
    var newFieldName by remember { mutableStateOf("") }

    val genderOptions = listOf("男", "女")
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val context = LocalContext.current

    fun calcAge(dateStr: String): Int {
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val birth = LocalDate.parse(dateStr, formatter)
            ChronoUnit.YEARS.between(birth, LocalDate.now()).toInt()
        } catch (e: Exception) { 0 }
    }

    val displayAge = if (birthDate.isNotEmpty()) {
        val a = calcAge(birthDate); if (a > 0) "$a 岁" else ""
    } else age

    LaunchedEffect(message) {
        message?.let {
            if (it.contains("成功")) onBack()
            viewModel.clearMessage()
        }
    }

    // 添加自定义字段对话框
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
                    showAddFieldDialog = false
                    newFieldName = ""
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
                title = { Text(if (resident == null) "添加居民" else "编辑居民") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    placeholder = { Text("请选择") }
                )
                ExposedDropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                    genderOptions.forEach { option ->
                        DropdownMenuItem(text = { Text(option) }, onClick = {
                            gender = option; genderExpanded = false
                        })
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
                        val initYear = if (birthDate.isNotEmpty()) try { birthDate.substring(0,4).toInt() } catch (e: Exception) { today.year - 30 } else today.year - 30
                        val initMonth = if (birthDate.length >= 7) try { birthDate.substring(5,7).toInt() - 1 } catch (e: Exception) { 0 } else 0
                        val initDay = if (birthDate.length == 10) try { birthDate.substring(8,10).toInt() } catch (e: Exception) { 1 } else 1
                        DatePickerDialog(context, { _, year, month, day ->
                            birthDate = "%04d-%02d-%02d".format(year, month + 1, day)
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
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = educationExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    placeholder = { Text("请选择") }
                )
                ExposedDropdownMenu(expanded = educationExpanded, onDismissRequest = { educationExpanded = false }) {
                    EDUCATION_OPTIONS.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(if (option.isEmpty()) "不填写" else option) },
                            onClick = { education = option; educationExpanded = false }
                        )
                    }
                }
            }

            // 职业
            OutlinedTextField(
                value = occupation,
                onValueChange = { occupation = it },
                label = { Text("职业") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 电话
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("电话") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 地址
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("地址") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )

            // 自定义字段
            if (customFields.isNotEmpty()) {
                Text("自定义字段", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
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

            // 添加自定义字段按钮
            OutlinedButton(
                onClick = { showAddFieldDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("添加自定义字段")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 保存按钮
            Button(
                onClick = {
                    val finalAge = if (birthDate.isNotEmpty()) calcAge(birthDate)
                                   else age.toIntOrNull() ?: 0
                    val newResident = if (resident == null) {
                        Resident(
                            name = name, gender = gender, birthDate = birthDate,
                            age = finalAge, education = education,
                            occupation = occupation, phone = phone, address = address,
                            customFields = customFields
                        )
                    } else {
                        resident.copy(
                            name = name, gender = gender, birthDate = birthDate,
                            age = finalAge, education = education,
                            occupation = occupation, phone = phone, address = address,
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
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
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
