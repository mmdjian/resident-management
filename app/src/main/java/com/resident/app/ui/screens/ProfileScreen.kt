package com.resident.app.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import java.io.File

// 用 SharedPreferences 持久化个人信息
private fun getProfilePrefs(context: Context) =
    context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val prefs = remember { getProfilePrefs(context) }

    var name by remember { mutableStateOf(prefs.getString("name", "") ?: "") }
    var phone by remember { mutableStateOf(prefs.getString("phone", "") ?: "") }
    var area by remember { mutableStateOf(prefs.getString("area", "") ?: "") }
    var avatarPath by remember { mutableStateOf(prefs.getString("avatar", "") ?: "") }
    var isEditing by remember { mutableStateOf(false) }
    var showSaved by remember { mutableStateOf(false) }

    // 头像选择器
    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            // 把图片复制到 App 内部存储，避免权限失效
            try {
                val destFile = File(context.filesDir, "avatar.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output -> input.copyTo(output) }
                }
                avatarPath = destFile.absolutePath
                prefs.edit().putString("avatar", avatarPath).apply()
            } catch (e: Exception) { /* 忽略 */ }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .verticalScroll(rememberScrollState())
    ) {
        // 顶部绿色横幅 + 头像
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(Color(0xFF2E7D32))
        ) {
            // 编辑按钮
            IconButton(
                onClick = {
                    if (isEditing) {
                        // 保存
                        prefs.edit()
                            .putString("name", name)
                            .putString("phone", phone)
                            .putString("area", area)
                            .apply()
                        showSaved = true
                    }
                    isEditing = !isEditing
                },
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
            ) {
                Icon(
                    if (isEditing) Icons.Default.Save else Icons.Default.Edit,
                    contentDescription = if (isEditing) "保存" else "编辑",
                    tint = Color.White
                )
            }

            // 头像
            Box(
                modifier = Modifier.align(Alignment.BottomCenter).offset(y = 44.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .border(3.dp, Color.White, CircleShape)
                        .background(Color(0xFFE8F5E9))
                        .clickable(enabled = isEditing) { avatarPicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarPath.isNotEmpty() && File(avatarPath).exists()) {
                        Image(
                            painter = rememberAsyncImagePainter(model = File(avatarPath)),
                            contentDescription = "头像",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF2E7D32)
                        )
                    }
                }
                if (isEditing) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1B5E20))
                            .clickable { avatarPicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "更换头像",
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(52.dp))

        // 姓名展示
        Text(
            text = name.ifEmpty { "点击右上角编辑" },
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = if (name.isEmpty()) Color(0xFFAAAAAA) else Color(0xFF1A1A1A),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 信息编辑区
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("基本信息", fontWeight = FontWeight.Bold, fontSize = 15.sp,
                    color = Color(0xFF2E7D32))

                ProfileField(
                    icon = { Icon(Icons.Default.Person, null, tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(20.dp)) },
                    label = "姓名",
                    value = name,
                    isEditing = isEditing,
                    onValueChange = { name = it }
                )

                HorizontalDivider(color = Color(0xFFEEEEEE))

                ProfileField(
                    icon = { Icon(Icons.Default.Phone, null, tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(20.dp)) },
                    label = "联系电话",
                    value = phone,
                    isEditing = isEditing,
                    onValueChange = { phone = it },
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                )

                HorizontalDivider(color = Color(0xFFEEEEEE))

                ProfileField(
                    icon = { Icon(Icons.Default.LocationOn, null, tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(20.dp)) },
                    label = "负责区域",
                    value = area,
                    isEditing = isEditing,
                    onValueChange = { area = it },
                    placeholder = "如：润泽知园1-3号楼"
                )
            }
        }

        // 保存成功提示
        if (showSaved) {
            LaunchedEffect(showSaved) {
                kotlinx.coroutines.delay(2000)
                showSaved = false
            }
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
            ) {
                Text(
                    "信息已保存",
                    modifier = Modifier.padding(12.dp),
                    color = Color(0xFF2E7D32),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 版本信息
        Text(
            "居民信息管理系统 v2.0",
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            fontSize = 12.sp,
            color = Color(0xFFAAAAAA)
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileField(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardType: androidx.compose.ui.text.input.KeyboardType =
        androidx.compose.ui.text.input.KeyboardType.Text
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        icon()
        Spacer(modifier = Modifier.width(12.dp))
        if (isEditing) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label) },
                placeholder = { Text(placeholder.ifEmpty { "请输入$label" }) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType)
            )
        } else {
            Column {
                Text(label, fontSize = 12.sp, color = Color(0xFF888888))
                Text(
                    value.ifEmpty { "未填写" },
                    fontSize = 15.sp,
                    color = if (value.isEmpty()) Color(0xFFCCCCCC) else Color(0xFF1A1A1A)
                )
            }
        }
    }
}
