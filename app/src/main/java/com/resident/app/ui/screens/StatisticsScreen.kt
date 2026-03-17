package com.resident.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resident.app.ui.viewmodel.ResidentViewModel
import com.resident.app.ui.viewmodel.StatisticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    residentViewModel: ResidentViewModel,
    onBack: () -> Unit,
    onExportClick: () -> Unit,
    onFilterClick: (String) -> Unit = {}
) {
    val statistics by viewModel.statistics.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val lastBackupDays by viewModel.lastBackupDays.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadStatistics()
        viewModel.loadBackupInfo(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("数据统计") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onExportClick) {
                        Icon(Icons.Default.FileDownload, contentDescription = "导出")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading || statistics == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 备份提醒
                val warnColor = if (lastBackupDays >= 7) Color(0xFFE65100) else Color(0xFF2E7D32)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (lastBackupDays >= 7) Color(0xFFFFF3E0) else Color(0xFFF1F8E9)
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (lastBackupDays >= 7) Icons.Default.Warning else Icons.Default.Backup,
                                contentDescription = null,
                                tint = warnColor,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    if (lastBackupDays == 0L) "今日已备份" else "距上次备份 $lastBackupDays 天",
                                    fontWeight = FontWeight.Bold,
                                    color = warnColor
                                )
                                if (lastBackupDays >= 7) {
                                    Text("建议及时备份数据", style = MaterialTheme.typography.bodySmall, color = warnColor)
                                }
                            }
                        }
                        Button(
                            onClick = { viewModel.doBackup(context, residentViewModel) },
                            colors = ButtonDefaults.buttonColors(containerColor = warnColor)
                        ) { Text("立即备份", fontSize = 13.sp) }
                    }
                }

                // 基础统计
                Text("基础统计", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ClickableStatCard(
                        title = "总居民",
                        value = statistics!!.totalCount.toString(),
                        modifier = Modifier.weight(1f),
                        onClick = { onFilterClick("all") }
                    )
                    ClickableStatCard(
                        title = "男性",
                        value = statistics!!.maleCount.toString(),
                        modifier = Modifier.weight(1f),
                        onClick = { if (statistics!!.maleCount > 0) onFilterClick("gender:男") }
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ClickableStatCard(
                        title = "女性",
                        value = statistics!!.femaleCount.toString(),
                        modifier = Modifier.weight(1f),
                        onClick = { if (statistics!!.femaleCount > 0) onFilterClick("gender:女") }
                    )
                }

                // 学历统计
                if (statistics!!.educationCounts.any { it.value > 0 }) {
                    Text("学历分布", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    statistics!!.educationCounts.forEach { (edu, count) ->
                        if (count > 0) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onFilterClick("education:$edu") }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(edu, style = MaterialTheme.typography.bodyLarge)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("$count 人", style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            Icons.Default.ChevronRight,
                                            contentDescription = "查看",
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ClickableStatCard(title: String, value: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable(enabled = value != "0") { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (value == "0") MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                if (value != "0") {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "查看",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
