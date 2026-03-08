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
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.sleepData.isEmpty()) {
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
 * 将入睡时间转换为图表坐标
 * 入睡时间范围：18:00 - 次日06:00 映射到 22:00 - 次日12:00 图表范围
 */
private fun convertSleepHourToChart(hour: Float, minute: Float): Float {
    val h = hour + minute / 60f
    // 如果是凌晨时间(0-12点)，视为次日
    return if (h < 12f) h + 24f else h
}

/**
 * 将起床时间转换为图表坐标
 * 起床时间范围：06:00 - 14:00 映射到图表范围
 */
private fun convertWakeHourToChart(hour: Float, minute: Float): Float {
    return hour + minute / 60f
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

    // 时间范围：22:00 到 12:00（次日中午）
    val startHour = 22f
    val endHour = 36f // 12:00 + 24
    val totalHours = endHour - startHour

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
                    val lineY = (hourContinuous - startHour) / totalHours * chartHeight
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
                    val sleepHourContinuous = convertSleepHourToChart(
                        calSleep.get(Calendar.HOUR_OF_DAY).toFloat(),
                        calSleep.get(Calendar.MINUTE).toFloat()
                    )

                    val calWake = Calendar.getInstance()
                    calWake.timeInMillis = sleep.wakeTime
                    var wakeHourContinuous = convertWakeHourToChart(
                        calWake.get(Calendar.HOUR_OF_DAY).toFloat(),
                        calWake.get(Calendar.MINUTE).toFloat()
                    )
                    // 如果起床时间小于入睡时间（跨天），加24小时
                    if (wakeHourContinuous < sleepHourContinuous - 12f) {
                        wakeHourContinuous += 24f
                    }

                    // 限制在图表范围内
                    val clampedSleepHour = sleepHourContinuous.coerceIn(startHour, endHour)
                    val clampedWakeHour = wakeHourContinuous.coerceIn(startHour, endHour)

                    val x = spacing + index * (barWidth + spacing)
                    val sleepY = ((clampedSleepHour - startHour) / totalHours * chartHeight).coerceIn(0f, chartHeight)
                    val wakeY = ((clampedWakeHour - startHour) / totalHours * chartHeight).coerceIn(0f, chartHeight)

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
    // 计算四周的数据
    val calendar = Calendar.getInstance()
    val now = System.currentTimeMillis()
    calendar.timeInMillis = now
    calendar.add(Calendar.WEEK_OF_YEAR, -(3 + periodOffset * 4))
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    val weeklyData = mutableListOf<WeeklySleepChartData>()
    val startHour = 22f
    val endHour = 36f

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
                convertSleepHourToChart(
                    cal.get(Calendar.HOUR_OF_DAY).toFloat(),
                    cal.get(Calendar.MINUTE).toFloat()
                )
            }.average().toFloat()
        } else 23f

        val avgWakeHour = if (weekRecords.isNotEmpty()) {
            weekRecords.map { record ->
                val cal = Calendar.getInstance()
                cal.timeInMillis = record.wakeTime
                convertWakeHourToChart(
                    cal.get(Calendar.HOUR_OF_DAY).toFloat(),
                    cal.get(Calendar.MINUTE).toFloat()
                )
            }.average().toFloat()
        } else 7f

        weeklyData.add(WeeklySleepChartData("第${i + 1}周", avgSleepHour, avgWakeHour))

        calendar.add(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
    }

    if (weeklyData.isEmpty()) {
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

    val totalHours = endHour - startHour

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
                val barCount = weeklyData.size
                val totalSpacing = chartWidth * 0.4f
                val totalBarWidth = chartWidth - totalSpacing
                val barWidth = totalBarWidth / barCount
                val spacing = totalSpacing / (barCount + 1)

                // 绘制虚线
                timePoints.forEach { (hourContinuous, _) ->
                    val lineY = (hourContinuous - startHour) / totalHours * chartHeight
                    drawLine(
                        color = onSurfaceVariantColor,
                        start = Offset(0f, lineY),
                        end = Offset(chartWidth, lineY),
                        strokeWidth = 0.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                    )
                }

                // 绘制柱状图
                weeklyData.forEachIndexed { index, week ->
                    // 限制在图表范围内
                    val clampedSleepHour = week.avgSleepHour.coerceIn(startHour, endHour)
                    val clampedWakeHour = week.avgWakeHour.coerceIn(startHour, endHour)

                    val x = spacing + index * (barWidth + spacing)
                    val sleepY = ((clampedSleepHour - startHour) / totalHours * chartHeight).coerceIn(0f, chartHeight)
                    val wakeY = ((clampedWakeHour - startHour) / totalHours * chartHeight).coerceIn(0f, chartHeight)

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
                weeklyData.forEach { week ->
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

        val startHour = 22f
        val endHour = 36f

        val avgSleepHour = if (monthRecords.isNotEmpty()) {
            monthRecords.map { record ->
                val cal = Calendar.getInstance()
                cal.timeInMillis = record.sleepTime
                convertSleepHourToChart(
                    cal.get(Calendar.HOUR_OF_DAY).toFloat(),
                    cal.get(Calendar.MINUTE).toFloat()
                )
            }.average().toFloat()
        } else 23f

        val avgWakeHour = if (monthRecords.isNotEmpty()) {
            monthRecords.map { record ->
                val cal = Calendar.getInstance()
                cal.timeInMillis = record.wakeTime
                convertWakeHourToChart(
                    cal.get(Calendar.HOUR_OF_DAY).toFloat(),
                    cal.get(Calendar.MINUTE).toFloat()
                )
            }.average().toFloat()
        } else 7f

        MonthlySleepChartData("${month}月", month, avgSleepHour, avgWakeHour)
    }

    // 根据 page 选择显示的月份
    val displayData = if (page == 0) monthlyData.take(6) else monthlyData.takeLast(6)

    val startHour = 22f
    val endHour = 36f
    val totalHours = endHour - startHour

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
                    val lineY = (hourContinuous - startHour) / totalHours * chartHeight
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
                    // 限制在图表范围内
                    val clampedSleepHour = month.avgSleepHour.coerceIn(startHour, endHour)
                    val clampedWakeHour = month.avgWakeHour.coerceIn(startHour, endHour)

                    val x = spacing + index * (barWidth + spacing)
                    val sleepY = ((clampedSleepHour - startHour) / totalHours * chartHeight).coerceIn(0f, chartHeight)
                    val wakeY = ((clampedWakeHour - startHour) / totalHours * chartHeight).coerceIn(0f, chartHeight)

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

private fun formatSleepDuration(durationMs: Long): String {
    val hours = durationMs / 3600000
    val minutes = (durationMs % 3600000) / 60000
    return if (hours > 0) "${hours}h${minutes}m" else "${minutes}m"
}