package com.resident.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.resident.app.ui.viewmodel.MemoViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMemoScreen(
    viewModel: MemoViewModel,
    onBack: () -> Unit,
    memoId: Long? = null
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isImmediate by remember { mutableStateOf(false) }
    var remindTime by remember { mutableStateOf(0L) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val scope = rememberCoroutineScope()

    // 加载现有备忘数据
    LaunchedEffect(memoId) {
        if (memoId != null) {
            viewModel.getMemoById(memoId)?.let { memo ->
                title = memo.title
                content = memo.content
                isImmediate = memo.isImmediate
                remindTime = memo.remindTime
            }
        }
    }

    // 显示消息提示
    LaunchedEffect(message) {
        if (message != null) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearMessage()
        }
    }

    // 检查是否需要显示即时提醒
    val immediateMemos by viewModel.immediateMemos.collectAsState()

    // 显示即时提醒
    if (immediateMemos.isNotEmpty()) {
        LaunchedEffect(Unit) {
            // 可以在这里添加通知或弹窗提醒
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (memoId != null) "编辑备忘" else "添加备忘", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B5E20),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                actions = {
                    IconButton(
                        onClick = {
                            if (title.isNotBlank() && content.isNotBlank()) {
                                viewModel.insertMemo(title, content, isImmediate, remindTime)
                                scope.launch {
                                    kotlinx.coroutines.delay(500)
                                    onBack()
                                }
                            }
                        },
                        enabled = title.isNotBlank() && content.isNotBlank() && !isLoading
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "保存")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F7FA))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题输入
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("备忘标题", color = Color(0xFF9E9E9E)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent
                    ),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleMedium
                )
            }

            // 内容输入
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                TextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("备忘内容", color = Color(0xFF9E9E9E)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    minLines = 5,
                    maxLines = 10
                )
            }

            // 提醒设置
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "提醒设置",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 即时提醒开关
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "即时提醒",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "打开应用时立即提醒",
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
                            )
                        }
                        Switch(
                            checked = isImmediate,
                            onCheckedChange = {
                                isImmediate = it
                                if (it) remindTime = 0L
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF1B5E20),
                                checkedTrackColor = Color(0xFFC8E6C9),
                                uncheckedThumbColor = Color(0xFFBDBDBD),
                                uncheckedTrackColor = Color(0xFFE0E0E0)
                            )
                        )
                    }

                    if (isImmediate) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // 延迟提醒
                    if (!isImmediate) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (remindTime > 0) {
                                        val date = Date(remindTime)
                                        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                        format.format(date)
                                    } else "设置提醒时间",
                                    fontSize = 14.sp,
                                    fontWeight = if (remindTime > 0) FontWeight.Medium else FontWeight.Normal,
                                    color = if (remindTime > 0) Color(0xFF1B5E20) else Color(0xFF616161)
                                )
                                Text(
                                    text = if (remindTime > 0) "已设置" else "点击设置具体提醒时间",
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575)
                                )
                            }
                            IconButton(
                                onClick = {
                                    showDatePicker = true
                                }
                            ) {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = "设置提醒时间",
                                    tint = Color(0xFF1B5E20)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 提示信息
            if (title.isBlank() || content.isBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "请填写标题和内容",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp,
                        color = Color(0xFFE65100)
                    )
                }
            }
        }

        // 日期选择器
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDateSelected = {
                    if (it != null) {
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = it
                        calendar.set(Calendar.HOUR_OF_DAY, 9)
                        calendar.set(Calendar.MINUTE, 0)
                        remindTime = calendar.timeInMillis
                    }
                    showDatePicker = false
                },
                onDismissRequest = { showDatePicker = false },
                datePickerState = datePickerState
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDateSelected: (Long?) -> Unit,
    onDismissRequest: () -> Unit,
    datePickerState: DatePickerState
) {
    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("取消")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
