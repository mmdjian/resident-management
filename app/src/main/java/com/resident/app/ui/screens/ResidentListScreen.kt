package com.resident.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.resident.app.data.entity.Resident
import com.resident.app.ui.viewmodel.ResidentViewModel

// ───────────────────── 主入口（含底部导航） ─────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentListScreen(
    viewModel: ResidentViewModel,
    onAddClick: () -> Unit,
    onEditClick: (Resident) -> Unit,
    onStatisticsClick: () -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(1) }  // 默认居民Tab

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.GridView, contentDescription = null) },
                    label = { Text("工作") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.People, contentDescription = null) },
                    label = { Text("居民") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("我的") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> WorkScreen()
                1 -> ResidentTab(
                    viewModel = viewModel,
                    onAddClick = onAddClick,
                    onEditClick = onEditClick,
                    onStatisticsClick = onStatisticsClick,
                    onExportClick = onExportClick,
                    onImportClick = onImportClick
                )
                2 -> ProfileScreen()
            }
        }
    }
}

// ───────────────────── 居民 Tab ─────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentTab(
    viewModel: ResidentViewModel,
    onAddClick: () -> Unit,
    onEditClick: (Resident) -> Unit,
    onStatisticsClick: () -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit
) {
    val residents by viewModel.residents.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchMode by viewModel.searchMode.collectAsState()
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
                    IconButton(onClick = onStatisticsClick) {
                        Icon(Icons.Default.BarChart, contentDescription = "统计")
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "更多")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("导入数据") },
                                onClick = { showMenu = false; onImportClick() }
                            )
                            DropdownMenuItem(
                                text = { Text("导出数据") },
                                onClick = { showMenu = false; onExportClick() }
                            )
                            DropdownMenuItem(
                                text = { Text("修改密码") },
                                onClick = { showMenu = false; showChangePwd = true }
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
            // ── 搜索栏 + 模式切换 ──
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    placeholder = {
                        Text(if (searchMode == "address") "按居住单元搜索 (如: 1-0-0 表示1号楼全部, 0-0-304表示所有楼304户)..." else "输入姓名、电话、地址等任意信息搜索...")
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "清空")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(6.dp))
                // 搜索模式切换芯片
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SearchModeChip(
                        label = "全字段",
                        selected = searchMode == "all",
                        onClick = { viewModel.setSearchMode("all") }
                    )
                    SearchModeChip(
                        label = "按姓名",
                        selected = searchMode == "name",
                        onClick = { viewModel.setSearchMode("name") }
                    )
                    SearchModeChip(
                        label = "按居住单元",
                        selected = searchMode == "address",
                        onClick = { viewModel.setSearchMode("address") }
                    )
                }
            }

            // ── 统计数字条 ──
            if (residents.isNotEmpty()) {
                Text(
                    text = "共 ${residents.size} 位居民",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF888888),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )
            }

            // ── 列表 ──
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (residents.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.PeopleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFFCCCCCC)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (searchQuery.isEmpty()) "暂无居民数据\n点击右下角 + 添加"
                            else "未找到匹配的居民",
                            color = Color(0xFFAAAAAA),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
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

// ───────────────────── 搜索模式芯片 ─────────────────────

@Composable
fun SearchModeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFF2E7D32),
            selectedLabelColor = Color.White
        )
    )
}

// ───────────────────── 居民卡片 ─────────────────────

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = resident.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (resident.gender.isNotEmpty()) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = if (resident.gender == "男") Color(0xFFE3F2FD)
                                    else Color(0xFFFCE4EC)
                        ) {
                            Text(
                                resident.gender,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (resident.gender == "男") Color(0xFF1565C0)
                                        else Color(0xFFC62828)
                            )
                        }
                    }
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "删除",
                        tint = Color(0xFFCCCCCC))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))

            if (resident.birthDate.isNotEmpty()) {
                Text("${resident.birthDate}  ·  ${resident.age}岁",
                    style = MaterialTheme.typography.bodySmall, color = Color(0xFF666666))
            } else if (resident.age > 0) {
                Text("${resident.age}岁",
                    style = MaterialTheme.typography.bodySmall, color = Color(0xFF666666))
            }
            if (resident.phone.isNotEmpty()) {
                Text("📞 ${resident.phone}",
                    style = MaterialTheme.typography.bodySmall, color = Color(0xFF666666))
            }
            if (resident.address.isNotEmpty()) {
                Text("📍 ${resident.address}",
                    style = MaterialTheme.typography.bodySmall, color = Color(0xFF666666))
            }
            if (resident.education.isNotEmpty()) {
                Text("学历: ${resident.education}",
                    style = MaterialTheme.typography.bodySmall, color = Color(0xFF888888))
            }
            if (resident.occupation.isNotEmpty()) {
                Text("政治面貌: ${resident.occupation}",
                    style = MaterialTheme.typography.bodySmall, color = Color(0xFF888888))
            }
            if (resident.notes.isNotEmpty()) {
                Text("备注: ${resident.notes}",
                    style = MaterialTheme.typography.bodySmall, color = Color(0xFF888888))
            }
            resident.customFields.forEach { (key, value) ->
                if (value.isNotEmpty()) Text("$key: $value",
                    style = MaterialTheme.typography.bodySmall, color = Color(0xFF888888))
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除 ${resident.name} 吗？") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("取消") }
            }
        )
    }
}
