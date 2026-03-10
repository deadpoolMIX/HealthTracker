package com.example.healthtracker.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToUserProfile: () -> Unit,
    onNavigateToMealPlans: () -> Unit,
    onNavigateToFoodManager: () -> Unit,
    onNavigateToDataExport: () -> Unit,
    onNavigateToThemeSettings: () -> Unit = {},
    onNavigateToFoodDataImport: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {}
) {
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
            SettingsItem(
                title = "食物管理",
                subtitle = "添加或编辑自定义食物",
                onClick = onNavigateToFoodManager
            )
            HorizontalDivider()
            SettingsItem(
                title = "导入食物数据",
                subtitle = "从JSON/CSV文件导入食物营养数据库",
                onClick = onNavigateToFoodDataImport
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
            SettingsItem(
                title = "关于软件",
                subtitle = "版本信息和项目地址",
                onClick = onNavigateToAbout
            )
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    )
}