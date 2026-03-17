package com.resident.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.resident.app.ui.viewmodel.ResidentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(
    viewModel: ResidentViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedFileName by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var resultMsg by remember { mutableStateOf("") }
    var resultIsError by remember { mutableStateOf(false) }
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()

    LaunchedEffect(message) {
        message?.let {
            resultMsg = it
            resultIsError = it.contains("失败") || it.contains("错误")
            viewModel.clearMessage()
        }
    }

    // 文件选择器（支持 .xls 和 .xlsx）
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            // 获取文件名
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) selectedFileName = it.getString(nameIndex)
                }
            }
            resultMsg = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("导入 Excel") },
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
            // 说明卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("使用说明", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1565C0))
                    Text("• 支持 .xls 格式的 Excel 文件", style = MaterialTheme.typography.bodySmall)
                    Text("• 第一行必须是表头，支持以下列名：", style = MaterialTheme.typography.bodySmall)
                    Text("  姓名、性别、出生年月日、年龄、受教育水平、职业、电话、地址",
                        style = MaterialTheme.typography.bodySmall, color = Color(0xFF1565C0))
                    Text("• 姓名为必填项，其余列可选", style = MaterialTheme.typography.bodySmall)
                    Text("• 不认识的列名会作为自定义字段导入", style = MaterialTheme.typography.bodySmall)
                    Text("• 重复导入同名居民会新增而不覆盖", style = MaterialTheme.typography.bodySmall)
                }
            }

            // 选择文件按钮
            OutlinedButton(
                onClick = { filePicker.launch("application/vnd.ms-excel") },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (selectedFileName.isEmpty()) "选择 Excel 文件" else "已选择：$selectedFileName")
            }

            // 如果选了文件，显示导入按钮
            if (selectedUri != null) {
                Button(
                    onClick = {
                        resultMsg = ""
                        viewModel.importFromExcel(context, selectedUri!!)
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(Icons.Default.Upload, contentDescription = null,
                            modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("开始导入")
                    }
                }
            }

            // 结果展示
            if (resultMsg.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (resultIsError) Color(0xFFFFEBEE) else Color(0xFFF1F8E9)
                    )
                ) {
                    Text(
                        text = resultMsg,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (resultIsError) Color(0xFFC62828) else Color(0xFF2E7D32)
                    )
                }
            }
        }
    }
}
