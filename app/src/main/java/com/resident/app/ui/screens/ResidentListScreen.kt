package com.resident.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.resident.app.data.entity.Resident
import com.resident.app.ui.viewmodel.ResidentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentListScreen(
    viewModel: ResidentViewModel,
    onAddClick: () -> Unit,
    onEditClick: (Resident) -> Unit,
    onStatisticsClick: () -> Unit,
    onExportClick: () -> Unit
) {
    val residents by viewModel.residents.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showChangePwd by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        message?.let { viewModel.clearMessage() }
    }

    if (showChangePwd) {
        ChangePasswordDialog(onDismiss = { showChangePwd = false })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("润泽知园居民信息管理") },
                actions = {
                    // 统计按钮
                    IconButton(onClick = onStatisticsClick) {
                        Icon(Icons.Default.BarChart, contentDescription = "统计")
                    }
                    // 更多菜单
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "更多")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("导出数据") },
                                onClick = {
                                    showMenu = false
                                    onExportClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("修改密码") },
                                onClick = {
                                    showMenu = false
                                    showChangePwd = true
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "添加居民")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = { Text("搜索居民...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            } else if (residents.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { Text("暂无数据") }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(residents) { resident ->
                        ResidentItem(
                            resident = resident,
                            onClick = { onEditClick(resident) },
                            onDelete = { viewModel.deleteResident(resident) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ResidentItem(
    resident: Resident,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = resident.name,
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "删除")
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("性别: ${resident.gender.ifEmpty { "未填写" }}")
            if (resident.birthDate.isNotEmpty()) {
                Text("出生日期: ${resident.birthDate}  年龄: ${resident.age}岁")
            } else if (resident.age > 0) {
                Text("年龄: ${resident.age}岁")
            }
            if (resident.education.isNotEmpty()) Text("学历: ${resident.education}")
            if (resident.occupation.isNotEmpty()) Text("职业: ${resident.occupation}")
            if (resident.phone.isNotEmpty()) Text("电话: ${resident.phone}")
            if (resident.address.isNotEmpty()) Text("地址: ${resident.address}")
            // 显示自定义字段
            resident.customFields.forEach { (key, value) ->
                if (value.isNotEmpty()) Text("$key: $value")
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除${resident.name}吗？") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("取消") }
            }
        )
    }
}
