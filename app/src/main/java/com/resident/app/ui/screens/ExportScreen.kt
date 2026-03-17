package com.resident.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.resident.app.ui.viewmodel.ResidentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    viewModel: ResidentViewModel,
    onBack: () -> Unit
) {
    var genderFilter by remember { mutableStateOf("") }
    var educationFilter by remember { mutableStateOf("") }
    var customKey by remember { mutableStateOf("") }
    var customValue by remember { mutableStateOf("") }

    var genderExpanded by remember { mutableStateOf(false) }
    var educationExpanded by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    var resultMsg by remember { mutableStateOf("") }

    LaunchedEffect(message) {
        message?.let {
            resultMsg = it
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("筛选导出") },
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
            Text("选择筛选条件（不选则导出全部）",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

            // 性别筛选
            ExposedDropdownMenuBox(
                expanded = genderExpanded,
                onExpandedChange = { genderExpanded = !genderExpanded }
            ) {
                OutlinedTextField(
                    value = if (genderFilter.isEmpty()) "全部" else genderFilter,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("性别") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                    listOf("" to "全部", "男" to "男", "女" to "女").forEach { (value, label) ->
                        DropdownMenuItem(text = { Text(label) }, onClick = {
                            genderFilter = value; genderExpanded = false
                        })
                    }
                }
            }

            // 学历筛选
            ExposedDropdownMenuBox(
                expanded = educationExpanded,
                onExpandedChange = { educationExpanded = !educationExpanded }
            ) {
                OutlinedTextField(
                    value = if (educationFilter.isEmpty()) "全部" else educationFilter,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("受教育水平") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = educationExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = educationExpanded, onDismissRequest = { educationExpanded = false }) {
                    (listOf("" to "全部") + EDUCATION_OPTIONS.filter { it.isNotEmpty() }.map { it to it })
                        .forEach { (value, label) ->
                            DropdownMenuItem(text = { Text(label) }, onClick = {
                                educationFilter = value; educationExpanded = false
                            })
                        }
                }
            }

            // 自定义字段筛选
            Text("自定义字段筛选（可选）",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = customKey,
                    onValueChange = { customKey = it },
                    label = { Text("字段名，如：是否党员") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = customValue,
                    onValueChange = { customValue = it },
                    label = { Text("值，如：是") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Divider()

            // 导出按钮
            Button(
                onClick = {
                    resultMsg = ""
                    val customFilter = if (customKey.isNotBlank() && customValue.isNotBlank())
                        customKey to customValue else null
                    viewModel.exportFiltered(genderFilter, educationFilter, customFilter)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Icon(Icons.Default.FileDownload, contentDescription = null,
                        modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("导出 Excel")
                }
            }

            if (resultMsg.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = resultMsg,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Text(
                "导出文件保存在手机「下载」文件夹中",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
