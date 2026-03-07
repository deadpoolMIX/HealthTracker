package com.example.healthtracker.ui.screens.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.healthtracker.data.local.entity.SleepRecordEntity
import com.example.healthtracker.util.DateTimeUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepDetailScreen(
    viewModel: ReportsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("睡眠记录详情", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.sleepData.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无睡眠数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 统计概览
                item {
                    SleepOverview(
                        data = uiState.sleepData,
                        avgSleepTime = viewModel.getAverageSleepTime(),
                        avgWakeTime = viewModel.getAverageWakeTime(),
                        avgDuration = viewModel.getAverageSleepDuration()
                    )
                }

                // 每日数据列表
                items(uiState.sleepData.size) { index ->
                    SleepDataItem(data = uiState.sleepData[index])
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun SleepOverview(
    data: List<SleepRecordEntity>,
    avgSleepTime: String,
    avgWakeTime: String,
    avgDuration: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "睡眠数据概览",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SleepStatItem(label = "平均入睡", value = avgSleepTime)
                SleepStatItem(label = "平均起床", value = avgWakeTime)
                SleepStatItem(label = "平均时长", value = formatDuration(avgDuration))
            }

            // 睡眠质量分析
            if (data.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                val avgDurationHours = avgDuration / 3600000.0
                val qualityText = when {
                    avgDurationHours >= 7.5 -> "睡眠充足"
                    avgDurationHours >= 6.5 -> "睡眠尚可"
                    else -> "睡眠不足"
                }
                val qualityColor = when {
                    avgDurationHours >= 7.5 -> MaterialTheme.colorScheme.primary
                    avgDurationHours >= 6.5 -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.error
                }
                Text(
                    text = "睡眠质量: $qualityText",
                    style = MaterialTheme.typography.bodyMedium,
                    color = qualityColor
                )
            }
        }
    }
}

@Composable
private fun SleepStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SleepDataItem(data: SleepRecordEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = DateTimeUtils.formatDate(data.date),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 入睡时间
                Column {
                    Text(
                        text = "入睡",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatTime(data.sleepTime),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 睡眠时长
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "时长",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDuration(data.duration),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // 起床时间
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "起床",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatTime(data.wakeTime),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = timestamp
    return String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
}

private fun formatDuration(durationMs: Long): String {
    val hours = durationMs / 3600000
    val minutes = (durationMs % 3600000) / 60000
    return if (hours > 0) {
        "${hours}h ${minutes}min"
    } else {
        "${minutes}min"
    }
}