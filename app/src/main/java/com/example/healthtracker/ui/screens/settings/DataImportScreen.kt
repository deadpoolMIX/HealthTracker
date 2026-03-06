package com.example.healthtracker.ui.screens.settings

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
fun DataImportScreen(
    onNavigateBack: () -> Unit,
    viewModel: DataExportViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showResultDialog by remember { mutableStateOf(false) }

    // 文件选择启动器
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importData(it) }
    }

    LaunchedEffect(state.exportSuccess) {
        if (state.exportSuccess != null) {
            showResultDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("导入数据", fontWeight = FontWeight.Medium) },
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Icon(
                imageVector = Icons.Default.Upload,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )

            Text(
                text = "选择备份文件",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "选择要导入的 JSON 备份文件\n数据将追加到现有数据中",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    importLauncher.launch(arrayOf("application/json", "*/*"))
                },
                enabled = !state.isExporting,
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                if (state.isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("导入中...")
                } else {
                    Icon(Icons.Default.FolderOpen, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("选择文件")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                            text = "导入说明",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• 支持本应用导出的 JSON 备份文件\n• 导入数据会追加，不会覆盖现有数据\n• 如果导入重复数据，可能会出现重复记录\n• 建议在导入前先导出一份当前数据作为备份",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // 结果对话框
    if (showResultDialog && state.recordCounts != null && state.exportSuccess == true) {
        AlertDialog(
            onDismissRequest = {
                showResultDialog = false
                viewModel.clearMessage()
            },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("导入成功") },
            text = {
                Column {
                    Text("已成功导入以下数据：")
                    Spacer(modifier = Modifier.height(8.dp))
                    state.recordCounts?.let { counts ->
                        if (counts.intakeCount > 0) Text("• 摄入记录：${counts.intakeCount} 条")
                        if (counts.bodyCount > 0) Text("• 身体数据：${counts.bodyCount} 条")
                        if (counts.sleepCount > 0) Text("• 睡眠记录：${counts.sleepCount} 条")
                        if (counts.foodCount > 0) Text("• 自定义食物：${counts.foodCount} 个")
                        if (counts.intakeCount == 0 && counts.bodyCount == 0 &&
                            counts.sleepCount == 0 && counts.foodCount == 0) {
                            Text("备份文件中没有数据")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showResultDialog = false
                    viewModel.clearMessage()
                    onNavigateBack()
                }) {
                    Text("确定")
                }
            }
        )
    }

    // 错误对话框
    if (showResultDialog && state.exportSuccess == false && state.message != null) {
        AlertDialog(
            onDismissRequest = {
                showResultDialog = false
                viewModel.clearMessage()
            },
            icon = { Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("导入失败") },
            text = { Text(state.message ?: "未知错误") },
            confirmButton = {
                TextButton(onClick = {
                    showResultDialog = false
                    viewModel.clearMessage()
                }) {
                    Text("确定")
                }
            }
        )
    }
}