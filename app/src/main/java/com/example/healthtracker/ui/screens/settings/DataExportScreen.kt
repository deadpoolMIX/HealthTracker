package com.example.healthtracker.ui.screens.settings

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataExportScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDataImport: () -> Unit,
    viewModel: DataExportViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showSuccessDialog by remember { mutableStateOf(false) }

    // 文件保存启动器
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportData(it) }
    }

    LaunchedEffect(state.exportSuccess) {
        if (state.exportSuccess == true) {
            showSuccessDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("数据备份", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 导出卡片
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "导出数据",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "将所有数据导出为 JSON 文本文件，用于备份或迁移到其他设备。包含：摄入记录、身体数据、睡眠记录、饮食计划、自定义食物。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val fileName = "health_tracker_backup_${System.currentTimeMillis()}.json"
                            exportLauncher.launch(fileName)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isExporting
                    ) {
                        if (state.isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("导出中...")
                        } else {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("导出备份文件")
                        }
                    }
                }
            }

            // 导入卡片
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Upload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "导入数据",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "从备份文件恢复数据。注意：导入的数据会追加到现有数据中，不会覆盖。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = onNavigateToDataImport,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("导入备份文件")
                    }
                }
            }

            // 说明卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "备份说明",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• 备份文件为 JSON 格式文本文件\n• 可使用任意文本编辑器查看\n• 建议定期备份以防数据丢失\n• 导入时会追加数据，不会覆盖现有数据",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // 成功对话框
    if (showSuccessDialog && state.recordCounts != null) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                viewModel.clearMessage()
            },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("导出成功") },
            text = {
                Column {
                    Text("已成功导出以下数据：")
                    Spacer(modifier = Modifier.height(8.dp))
                    state.recordCounts?.let { counts ->
                        if (counts.intakeCount > 0) Text("• 摄入记录：${counts.intakeCount} 条")
                        if (counts.bodyCount > 0) Text("• 身体数据：${counts.bodyCount} 条")
                        if (counts.sleepCount > 0) Text("• 睡眠记录：${counts.sleepCount} 条")
                        if (counts.foodCount > 0) Text("• 自定义食物：${counts.foodCount} 个")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    viewModel.clearMessage()
                }) {
                    Text("确定")
                }
            }
        )
    }

    // 错误对话框
    if (state.exportSuccess == false && state.message != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearMessage() },
            icon = { Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("操作失败") },
            text = { Text(state.message ?: "未知错误") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearMessage() }) {
                    Text("确定")
                }
            }
        )
    }
}