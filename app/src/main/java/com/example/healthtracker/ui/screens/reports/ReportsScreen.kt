package com.example.healthtracker.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.healthtracker.data.local.entity.BodyRecordEntity
import com.example.healthtracker.data.local.entity.SleepRecordEntity
import com.example.healthtracker.ui.screens.reports.DailyNutrition
import com.example.healthtracker.ui.screens.reports.ReportsViewModel
import com.example.healthtracker.util.DateTimeUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel(),
    onNavigateToDataExport: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val periods = listOf("天", "周", "月", "年")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("数据报表", fontWeight = FontWeight.Medium) },
                actions = {
                    IconButton(onClick = onNavigateToDataExport) {
                        Icon(Icons.Default.Download, contentDescription = "导出数据")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.intakeData.isEmpty() && uiState.bodyData.isEmpty() && uiState.sleepData.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "暂无数据",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "请先记录数据或生成测试数据",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 周期选择
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        periods.forEachIndexed { index, period ->
                            FilterChip(
                                selected = uiState.selectedPeriod == index,
                                onClick = { viewModel.setPeriod(index) },
                                label = { Text(period) }
                            )
                        }
                    }
                }

                // 营养素堆叠柱状图
                item {
                    NutritionChartCard(
                        data = uiState.intakeData,
                        period = uiState.selectedPeriod
                    )
                }

                // 身体数据折线图
                if (uiState.bodyData.isNotEmpty()) {
                    item {
                        BodyDataChartCard(
                            data = uiState.bodyData,
                            period = uiState.selectedPeriod
                        )
                    }
                }

                // 睡眠范围条形图
                if (uiState.sleepData.isNotEmpty()) {
                    item {
                        SleepChartCard(
                            data = uiState.sleepData,
                            period = uiState.selectedPeriod,
                            avgSleepTime = viewModel.getAverageSleepTime(),
                            avgWakeTime = viewModel.getAverageWakeTime(),
                            avgDuration = viewModel.getAverageSleepDuration()
                        )
                    }
                }

                // 底部间距
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// 营养素柱状图卡片
@Composable
private fun NutritionChartCard(
    data: List<DailyNutrition>,
    period: Int
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "营养素摄入",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                // 图例
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LegendItem("碳水", primaryColor)
                    LegendItem("蛋白质", secondaryColor)
                    LegendItem("脂肪", tertiaryColor)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (data.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无摄入数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                // 柱状图
                val maxCalories = data.maxOfOrNull { it.calories } ?: 1.0
                val maxValue = maxOf(
                    data.maxOfOrNull { it.carbs } ?: 1.0,
                    data.maxOfOrNull { it.protein } ?: 1.0,
                    data.maxOfOrNull { it.fat } ?: 1.0
                )

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    val barWidth = size.width / (data.size * 1.5f)
                    val spacing = barWidth * 0.5f

                    data.sortedBy { it.date }.forEachIndexed { index, day ->
                        val x = index * (barWidth * 1.5f) + spacing

                        // 碳水
                        val carbsHeight = (day.carbs / maxValue * (size.height - 40)).toFloat()
                        drawRect(
                            color = primaryColor,
                            topLeft = Offset(x, size.height - carbsHeight - 20),
                            size = Size(barWidth / 3, carbsHeight)
                        )

                        // 蛋白质
                        val proteinHeight = (day.protein / maxValue * (size.height - 40)).toFloat()
                        drawRect(
                            color = secondaryColor,
                            topLeft = Offset(x + barWidth / 3, size.height - proteinHeight - 20),
                            size = Size(barWidth / 3, proteinHeight)
                        )

                        // 脂肪
                        val fatHeight = (day.fat / maxValue * (size.height - 40)).toFloat()
                        drawRect(
                            color = tertiaryColor,
                            topLeft = Offset(x + barWidth * 2 / 3, size.height - fatHeight - 20),
                            size = Size(barWidth / 3, fatHeight)
                        )
                    }
                }

                // 日期标签
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    data.sortedBy { it.date }.takeLast(7).forEach { day ->
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = day.date
                        Text(
                            text = "${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.DAY_OF_MONTH)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 总热量显示
                Spacer(modifier = Modifier.height(8.dp))
                val totalCalories = data.sumOf { it.calories }
                val avgCalories = if (data.isNotEmpty()) totalCalories / data.size else 0.0
                Text(
                    text = "平均摄入: ${String.format("%.0f", avgCalories)} kcal/天",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LegendItem(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// 身体数据折线图卡片
@Composable
private fun BodyDataChartCard(
    data: List<BodyRecordEntity>,
    period: Int
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "身体数据趋势",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LegendItem("体重", primaryColor)
                    LegendItem("体脂", errorColor)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 折线图
            val sortedData = data.sortedBy { it.date }
            val weights = sortedData.mapNotNull { it.weight }
            val bodyFats = sortedData.mapNotNull { it.bodyFatRate }

            if (weights.isEmpty() && bodyFats.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无身体数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                val minWeight = weights.minOrNull() ?: 0.0
                val maxWeight = weights.maxOrNull() ?: 100.0
                val minBodyFat = bodyFats.minOrNull() ?: 0.0
                val maxBodyFat = bodyFats.maxOrNull() ?: 30.0

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    // 体重折线
                    if (weights.isNotEmpty()) {
                        val path = Path()
                        weights.forEachIndexed { index, weight ->
                            val x = (index.toFloat() / (weights.size - 1).coerceAtLeast(1)) * size.width
                            val y = size.height - ((weight - minWeight) / (maxWeight - minWeight).coerceAtLeast(1.0) * (size.height - 40)).toFloat() - 20
                            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(
                            path = path,
                            color = primaryColor,
                            style = Stroke(width = 3f)
                        )
                        // 绘制点
                        weights.forEachIndexed { index, weight ->
                            val x = (index.toFloat() / (weights.size - 1).coerceAtLeast(1)) * size.width
                            val y = size.height - ((weight - minWeight) / (maxWeight - minWeight).coerceAtLeast(1.0) * (size.height - 40)).toFloat() - 20
                            drawCircle(
                                color = primaryColor,
                                radius = 4f,
                                center = Offset(x, y)
                            )
                        }
                    }

                    // 体脂折线
                    if (bodyFats.isNotEmpty()) {
                        val path = Path()
                        bodyFats.forEachIndexed { index, bodyFat ->
                            val x = (index.toFloat() / (bodyFats.size - 1).coerceAtLeast(1)) * size.width
                            val y = size.height - ((bodyFat - minBodyFat) / (maxBodyFat - minBodyFat).coerceAtLeast(1.0) * (size.height - 40)).toFloat() - 20
                            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(
                            path = path,
                            color = errorColor,
                            style = Stroke(width = 3f)
                        )
                        // 绘制点
                        bodyFats.forEachIndexed { index, bodyFat ->
                            val x = (index.toFloat() / (bodyFats.size - 1).coerceAtLeast(1)) * size.width
                            val y = size.height - ((bodyFat - minBodyFat) / (maxBodyFat - minBodyFat).coerceAtLeast(1.0) * (size.height - 40)).toFloat() - 20
                            drawCircle(
                                color = errorColor,
                                radius = 4f,
                                center = Offset(x, y)
                            )
                        }
                    }
                }

                // 统计信息
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    weights.lastOrNull()?.let {
                        StatItem("当前体重", "${String.format("%.1f", it)} kg")
                    }
                    bodyFats.lastOrNull()?.let {
                        StatItem("当前体脂", "${String.format("%.1f", it)}%")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// 睡眠范围条形图卡片
@Composable
private fun SleepChartCard(
    data: List<SleepRecordEntity>,
    period: Int,
    avgSleepTime: String,
    avgWakeTime: String,
    avgDuration: Long
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "睡眠记录",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 平均睡眠信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("平均入睡", avgSleepTime)
                StatItem("平均起床", avgWakeTime)
                StatItem("平均时长", formatDuration(avgDuration))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 睡眠范围条形图
            val sortedData = data.sortedBy { it.date }.takeLast(7)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                // 时间轴：18:00 - 12:00（次日中午）
                val startHour = 18f
                val totalHours = 18f

                sortedData.forEachIndexed { index, sleep ->
                    val calSleep = Calendar.getInstance()
                    calSleep.timeInMillis = sleep.sleepTime
                    val sleepHour = calSleep.get(Calendar.HOUR_OF_DAY) + calSleep.get(Calendar.MINUTE) / 60f

                    val calWake = Calendar.getInstance()
                    calWake.timeInMillis = sleep.wakeTime
                    val wakeHour = calWake.get(Calendar.HOUR_OF_DAY) + calWake.get(Calendar.MINUTE) / 60f

                    // 计算条形位置
                    var sleepOffset = sleepHour - startHour
                    if (sleepOffset < 0) sleepOffset += 24f

                    var wakeOffset = wakeHour - startHour
                    if (wakeOffset < 0) wakeOffset += 24f

                    // 如果起床时间小于入睡时间，说明跨越了午夜
                    val barWidth = if (wakeOffset < sleepOffset) {
                        (24f - sleepOffset) + wakeOffset
                    } else {
                        wakeOffset - sleepOffset
                    }

                    val barHeight = size.height / sortedData.size * 0.7f
                    val y = index * (size.height / sortedData.size) + barHeight * 0.15f

                    // 绘制睡眠条
                    drawRect(
                        color = primaryColor.copy(alpha = 0.6f),
                        topLeft = Offset(sleepOffset / totalHours * size.width, y),
                        size = Size(barWidth / totalHours * size.width, barHeight)
                    )
                }

                // 绘制午夜线
                val midnightX = (24f - startHour) / totalHours * size.width
                drawLine(
                    color = onSurfaceVariantColor.copy(alpha = 0.3f),
                    start = Offset(midnightX, 0f),
                    end = Offset(midnightX, size.height),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
                )
            }

            // 时间轴标签
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("18:00", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariantColor)
                Text("00:00", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariantColor)
                Text("06:00", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariantColor)
                Text("12:00", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariantColor)
            }
        }
    }
}

private fun formatDuration(minutes: Long): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return if (hours > 0) "${hours}h${mins}m" else "${mins}m"
}