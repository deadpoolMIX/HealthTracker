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
import com.example.healthtracker.data.local.entity.BodyRecordEntity
import com.example.healthtracker.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyDataDetailScreen(
    viewModel: ReportsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("身体数据详情", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.bodyData.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无身体数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    BodyDataOverview(data = uiState.bodyData)
                }

                // 每日数据列表
                items(uiState.bodyData.size) { index ->
                    BodyDataItem(data = uiState.bodyData[index])
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun BodyDataOverview(data: List<BodyRecordEntity>) {
    val avgWeight = if (data.isNotEmpty()) data.mapNotNull { it.weight }.average() else 0.0
    val avgBodyFat = if (data.isNotEmpty()) data.mapNotNull { it.bodyFatRate }.average() else 0.0
    val avgMuscle = if (data.isNotEmpty()) data.mapNotNull { it.muscleMass }.average() else 0.0

    val latestWeight = data.firstOrNull()?.weight
    val firstWeight = data.lastOrNull()?.weight
    val weightChange = if (latestWeight != null && firstWeight != null) latestWeight - firstWeight else 0.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "身体数据概览",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BodyStatItem(
                    label = "平均体重",
                    value = String.format("%.1f", avgWeight),
                    unit = "kg"
                )
                BodyStatItem(
                    label = "平均体脂",
                    value = String.format("%.1f", avgBodyFat),
                    unit = "%"
                )
                BodyStatItem(
                    label = "平均肌肉",
                    value = String.format("%.1f", avgMuscle),
                    unit = "kg"
                )
            }
            if (weightChange != 0.0) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "期间体重变化: ${if (weightChange > 0) "+" else ""}${String.format("%.1f", weightChange)} kg",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (weightChange < 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun BodyStatItem(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BodyDataItem(data: BodyRecordEntity) {
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
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                data.weight?.let {
                    DataChip(label = "体重", value = String.format("%.1f kg", it))
                }
                data.bodyFatRate?.let {
                    DataChip(label = "体脂", value = String.format("%.1f%%", it))
                }
                data.muscleMass?.let {
                    DataChip(label = "肌肉", value = String.format("%.1f kg", it))
                }
            }

            if (data.chest != null || data.waist != null || data.hip != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    data.chest?.let {
                        DataChip(label = "胸围", value = String.format("%.1f cm", it))
                    }
                    data.waist?.let {
                        DataChip(label = "腰围", value = String.format("%.1f cm", it))
                    }
                    data.hip?.let {
                        DataChip(label = "臀围", value = String.format("%.1f cm", it))
                    }
                }
            }
        }
    }
}

@Composable
private fun DataChip(label: String, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}