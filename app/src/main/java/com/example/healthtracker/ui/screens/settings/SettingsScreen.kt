package com.example.healthtracker.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToUserProfile: () -> Unit,
    onNavigateToMealPlans: () -> Unit,
    onNavigateToFoodManager: () -> Unit,
    onNavigateToDataExport: () -> Unit,
    onNavigateToThemeSettings: () -> Unit = {},
    testDataViewModel: TestDataViewModel = hiltViewModel()
) {
    val testDataState by testDataViewModel.state.collectAsState()
    var showGenerateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置", fontWeight = FontWeight.Medium) },
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
        ) {
            SettingsItem(
                title = "个人资料",
                subtitle = "设置身高、体重、活动水平等基本信息",
                onClick = onNavigateToUserProfile
            )
            HorizontalDivider()
            // 饮食计划已隐藏
            // SettingsItem(
            //     title = "饮食计划",
            //     subtitle = "管理自定义饮食计划",
            //     onClick = onNavigateToMealPlans
            // )
            // HorizontalDivider()
            SettingsItem(
                title = "食物管理",
                subtitle = "添加或编辑自定义食物",
                onClick = onNavigateToFoodManager
            )
            HorizontalDivider()
            SettingsItem(
                title = "数据备份与恢复",
                subtitle = "导出或导入应用数据",
                onClick = onNavigateToDataExport
            )
            HorizontalDivider()
            SettingsItem(
                title = "主题设置",
                subtitle = "切换主题颜色和深色模式",
                onClick = onNavigateToThemeSettings
            )
            HorizontalDivider()

            // 开发者选项
            Text(
                text = "开发者选项",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )
            SettingsItem(
                title = "生成测试数据",
                subtitle = "模拟10天的摄入、身体、睡眠数据用于测试报表",
                icon = Icons.Default.Science,
                onClick = { showGenerateDialog = true }
            )
        }
    }

    // 生成测试数据确认对话框
    if (showGenerateDialog) {
        AlertDialog(
            onDismissRequest = { showGenerateDialog = false },
            icon = { Icon(Icons.Default.BugReport, contentDescription = null) },
            title = { Text("生成测试数据") },
            text = { Text("将生成最近一个月（30天）的模拟数据（摄入、身体、睡眠记录），用于测试报表功能。\n\n注意：已存在的数据不会被覆盖。") },
            confirmButton = {
                Button(
                    onClick = {
                        testDataViewModel.generateTestData()
                        showGenerateDialog = false
                    }
                ) {
                    Text("生成")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGenerateDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 结果提示
    testDataState.message?.let { message ->
        AlertDialog(
            onDismissRequest = { testDataViewModel.clearMessage() },
            title = { Text(if (testDataState.success == true) "成功" else "失败") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { testDataViewModel.clearMessage() }) {
                    Text("确定")
                }
            }
        )
    }

    // 加载中提示
    if (testDataState.isGenerating) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = icon?.let {
            { Icon(it, contentDescription = null) }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    )
}