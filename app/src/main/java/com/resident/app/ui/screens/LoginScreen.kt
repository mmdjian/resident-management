package com.resident.app.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.resident.app.data.security.BiometricAuthManager

// 密码管理工具类，使用 SharedPreferences 持久化
object AppPassword {
    private const val PREFS_NAME = "app_password_prefs"
    private const val KEY_PASSWORD = "password"
    private const val DEFAULT_PASSWORD = "123456"

    fun getPassword(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_PASSWORD, DEFAULT_PASSWORD) ?: DEFAULT_PASSWORD
    }

    fun setPassword(context: Context, newPassword: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PASSWORD, newPassword).apply()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var input by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    var isAuthenticating by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val activity = context as FragmentActivity
    val scope = rememberCoroutineScope()

    // 检查是否启用指纹
    val biometricEnabled = remember { BiometricAuthManager.isBiometricEnabled(context) }
    val biometricAvailable = remember { BiometricAuthManager.isBiometricAvailable(context) }

    LaunchedEffect(Unit) {
        if (biometricEnabled && biometricAvailable) {
            focusRequester.requestFocus()
        } else {
            focusRequester.requestFocus()
        }
    }

    fun tryLogin() {
        if (input == AppPassword.getPassword(context)) {
            keyboard?.hide()
            onLoginSuccess()
        } else {
            errorMsg = "密码错误，请重试"
            input = ""
        }
    }

    fun tryBiometricLogin() {
        if (!biometricAvailable || !biometricEnabled) return

        scope.launch {
            isAuthenticating = true
            val success = BiometricAuthManager.authenticate(
                activity = activity,
                title = "指纹验证",
                subtitle = "请验证指纹以登录"
            )
            isAuthenticating = false
            if (success) {
                errorMsg = ""
                keyboard?.hide()
                onLoginSuccess()
            } else {
                errorMsg = ""
                // 指纹验证失败，不显示错误，让用户选择密码登录
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1565C0),
                        Color(0xFF1E88E5),
                        Color(0xFF42A5F5)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo 区域
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "润泽知园",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    letterSpacing = 4.sp
                ),
                color = Color.White
            )
            Text(
                text = "居民信息管理系统",
                style = MaterialTheme.typography.titleMedium.copy(
                    letterSpacing = 2.sp
                ),
                color = Color.White.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 登录卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "请输入访问密码",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFF424242)
                    )

                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it; errorMsg = "" },
                        label = { Text("密码") },
                        visualTransformation = if (showPassword) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff
                                                  else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { tryLogin() }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        singleLine = true,
                        isError = errorMsg.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (errorMsg.isNotEmpty()) {
                        Text(
                            text = errorMsg,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Button(
                        onClick = { tryLogin() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = input.isNotEmpty() && !isAuthenticating,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1565C0)
                        )
                    ) {
                        Text(
                            "进入系统",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // 指纹登录按钮
                    if (biometricAvailable && biometricEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { tryBiometricLogin() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = !isAuthenticating,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF1565C0)
                            )
                        ) {
                            Icon(
                                Icons.Default.Fingerprint,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (isAuthenticating) "验证中..." else "指纹登录",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

// 修改密码界面
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordDialog(onDismiss: () -> Unit) {
    var oldPwd by remember { mutableStateOf("") }
    var newPwd by remember { mutableStateOf("") }
    var confirmPwd by remember { mutableStateOf("") }
    var showOld by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    var successMsg by remember { mutableStateOf("") }
    var showBiometricPrompt by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // 检查是否支持指纹
    val biometricAvailable = remember { BiometricAuthManager.isBiometricAvailable(context) }

    fun handleConfirm() {
        when {
            oldPwd != AppPassword.getPassword(context) -> errorMsg = "当前密码错误"
            newPwd.length < 4 -> errorMsg = "新密码至少4位"
            newPwd != confirmPwd -> errorMsg = "两次密码不一致"
            else -> {
                AppPassword.setPassword(context, newPwd)
                // 如果支持指纹，询问是否启用
                if (biometricAvailable && !BiometricAuthManager.isBiometricEnabled(context)) {
                    showBiometricPrompt = true
                } else {
                    successMsg = "密码修改成功！"
                    errorMsg = ""
                }
            }
        }
    }

    if (showBiometricPrompt) {
        AlertDialog(
            onDismissRequest = { showBiometricPrompt = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("启用指纹登录", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        "是否启用指纹登录？\n\n启用后，下次登录可以直接使用指纹验证，无需输入密码。",
                        lineHeight = 22.sp
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    BiometricAuthManager.setBiometricEnabled(context, true)
                    showBiometricPrompt = false
                    successMsg = "密码修改成功，已启用指纹登录！"
                    errorMsg = ""
                }) {
                    Text("启用")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showBiometricPrompt = false
                    successMsg = "密码修改成功！"
                    errorMsg = ""
                }) {
                    Text("暂不启用")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("修改密码", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = oldPwd,
                    onValueChange = { oldPwd = it; errorMsg = "" },
                    label = { Text("当前密码") },
                    visualTransformation = if (showOld) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showOld = !showOld }) {
                            Icon(if (showOld) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPwd,
                    onValueChange = { newPwd = it; errorMsg = "" },
                    label = { Text("新密码") },
                    visualTransformation = if (showNew) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showNew = !showNew }) {
                            Icon(if (showNew) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPwd,
                    onValueChange = { confirmPwd = it; errorMsg = "" },
                    label = { Text("确认新密码") },
                    visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showConfirm = !showConfirm }) {
                            Icon(if (showConfirm) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMsg.isNotEmpty()) {
                    Text(errorMsg, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                }
                if (successMsg.isNotEmpty()) {
                    Text(successMsg, color = Color(0xFF2E7D32),
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(onClick = { handleConfirm() }) { Text("确认修改") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
