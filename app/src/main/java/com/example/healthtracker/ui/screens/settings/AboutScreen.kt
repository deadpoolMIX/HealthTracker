package com.example.healthtracker.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTestData: () -> Unit = {},
    versionName: String = "1.0",
    githubUrl: String = "https://github.com/deadpoolMIX/HealthTracker"
) {
    val context = LocalContext.current

    // 连续点击计数
    var clickCount by remember { mutableStateOf(0) }
    var showTestDataOption by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("关于软件", fontWeight = FontWeight.Medium) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 版本号
            ListItem(
                headlineContent = { Text("版本") },
                supportingContent = { Text(versionName) },
                modifier = Modifier.clickable {
                    clickCount++
                    if (clickCount >= 5) {
                        showTestDataOption = true
                        clickCount = 0
                    }
                }
            )

            HorizontalDivider()

            // GitHub 地址
            ListItem(
                headlineContent = { Text("GitHub") },
                supportingContent = { Text(githubUrl) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
                        context.startActivity(intent)
                    }
            )

            // 测试数据功能入口（连点5次后显示）
            if (showTestDataOption) {
                HorizontalDivider()
                ListItem(
                    headlineContent = {
                        Text(
                            "测试数据生成",
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    supportingContent = { Text("生成或删除模拟测试数据") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToTestData() }
                )
            }
        }
    }
}