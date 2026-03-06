package com.example.healthtracker.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.healthtracker.ui.theme.ThemeColor
import com.example.healthtracker.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ThemeSettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("主题设置", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 主题模式
            item {
                SettingsSection(title = "主题模式") {
                    ThemeModeSelector(
                        currentMode = settings?.themeMode ?: 0,
                        onModeSelected = { viewModel.updateThemeMode(it) }
                    )
                }
            }

            // 主题颜色
            item {
                SettingsSection(title = "主题颜色") {
                    ThemeColorSelector(
                        currentColor = settings?.themeColor ?: 0,
                        onColorSelected = { viewModel.updateThemeColor(it) }
                    )
                }
            }

            // 预览
            item {
                SettingsSection(title = "颜色预览") {
                    ColorPreviewCard()
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun ThemeModeSelector(
    currentMode: Int,
    onModeSelected: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ThemeMode.values().forEach { mode ->
            ThemeModeItem(
                mode = mode,
                isSelected = currentMode == mode.index,
                onClick = { onModeSelected(mode.index) }
            )
        }
    }
}

@Composable
private fun ThemeModeItem(
    mode: ThemeMode,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else
            null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mode.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = getModeDescription(mode),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "已选择",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun getModeDescription(mode: ThemeMode): String {
    return when (mode) {
        ThemeMode.SYSTEM -> "根据系统设置自动切换"
        ThemeMode.LIGHT -> "始终使用浅色主题"
        ThemeMode.DARK -> "始终使用深色主题"
    }
}

@Composable
private fun ThemeColorSelector(
    currentColor: Int,
    onColorSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ThemeColor.values().forEach { color ->
            ColorItem(
                color = color,
                isSelected = currentColor == color.index,
                onClick = { onColorSelected(color.index) }
            )
        }
    }
}

@Composable
private fun RowScope.ColorItem(
    color: ThemeColor,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorValue = getPreviewColor(color)

    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(colorValue)
            .then(
                if (isSelected)
                    Modifier.border(3.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                else
                    Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "已选择",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = color.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
    }
}

@Composable
private fun ColorPreviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "预览效果",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // 按钮预览
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = {}) {
                    Text("主要按钮")
                }
                OutlinedButton(onClick = {}) {
                    Text("次要按钮")
                }
            }

            // 文本预览
            Text(
                text = "这是正文文本样式",
                style = MaterialTheme.typography.bodyMedium
            )

            // 进度条预览
            LinearProgressIndicator(
                progress = { 0.7f },
                modifier = Modifier.fillMaxWidth()
            )

            // Chip 预览
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(onClick = {}, label = { Text("标签1") })
                FilterChip(selected = true, onClick = {}, label = { Text("标签2") })
            }
        }
    }
}

@Composable
private fun getPreviewColor(themeColor: ThemeColor): Color {
    return when (themeColor) {
        ThemeColor.GREEN -> Color(0xFF326B26)
        ThemeColor.BLUE -> Color(0xFF005DB6)
        ThemeColor.PURPLE -> Color(0xFF6650a4)
        ThemeColor.ORANGE -> Color(0xFFA63E00)
        ThemeColor.RED -> Color(0xFFBA1A1A)
    }
}