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
                },
                actions = {
                    // 周期切换按钮
                    val prevLabel = viewModel.getPreviousPeriodLabel()
                    val nextLabel = viewModel.getNextPeriodLabel()

                    if (uiState.periodOffset > 0 && prevLabel.isNotEmpty()) {
                        TextButton(onClick = { viewModel.setPeriodOffset(uiState.periodOffset - 1) }) {
                            Text(prevLabel, fontSize = 12.sp)
                        }
                    }
                    if (nextLabel.isNotEmpty()) {
                        TextButton(onClick = { viewModel.setPeriodOffset(uiState.periodOffset + 1) }) {
                            Text(nextLabel, fontSize = 12.sp)
                        }
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
                // 周期选择
                item {
                    PeriodSelector(
                        selectedPeriod = uiState.selectedPeriod,
                        onPeriodSelected = { viewModel.setPeriod(it) }
                    )
                }

                // 睡眠图表
                item {
                    SleepChartCard(
                        sleepData = uiState.sleepData,
                        period = uiState.selectedPeriod,
                        yearPage = uiState.yearPage,
                        onYearPageChange = { viewModel.setYearPage(it) }
                    )
                }

                // 睡眠总结
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
private fun PeriodSelector(
    selectedPeriod: Int,
    onPeriodSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("周", "月", "年").forEachIndexed { index, label ->
            FilterChip(
                selected = selectedPeriod == index,
                onClick = { onPeriodSelected(index) },
                label = { Text(label) },
                modifier = Modifier.weight(1f)
            )
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
                1 -> MonthSleepChart(data = sleepData, primaryColor = primaryColor, onSurfaceVariantColor = onSurfaceVariantColor)
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

    val startHour = 22
    val endHour = 36
    val totalHours = endHour - startHour

    val timePoints = listOf(
        22 to "22:00",
        24 to "00:00",
        26 to "02:00",
        28 to "04:00",
        30 to "06:00",
        32 to "08:00",
        34 to "10:00",
        36 to "12:00"
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

                timePoints.forEach { (hourContinuous, _) ->
                    val lineY = (hourContinuous.toFloat() - startHour.toFloat()) / totalHours.toFloat() * chartHeight
                    drawLine(
                        color = onSurfaceVariantColor,
                        start = Offset(0f, lineY),
                        end = Offset(chartWidth, lineY),
                        strokeWidth = 0.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                    )
                }

                sortedData.forEachIndexed { index, sleep ->
                    val calSleep = Calendar.getInstance()
                    calSleep.timeInMillis = sleep.sleepTime
                    var sleepHourContinuous = calSleep.get(Calendar.HOUR_OF_DAY) + calSleep.get(Calendar.MINUTE) / 60f
                    if (sleepHourContinuous < startHour) sleepHourContinuous += 24

                    val calWake = Calendar.getInstance()
                    calWake.timeInMillis = sleep.wakeTime
                    var wakeHourContinuous = calWake.get(Calendar.HOUR_OF_DAY) + calWake.get(Calendar.MINUTE) / 60f
                    if (wakeHourContinuous < startHour) wakeHourContinuous += 24
                    if (wakeHourContinuous > endHour) wakeHourContinuous = endHour.toFloat()

                    val x = spacing + index * (barWidth + spacing)
                    val sleepY = (sleepHourContinuous - startHour) / totalHours * chartHeight
                    val wakeY = (wakeHourContinuous - startHour) / totalHours * chartHeight

                    drawRoundRect(
                        color = primaryColor.copy(alpha = 0.8f),
                        topLeft = Offset(x, sleepY),
                        size = Size(barWidth, wakeY - sleepY),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
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
    primaryColor: Color,
    onSurfaceVariantColor: Color
) {
    val calendar = Calendar.getInstance()
    val now = System.currentTimeMillis()
    calendar.timeInMillis = now
    calendar.add(Calendar.WEEK_OF_YEAR, -3)
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    val weeklyData = mutableListOf<WeeklySleepChartData>()
    val startHour = 22
    val endHour = 36

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
                var h = cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60f
                if (h < startHour) h += 24
                h
            }.average().toFloat()
        } else 23f

        val avgWakeHour = if (weekRecords.isNotEmpty()) {
            weekRecords.map { record ->
                val cal = Calendar.getInstance()
                cal.timeInMillis = record.wakeTime
                cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60f
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
        22 to "22:00",
        24 to "00:00",
        26 to "02:00",
        28 to "04:00",
        30 to "06:00",
        32 to "08:00",
        34 to "10:00",
        36 to "12:00"
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

                timePoints.forEach { (hourContinuous, _) ->
                    val lineY = (hourContinuous.toFloat() - startHour.toFloat()) / totalHours.toFloat() * chartHeight
                    drawLine(
                        color = onSurfaceVariantColor,
                        start = Offset(0f, lineY),
                        end = Offset(chartWidth, lineY),
                        strokeWidth = 0.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                    )
                }

                weeklyData.forEachIndexed { index, week ->
                    val x = spacing + index * (barWidth + spacing)
                    val sleepY = (week.avgSleepHour - startHour) / totalHours * chartHeight
                    val wakeY = (week.avgWakeHour - startHour) / totalHours * chartHeight

                    drawRoundRect(
                        color = primaryColor.copy(alpha = 0.8f),
                        topLeft = Offset(x, sleepY),
                        size = Size(barWidth, wakeY - sleepY),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
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

        val startHour = 22
        val avgSleepHour = if (monthRecords.isNotEmpty()) {
            monthRecords.map { record ->
                val cal = Calendar.getInstance()
                cal.timeInMillis = record.sleepTime
                var h = cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60f
                if (h < startHour) h += 24
                h
            }.average().toFloat()
        } else 23f

        val avgWakeHour = if (monthRecords.isNotEmpty()) {
            monthRecords.map { record ->
                val cal = Calendar.getInstance()
                cal.timeInMillis = record.wakeTime
                cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60f
            }.average().toFloat()
        } else 7f

        MonthlySleepChartData("${month}月", month, avgSleepHour, avgWakeHour)
    }

    // 根据 page 选择显示的月份
    val displayData = if (page == 0) monthlyData.take(6) else monthlyData.takeLast(6)

    val startHour = 22
    val endHour = 36
    val totalHours = endHour - startHour

    val timePoints = listOf(
        22 to "22:00",
        24 to "00:00",
        26 to "02:00",
        28 to "04:00",
        30 to "06:00",
        32 to "08:00",
        34 to "10:00",
        36 to "12:00"
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

                timePoints.forEach { (hourContinuous, _) ->
                    val lineY = (hourContinuous.toFloat() - startHour.toFloat()) / totalHours.toFloat() * chartHeight
                    drawLine(
                        color = onSurfaceVariantColor,
                        start = Offset(0f, lineY),
                        end = Offset(chartWidth, lineY),
                        strokeWidth = 0.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                    )
                }

                displayData.forEachIndexed { index, month ->
                    val x = spacing + index * (barWidth + spacing)
                    val sleepY = (month.avgSleepHour - startHour) / totalHours * chartHeight
                    val wakeY = (month.avgWakeHour - startHour) / totalHours * chartHeight

                    drawRoundRect(
                        color = primaryColor.copy(alpha = 0.8f),
                        topLeft = Offset(x, sleepY),
                        size = Size(barWidth, wakeY - sleepY),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
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