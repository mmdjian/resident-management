package com.resident.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.resident.app.data.entity.Resident
import com.resident.app.ui.viewmodel.ResidentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditResidentScreen(
    viewModel: ResidentViewModel,
    resident: Resident?,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(resident?.name ?: "") }
    var gender by remember { mutableStateOf(resident?.gender ?: "") }
    var age by remember { mutableStateOf(resident?.age?.toString() ?: "") }
    var occupation by remember { mutableStateOf(resident?.occupation ?: "") }
    var phone by remember { mutableStateOf(resident?.phone ?: "") }
    var address by remember { mutableStateOf(resident?.address ?: "") }

    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()

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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("姓名") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = gender,
                    onValueChange = { gender = it },
                    label = { Text("性别") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text("男/女") }
                )

                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("年龄") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

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

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (name.isNotBlank() && gender.isNotBlank() && age.isNotBlank()) {
                        val newResident = if (resident == null) {
                            Resident(
                                name = name,
                                gender = gender,
                                age = age.toInt(),
                                occupation = occupation,
                                phone = phone,
                                address = address
                            )
                        } else {
                            resident.copy(
                                name = name,
                                gender = gender,
                                age = age.toInt(),
                                occupation = occupation,
                                phone = phone,
                                address = address
                            )
                        }

                        if (resident == null) {
                            viewModel.insertResident(newResident)
                        } else {
                            viewModel.updateResident(newResident)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isLoading.not()
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
        }
    }
}
