package com.resident.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resident.app.data.entity.Memo
import com.resident.app.ui.viewmodel.MemoViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoListScreen(
    viewModel: MemoViewModel,
    onAddMemo: () -> Unit
) {
    val memos by viewModel.memos.collectAsState()
    val message by viewModel.message.collectAsState()

    // 显示消息提示
    LaunchedEffect(message) {
        message?.let { viewModel.clearMessage() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("工作备忘录", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B5E20),
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMemo,
                containerColor = Color(0xFF1B5E20),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加备忘")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F7FA))
        ) {
            // 即时提醒横幅
            val immediateMemos by viewModel.immediateMemos.collectAsState()
            if (immediateMemos.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "您有 ${immediateMemos.size} 条即时提醒待处理",
                            color = Color(0xFFE65100),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 备忘列表
            if (memos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无备忘录\n点击右下角 + 添加",
                        color = Color(0xFF9E9E9E),
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(memos) { memo ->
                        MemoItem(
                            memo = memo,
                            onClick = { /* TODO: 编辑功能 */ },
                            onDelete = { viewModel.deleteMemo(memo) },
                            onToggleComplete = { viewModel.toggleMemoComplete(memo) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MemoItem(
    memo: Memo,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onToggleComplete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (memo.isCompleted) Color(0xFFE8F5E9) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 完成状态图标
                    Icon(
                        if (memo.isCompleted)
                            Icons.Default.CheckCircle
                        else
                            Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (memo.isCompleted) Color(0xFF4CAF50) else Color(0xFFBDBDBD),
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(onClick = onToggleComplete)
                    )
                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = memo.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (memo.isCompleted) Color(0xFF757575) else Color(0xFF212121),
                            textDecoration = if (memo.isCompleted) TextDecoration.LineThrough else null,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // 删除按钮
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = Color(0xFFEF5350)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = memo.content,
                fontSize = 14.sp,
                color = if (memo.isCompleted) Color(0xFF9E9E9E) else Color(0xFF616161),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )

            // 提醒信息
            if (memo.isImmediate || memo.remindTime > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color(0xFF1B5E20),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when {
                            memo.isImmediate -> "打开应用时提醒"
                            memo.remindTime > 0 -> {
                                val date = Date(memo.remindTime)
                                val format = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                                format.format(date)
                            }
                            else -> ""
                        },
                        fontSize = 12.sp,
                        color = Color(0xFF1B5E20)
                    )
                }
            }
        }
    }
}
