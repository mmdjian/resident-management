package com.resident.app.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.resident.app.data.entity.Resident
import com.resident.app.ui.viewmodel.ResidentViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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
    var occupation by remember { mutableStateOf(resident?.occupation ?: "") }
    var phone by remember { mutableStateOf(resident?.phone ?: "") }
    var address by remember { mutableStateOf(resident?.address ?: "") }

    var genderExpanded by remember { mutableStateOf(false) }
    val genderOptions = listOf("男", "女")

    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()

    val context = LocalContext.current

    fun calcAge(dateStr: String): Int {
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val birth = LocalDate.parse(dateStr, formatter)
            ChronoUnit.YEARS.between(birth, LocalDate.now()).toInt()
        } catch (e: Exception) {
            0
        }
    }

    val displayAge = if (birthDate.isNotEmpty()) {
        val a = calcAge(birthDate)
        if (a > 0) "$a 岁" else ""
    } else {
        age
    }

    LaunchedEffect(message) {
        message?.let {
            if (it.contains("成功")) {
                onBack()
            }
            viewModel.clearMessage()
        }
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("姓名") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            ExposedDropdownMenuBox(
                expanded = genderExpanded,
                onExpandedChange = { genderExpanded = !genderExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = gender,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("性别") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    placeholder = { Text("请选择") }
                )
                ExposedDropdownMenu(
                    expanded = genderExpanded,
                    onDismissRequest = { genderExpanded = false }
                ) {
                    genderOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                gender = option
                                genderExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = birthDate,
                onValueChange = {},
                readOnly = true,
                label = { Text("出生年月日") },
                placeholder = { Text("点击选择日期") },
                trailingIcon = {
                    IconButton(onClick = {
                        val today = LocalDate.now()
                        val initYear = if (birthDate.isNotEmpty()) {
                            try { birthDate.substring(0, 4).toInt() } catch (e: Exception) { today.year - 30 }
                        } else today.year - 30
                        val initMonth = if (birthDate.length >= 7) {
                            try { birthDate.substring(5, 7).toInt() - 1 } catch (e: Exception) { 0 }
                        } else 0
                        val initDay = if (birthDate.length == 10) {
                            try { birthDate.substring(8, 10).toInt() } catch (e: Exception) { 1 }
                        } else 1

                        DatePickerDialog(context, { _, year, month, day ->
                            birthDate = "%04d-%02d-%02d".format(year, month + 1, day)
                            age = ""
                        }, initYear, initMonth, initDay).show()
                    }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "选择日期")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = displayAge,
                onValueChange = {
                    if (birthDate.isEmpty()) {
                        age = it.filter { c -> c.isDigit() }
                    }
                },
                label = { Text(if (birthDate.isNotEmpty()) "年龄（自动计算）" else "年龄") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                readOnly = birthDate.isNotEmpty(),
                enabled = birthDate.isEmpty()
            )

            OutlinedTextField(
                value = occupation,
                onValueChange = { occupation = it },
                label = { Text("职业") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

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
                label = { Text("地址") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val finalAge = if (birthDate.isNotEmpty()) calcAge(birthDate)
                                   else age.toIntOrNull() ?: 0
                    val newResident = if (resident == null) {
                        Resident(
                            name = name,
                            gender = gender,
                            birthDate = birthDate,
                            age = finalAge,
                            occupation = occupation,
                            phone = phone,
                            address = address
                        )
                    } else {
                        resident.copy(
                            name = name,
                            gender = gender,
                            birthDate = birthDate,
                            age = finalAge,
                            occupation = occupation,
                            phone = phone,
                            address = address,
                            updatedAt = System.currentTimeMillis()
                        )
                    }

                    if (resident == null) {
                        viewModel.insertResident(newResident)
                    } else {
                        viewModel.updateResident(newResident)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("保存")
                }
            }

            if (name.isBlank()) {
                Text(
                    text = "姓名为必填项",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
