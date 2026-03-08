package com.example.healthtracker.ui.screens.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.healthtracker.data.local.entity.SleepRecordEntity
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepDetailScreen(
    viewModel: SleepDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("睡眠记录详情", fontWeight = FontWeight.Medium) },
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
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 周期选择（带左右箭头）
            item {
                PeriodSelectorWithArrows(
                    selectedPeriod = uiState.selectedPeriod,
                    periodOffset = uiState.periodOffset,
                    onPeriodSelected = { viewModel.setPeriod(it) },
                    onPreviousPeriod = { viewModel.setPeriodOffset(uiState.periodOffset + 1) },
                    onNextPeriod = { if (uiState.periodOffset > 0) viewModel.setPeriodOffset(uiState.periodOffset - 1) }
                )
            }

            // 睡眠图表
            item {
                SleepChartCard(
                    sleepData = uiState.sleepData,
                    period = uiState.selectedPeriod,
                    periodOffset = uiState.periodOffset,
                    yearPage = uiState.yearPage,
                    onYearPageChange = { viewModel.setYearPage(it) }
                )
            }

            // 睡眠总结（基于当前数据）
            item {
                SleepSummaryCard(
                    avgSleepTime = viewModel.getAverageSleepTime(),
                    avgWakeTime = viewModel.getAverageWakeTime(),
                    avgDuration = viewModel.getAverageSleepDuration()
                )
            }
        }
    }
}

@Composable
private fun PeriodSelectorWithArrows(
    selectedPeriod: Int,
    periodOffset: Int,
    onPeriodSelected: (Int) -> Unit,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左箭头（上一个周期）
            IconButton(
                onClick = onPreviousPeriod,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.KeyboardArrowLeft,
                    contentDescription = "上一个周期",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // 周/月/年 选择器
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("周", "月", "年").forEachIndexed { index, label ->
                    FilterChip(
                        selected = selectedPeriod == index,
                        onClick = { onPeriodSelected(index) },
                        label = { Text(label, fontSize = 13.sp) },
                        modifier = Modifier.height(32.dp)
                    )
                }
            }

            // 右箭头（下一个周期，仅当 offset > 0 时可用）
            IconButton(
                onClick = onNextPeriod,
                enabled = periodOffset > 0,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = "下一个周期",
                    tint = if (periodOffset > 0) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
private fun SleepSummaryCard(
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
                text = "睡眠总结",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("平均入睡", avgSleepTime)
                StatItem("平均起床", avgWakeTime)
                StatItem("平均时长", formatSleepDuration(avgDuration))
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
private fun SleepChartCard(
    sleepData: List<SleepRecordEntity>,
    period: Int,
    periodOffset: Int,
    yearPage: Int,
    onYearPageChange: (Int) -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "睡眠图表",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // 年模式时显示翻页箭头
                if (period == 2) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onYearPageChange(0) },
                            enabled = yearPage == 1
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowLeft,
                                contentDescription = "上一页",
                                tint = if (yearPage == 0) onSurfaceVariantColor.copy(alpha = 0.3f) else primaryColor
                            )
                        }
                        Text(
                            text = "${yearPage + 1}/2",
                            style = MaterialTheme.typography.labelMedium
                        )
                        IconButton(
                            onClick = { onYearPageChange(1) },
                            enabled = yearPage == 0
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = "下一页",
                                tint = if (yearPage == 1) onSurfaceVariantColor.copy(alpha = 0.3f) else primaryColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (period) {
                0 -> WeekSleepChart(data = sleepData, primaryColor = primaryColor, onSurfaceVariantColor = onSurfaceVariantColor)
                1 -> MonthSleepChart(data = sleepData, periodOffset = periodOffset, primaryColor = primaryColor, onSurfaceVariantColor = onSurfaceVariantColor)
                2 -> YearSleepChart(
                    data = sleepData,
                    page = yearPage,
                    primaryColor = primaryColor,
                    onSurfaceVariantColor = onSurfaceVariantColor
                )
            }
        }
    }
}

/**
 * 时间范围常量
 * 图表显示从 22:00 到次日 12:00
 */
private const val CHART_START_HOUR = 22f
private const val CHART_END_HOUR = 36f // 12:00 + 24

/**
 * 将时间转换为图表坐标（0-14小时范围）
 * 入睡时间：晚上22点之后显示在图表顶部，凌晨0-6点显示在中间
 * 起床时间：早上6-12点显示在图表底部
 */
private fun convertToChartHour(hour: Int, minute: Int, isWakeTime: Boolean = false): Float {
    val h = hour + minute / 60f
    return when {
        // 凌晨时间 0:00-12:00 加24小时变成 24-36
        h < 12f -> h + 24f
        // 下午12:00-18:00 保持在原位置或映射到图表范围外
        h < 18f -> h + 24f // 12:00-18:00 -> 36-42（超出图表，会显示在底部）
        // 晚上18:00-22:00 映射到图表范围
        h < 22f -> 22f // 18:00-22:00 显示在图表顶部
        // 晚上22:00-24:00 保持原值
        else -> h
    }
}

@Composable
private fun WeekSleepChart(
    data: List<SleepRecordEntity>,
    primaryColor: Color,
    onSurfaceVariantColor: Color
) {
    val sortedData = data.sortedBy { it.date }.takeLast(7)

    if (sortedData.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("暂无睡眠数据", color = onSurfaceVariantColor)
        }
        return
    }

    val totalHours = CHART_END_HOUR - CHART_START_HOUR

    val timePoints = listOf(
        22f to "22:00",
        24f to "00:00",
        26f to "02:00",
        28f to "04:00",
        30f to "06:00",
        32f to "08:00",
        34f to "10:00",
        36f to "12:00"
    )

    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .width(40.dp)
                .height(200.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            timePoints.forEach { (_, label) ->
                Text(label, style = MaterialTheme.typography.labelSmall, color = onSurfaceVariantColor, maxLines = 1)
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val chartHeight = size.height
                val chartWidth = size.width
                val barCount = sortedData.size
                val totalSpacing = chartWidth * 0.25f
                val totalBarWidth = chartWidth - totalSpacing
                val barWidth = totalBarWidth / barCount
                val spacing = totalSpacing / (barCount + 1)

                // 绘制虚线
                timePoints.forEach { (hourContinuous, _) ->
                    val lineY = (hourContinuous - CHART_START_HOUR) / totalHours * chartHeight
                    drawLine(
                        color = onSurfaceVariantColor,
                        start = Offset(0f, lineY),
                        end = Offset(chartWidth, lineY),
                        strokeWidth = 0.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                    )
                }

                // 绘制柱状图
                sortedData.forEachIndexed { index, sleep ->
                    val calSleep = Calendar.getInstance()
                    calSleep.timeInMillis = sleep.sleepTime
                    val sleepHourChart = convertToChartHour(
                        calSleep.get(Calendar.HOUR_OF_DAY),
                        calSleep.get(Calendar.MINUTE),
                        isWakeTime = false
                    )

                    val calWake = Calendar.getInstance()
                    calWake.timeInMillis = sleep.wakeTime
                    val wakeHourChart = convertToChartHour(
                        calWake.get(Calendar.HOUR_OF_DAY),
                        calWake.get(Calendar.MINUTE),
                        isWakeTime = true
                    )

                    val x = spacing + index * (barWidth + spacing)
                    val sleepY = ((sleepHourChart - CHART_START_HOUR) / totalHours * chartHeight).coerceIn(0f, chartHeight)
                    val wakeY = ((wakeHourChart - CHART_START_HOUR) / totalHours * chartHeight).coerceIn(0f, chartHeight)

                    if (wakeY > sleepY) {
                        drawRoundRect(
                            color = primaryColor.copy(alpha = 0.8f),
                            topLeft = Offset(x, sleepY),
                            size = Size(barWidth, wakeY - sleepY),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                sortedData.forEach { sleep ->
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = sleep.date
                    Text(
                        text = "${cal.get(Calendar.DAY_OF_MONTH)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurfaceVariantColor,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthSleepChart(
    data: List<SleepRecordEntity>,
    periodOffset: Int,
    primaryColor: Color,
    onSurfaceVariantColor: Color
) {
    // 计算四周的数据 - 与 ViewModel 的日期范围逻辑一致
    val calendar = Calendar.getInstance()
    val now = System.currentTimeMillis()
    calendar.timeInMillis = now
    calendar.add(Calendar.MONTH, -periodOffset)
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    val monthlyData = mutableListOf<WeeklySleepChartData>()

    // 将一个月分成4周
    for (i in 0 until 4) {
        val weekStart = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 6)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val weekEnd = calendar.timeInMillis

        val weekRecords = data.filter { it.date in weekStart..weekEnd }

        val avgSleepHour = if (weekRecords.isNotEmpty()) {
            weekRecords.map { record ->
                val cal = Calendar.getInstance()
                cal.timeInMillis = record.sleepTime
                convertToChartHour(
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    isWakeTime = false
                )
            }.average().toFloat()
        } else 23f

        val avgWakeHour = if (weekRecords.isNotEmpty()) {
            weekRecords.map { record ->
                val cal = Calendar.getInstance()
                cal.timeInMillis = record.wakeTime
                convertToChartHour(
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    isWakeTime = true
                )
            }.average().toFloat()
        } else 31f // 默认07:00，即31

        monthlyData.add(WeeklySleepChartData("第${i + 1}周", avgSleepHour, avgWakeHour))

        calendar.add(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
    }

    val totalHours = CHART_END_HOUR - CHART_START_HOUR

    val timePoints = listOf(
        22f to "22:00",
        24f to "00:00",
        26f to "02:00",
        28f to "04:00",
        30f to "06:00",
        32f to "08:00",
        34f to "10:00",
        36f to "12:00"
    )

    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .width(40.dp)
                .height(200.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            timePoints.forEach { (_, label) ->
                Text(label, style = MaterialTheme.typography.labelSmall, color = onSurfaceVariantColor, maxLines = 1)
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val chartHeight = size.height
                val chartWidth = size.width
                val barCount = monthlyData.size
                val totalSpacing = chartWidth * 0.4f
                val totalBarWidth = chartWidth - totalSpacing
                val barWidth = totalBarWidth / barCount
                val spacing = totalSpacing / (barCount + 1)

                // 绘制虚线
                timePoints.forEach { (hourContinuous, _) ->
                    val lineY = (hourContinuous - CHART_START_HOUR) / totalHours * chartHeight
                    drawLine(
                        color = onSurfaceVariantColor,
                        start = Offset(0f, lineY),
                        end = Offset(chartWidth, lineY),
                        strokeWidth = 0.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                    )
                }

                // 绘制柱状图
                monthlyData.forEachIndexed { index, week ->
                    val x = spacing + index * (barWidth + spacing)
                    val sleepY = ((week.avgSleepHour - CHART_START_HOUR) / totalHours * chartHeight).coerceIn(0f, chartHeight)
                    val wakeY = ((week.avgWakeHour - CHART_START_HOUR) / totalHours * chartHeight).coerceIn(0f, chartHeight)

                    if (wakeY > sleepY) {
                        drawRoundRect(
                            color = primaryColor.copy(alpha = 0.8f),
                            topLeft = Offset(x, sleepY),
                            size = Size(barWidth, wakeY - sleepY),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                monthlyData.forEach { week ->
                    Text(
                        text = week.weekLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurfaceVariantColor,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun YearSleepChart(
    data: List<SleepRecordEntity>,
    page: Int,
    primaryColor: Color,
    onSurfaceVariantColor: Color
) {
    val calendar = Calendar.getInstance()

    // 按月分组计算平均睡眠时间
    val monthlyData = (1..12).map { month ->
        val monthRecords = data.filter { record ->
            calendar.timeInMillis = record.date
            calendar.get(Calendar.MONTH) + 1 == month
        }

        val avgSleepHour = if (monthRecords.isNotEmpty()) {
            monthRecords.map { record ->
                val cal = Calendar.getInstance()
                cal.timeInMillis = record.sleepTime
                convertToChartHour(
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    isWakeTime = false
                )
            }.average().toFloat()
        } else 23f

        val avgWakeHour = if (monthRecords.isNotEmpty()) {
            monthRecords.map { record ->
                val cal = Calendar.getInstance()
                cal.timeInMillis = record.wakeTime
                convertToChartHour(
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    isWakeTime = true
                )
            }.average().toFloat()
        } else 31f // 默认07:00，即31

        MonthlySleepChartData("${month}月", month, avgSleepHour, avgWakeHour)
    }

    // 根据 page 选择显示的月份
    val displayData = if (page == 0) monthlyData.take(6) else monthlyData.takeLast(6)

    val totalHours = CHART_END_HOUR - CHART_START_HOUR

    val timePoints = listOf(
        22f to "22:00",
        24f to "00:00",
        26f to "02:00",
        28f to "04:00",
        30f to "06:00",
        32f to "08:00",
        34f to "10:00",
        36f to "12:00"
    )

    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .width(40.dp)
                .height(200.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            timePoints.forEach { (_, label) ->
                Text(label, style = MaterialTheme.typography.labelSmall, color = onSurfaceVariantColor, maxLines = 1)
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val chartHeight = size.height
                val chartWidth = size.width
                val barCount = displayData.size
                val totalSpacing = chartWidth * 0.3f
                val totalBarWidth = chartWidth - totalSpacing
                val barWidth = totalBarWidth / barCount
                val spacing = totalSpacing / (barCount + 1)

                // 绘制虚线
                timePoints.forEach { (hourContinuous, _) ->
                    val lineY = (hourContinuous - CHART_START_HOUR) / totalHours * chartHeight
                    drawLine(
                        color = onSurfaceVariantColor,
                        start = Offset(0f, lineY),
                        end = Offset(chartWidth, lineY),
                        strokeWidth = 0.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                    )
                }

                // 绘制柱状图
                displayData.forEachIndexed { index, month ->
                    val x = spacing + index * (barWidth + spacing)
                    val sleepY = ((month.avgSleepHour - CHART_START_HOUR) / totalHours * chartHeight).coerceIn(0f, chartHeight)
                    val wakeY = ((month.avgWakeHour - CHART_START_HOUR) / totalHours * chartHeight).coerceIn(0f, chartHeight)

                    if (wakeY > sleepY) {
                        drawRoundRect(
                            color = primaryColor.copy(alpha = 0.8f),
                            topLeft = Offset(x, sleepY),
                            size = Size(barWidth, wakeY - sleepY),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                displayData.forEach { month ->
                    Text(
                        text = month.monthLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurfaceVariantColor,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

private data class WeeklySleepChartData(
    val weekLabel: String,
    val avgSleepHour: Float,
    val avgWakeHour: Float
)

private data class MonthlySleepChartData(
    val monthLabel: String,
    val monthIndex: Int,
    val avgSleepHour: Float,
    val avgWakeHour: Float
)

private fun formatSleepDuration(durationMinutes: Long): String {
    val hours = durationMinutes / 60
    val minutes = durationMinutes % 60
    return if (hours > 0) "${hours}h${minutes}m" else "${minutes}m"
}