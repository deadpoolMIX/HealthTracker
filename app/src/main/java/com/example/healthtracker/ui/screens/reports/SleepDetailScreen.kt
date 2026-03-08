package com.example.healthtracker.ui.screens.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
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
                    yearPage = uiState.yearPage,
                    onYearPageChange = { viewModel.setYearPage(it) },
                    periodLabel = viewModel.getPeriodLabel()
                )
            }

            // 睡眠总结（基于当前数据）
            item {
                SleepSummaryCard(
                    sleepData = uiState.sleepData
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
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
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
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
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
    sleepData: List<SleepRecordEntity>
) {
    // 直接在这里计算数据，确保使用最新的sleepData
    val avgSleepTime = remember(sleepData) {
        if (sleepData.isEmpty()) "--:--"
        else {
            val avgMinutes = sleepData.map {
                val cal = Calendar.getInstance()
                cal.timeInMillis = it.sleepTime
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                val minute = cal.get(Calendar.MINUTE)
                if (hour < 12) (hour + 24) * 60 + minute else hour * 60 + minute
            }.average().toInt()
            val actualMinutes = avgMinutes % (24 * 60)
            String.format("%02d:%02d", actualMinutes / 60, actualMinutes % 60)
        }
    }

    val avgWakeTime = remember(sleepData) {
        if (sleepData.isEmpty()) "--:--"
        else {
            val avgMinutes = sleepData.map {
                val cal = Calendar.getInstance()
                cal.timeInMillis = it.wakeTime
                cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
            }.average().toInt()
            String.format("%02d:%02d", avgMinutes / 60, avgMinutes % 60)
        }
    }

    val avgDuration = remember(sleepData) {
        if (sleepData.isEmpty()) 0L
        else sleepData.map { it.duration }.average().toLong()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "睡眠总结",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))

            // 统计项采用更清晰的布局
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 平均入睡
                SummaryItem(
                    label = "平均入睡",
                    value = avgSleepTime,
                    modifier = Modifier.weight(1f)
                )

                // 分隔线
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(48.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                // 平均起床
                SummaryItem(
                    label = "平均起床",
                    value = avgWakeTime,
                    modifier = Modifier.weight(1f)
                )

                // 分隔线
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(48.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                // 平均时长
                SummaryItem(
                    label = "平均时长",
                    value = formatSleepDuration(avgDuration),
                    modifier = Modifier.weight(1f),
                    valueFontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueFontSize: TextUnit = TextUnit.Unspecified
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = if (valueFontSize == TextUnit.Unspecified) MaterialTheme.typography.headlineSmall
                    else MaterialTheme.typography.headlineSmall.copy(fontSize = valueFontSize),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
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
    onYearPageChange: (Int) -> Unit,
    periodLabel: String
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

                Text(
                    text = periodLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = onSurfaceVariantColor
                )
            }

            // 年模式时显示翻页箭头
            if (period == 2) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onYearPageChange(0) },
                        enabled = yearPage == 1
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
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
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "下一页",
                            tint = if (yearPage == 1) onSurfaceVariantColor.copy(alpha = 0.3f) else primaryColor
                        )
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

private const val CHART_START_HOUR = 22f
private const val CHART_END_HOUR = 36f

private fun convertToChartHour(hour: Int, minute: Int): Float {
    val h = hour + minute / 60f
    return when {
        h < 12f -> h + 24f
        h < 22f -> 22f
        else -> h
    }
}

private fun formatHourToTime(chartHour: Float): String {
    val actualHour = if (chartHour >= 24f) chartHour - 24f else chartHour
    val hours = actualHour.toInt()
    val minutes = ((actualHour - hours) * 60).toInt()
    return String.format("%02d:%02d", hours, minutes)
}

@Composable
private fun WeekSleepChart(
    data: List<SleepRecordEntity>,
    primaryColor: Color,
    onSurfaceVariantColor: Color
) {
    val sortedData = data.sortedBy { it.date }.takeLast(7)

    // 点击选中的索引
    var selectedIndex by remember { mutableIntStateOf(-1) }

    // 详情弹窗
    if (selectedIndex >= 0 && selectedIndex < sortedData.size) {
        val selectedRecord = sortedData[selectedIndex]
        val cal = Calendar.getInstance()
        cal.timeInMillis = selectedRecord.date
        val dateStr = "${cal.get(Calendar.MONTH) + 1}月${cal.get(Calendar.DAY_OF_MONTH)}日"

        AlertDialog(
            onDismissRequest = { selectedIndex = -1 },
            title = { Text(dateStr, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val sleepCal = Calendar.getInstance().apply { timeInMillis = selectedRecord.sleepTime }
                    val wakeCal = Calendar.getInstance().apply { timeInMillis = selectedRecord.wakeTime }
                    val sleepTimeStr = String.format("%02d:%02d", sleepCal.get(Calendar.HOUR_OF_DAY), sleepCal.get(Calendar.MINUTE))
                    val wakeTimeStr = String.format("%02d:%02d", wakeCal.get(Calendar.HOUR_OF_DAY), wakeCal.get(Calendar.MINUTE))

                    SleepDetailRow("入睡时间", sleepTimeStr)
                    SleepDetailRow("起床时间", wakeTimeStr)
                    SleepDetailRow("睡眠时长", formatSleepDuration(selectedRecord.duration))
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedIndex = -1 }) {
                    Text("关闭")
                }
            }
        )
    }

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
                    .clip(RoundedCornerShape(8.dp))
                    .pointerInput(sortedData) {
                        detectTapGestures { offset ->
                            val barCount = sortedData.size
                            val totalSpacing = size.width * 0.25f
                            val totalBarWidth = size.width - totalSpacing
                            val barWidth = totalBarWidth / barCount.toFloat()
                            val spacing = totalSpacing / (barCount + 1)

                            sortedData.forEachIndexed { index, _ ->
                                val barStart = spacing + index * (barWidth + spacing)
                                val barEnd = barStart + barWidth
                                if (offset.x >= barStart && offset.x <= barEnd && offset.y >= 0f && offset.y <= size.height) {
                                    selectedIndex = index
                                    return@detectTapGestures
                                }
                            }
                        }
                    }
            ) {
                val chartHeight = size.height
                val chartWidth = size.width
                val barCount = sortedData.size
                val totalSpacing = chartWidth * 0.25f
                val totalBarWidth = chartWidth - totalSpacing
                val barWidth = totalBarWidth / barCount.toFloat()
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
                        calSleep.get(Calendar.MINUTE)
                    )

                    val calWake = Calendar.getInstance()
                    calWake.timeInMillis = sleep.wakeTime
                    val wakeHourChart = convertToChartHour(
                        calWake.get(Calendar.HOUR_OF_DAY),
                        calWake.get(Calendar.MINUTE)
                    )

                    val x = spacing + index * (barWidth + spacing)
                    val sleepY = ((sleepHourChart - CHART_START_HOUR) / totalHours * chartHeight).coerceIn(0f, chartHeight)
                    val wakeY = ((wakeHourChart - CHART_START_HOUR) / totalHours * chartHeight).coerceIn(0f, chartHeight)

                    if (wakeY > sleepY) {
                        drawRoundRect(
                            color = if (selectedIndex == index) primaryColor else primaryColor.copy(alpha = 0.8f),
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
    primaryColor: Color,
    onSurfaceVariantColor: Color
) {
    val calendar = Calendar.getInstance()

    // 按固定日期区间划分为4周
    val weeklyGroups = mutableMapOf<Int, MutableList<SleepRecordEntity>>()

    data.forEach { record ->
        calendar.timeInMillis = record.date
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        if (dayOfMonth <= 28) {
            val weekNum = when {
                dayOfMonth <= 7 -> 1
                dayOfMonth <= 14 -> 2
                dayOfMonth <= 21 -> 3
                else -> 4
            }
            if (!weeklyGroups.containsKey(weekNum)) {
                weeklyGroups[weekNum] = mutableListOf()
            }
            weeklyGroups[weekNum]?.add(record)
        }
    }

    val weeklyData = weeklyGroups.keys.sorted().mapNotNull { weekNum ->
        val records = weeklyGroups[weekNum] ?: return@mapNotNull null

        val avgSleepHour = records.map { record ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = record.sleepTime
            convertToChartHour(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
        }.average().toFloat()

        val avgWakeHour = records.map { record ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = record.wakeTime
            convertToChartHour(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
        }.average().toFloat()

        val avgDuration = records.map { it.duration }.average().toLong()

        WeeklySleepChartData("第${weekNum}周", weekNum, avgSleepHour, avgWakeHour, avgDuration, records)
    }

    // 点击选中的索引
    var selectedIndex by remember { mutableIntStateOf(-1) }

    // 详情弹窗
    if (selectedIndex >= 0 && selectedIndex < weeklyData.size) {
        val selectedWeek = weeklyData[selectedIndex]

        AlertDialog(
            onDismissRequest = { selectedIndex = -1 },
            title = { Text(selectedWeek.weekLabel, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SleepDetailRow("平均入睡", formatHourToTime(selectedWeek.avgSleepHour))
                    SleepDetailRow("平均起床", formatHourToTime(selectedWeek.avgWakeHour))
                    SleepDetailRow("平均时长", formatSleepDuration(selectedWeek.avgDuration))
                    SleepDetailRow("记录天数", "${selectedWeek.records.size}天")
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedIndex = -1 }) {
                    Text("关闭")
                }
            }
        )
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
                    .clip(RoundedCornerShape(8.dp))
                    .pointerInput(weeklyData) {
                        detectTapGestures { offset ->
                            val barCount = weeklyData.size
                            val totalSpacing = size.width * 0.4f
                            val totalBarWidth = size.width - totalSpacing
                            val barWidth = totalBarWidth / barCount.toFloat()
                            val spacing = totalSpacing / (barCount + 1)

                            weeklyData.forEachIndexed { index, _ ->
                                val barStart = spacing + index * (barWidth + spacing)
                                val barEnd = barStart + barWidth
                                if (offset.x >= barStart && offset.x <= barEnd && offset.y >= 0f && offset.y <= size.height) {
                                    selectedIndex = index
                                    return@detectTapGestures
                                }
                            }
                        }
                    }
            ) {
                val chartHeight = size.height
                val chartWidth = size.width
                val barCount = weeklyData.size
                val totalSpacing = chartWidth * 0.4f
                val totalBarWidth = chartWidth - totalSpacing
                val barWidth = totalBarWidth / barCount.toFloat()
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
                weeklyData.forEachIndexed { index, week ->
                    val x = spacing + index * (barWidth + spacing)
                    val sleepY = ((week.avgSleepHour - CHART_START_HOUR) / totalHours * chartHeight).coerceIn(0f, chartHeight)
                    val wakeY = ((week.avgWakeHour - CHART_START_HOUR) / totalHours * chartHeight).coerceIn(0f, chartHeight)

                    if (wakeY > sleepY) {
                        drawRoundRect(
                            color = if (selectedIndex == index) primaryColor else primaryColor.copy(alpha = 0.8f),
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

    val monthlyGroups = mutableMapOf<Int, MutableList<SleepRecordEntity>>()

    data.forEach { record ->
        calendar.timeInMillis = record.date
        val month = calendar.get(Calendar.MONTH) + 1
        if (!monthlyGroups.containsKey(month)) {
            monthlyGroups[month] = mutableListOf()
        }
        monthlyGroups[month]?.add(record)
    }

    val monthlyData = monthlyGroups.keys.sorted().mapNotNull { month ->
        val records = monthlyGroups[month] ?: return@mapNotNull null

        val avgSleepHour = records.map { record ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = record.sleepTime
            convertToChartHour(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
        }.average().toFloat()

        val avgWakeHour = records.map { record ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = record.wakeTime
            convertToChartHour(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
        }.average().toFloat()

        val avgDuration = records.map { it.duration }.average().toLong()

        MonthlySleepChartData("${month}月", month, avgSleepHour, avgWakeHour, avgDuration, records)
    }

    val displayData = if (page == 0) {
        monthlyData.filter { it.monthIndex <= 6 }
    } else {
        monthlyData.filter { it.monthIndex > 6 }
    }

    // 点击选中的索引
    var selectedIndex by remember { mutableIntStateOf(-1) }

    // 详情弹窗
    if (selectedIndex >= 0 && selectedIndex < displayData.size) {
        val selectedMonth = displayData[selectedIndex]

        AlertDialog(
            onDismissRequest = { selectedIndex = -1 },
            title = { Text(selectedMonth.monthLabel, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SleepDetailRow("平均入睡", formatHourToTime(selectedMonth.avgSleepHour))
                    SleepDetailRow("平均起床", formatHourToTime(selectedMonth.avgWakeHour))
                    SleepDetailRow("平均时长", formatSleepDuration(selectedMonth.avgDuration))
                    SleepDetailRow("记录天数", "${selectedMonth.records.size}天")
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedIndex = -1 }) {
                    Text("关闭")
                }
            }
        )
    }

    if (displayData.isEmpty()) {
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
                    .clip(RoundedCornerShape(8.dp))
                    .pointerInput(displayData) {
                        detectTapGestures { offset ->
                            val barCount = displayData.size
                            val totalSpacing = size.width * 0.3f
                            val totalBarWidth = size.width - totalSpacing
                            val barWidth = totalBarWidth / barCount.toFloat()
                            val spacing = totalSpacing / (barCount + 1)

                            displayData.forEachIndexed { index, _ ->
                                val barStart = spacing + index * (barWidth + spacing)
                                val barEnd = barStart + barWidth
                                if (offset.x >= barStart && offset.x <= barEnd && offset.y >= 0f && offset.y <= size.height) {
                                    selectedIndex = index
                                    return@detectTapGestures
                                }
                            }
                        }
                    }
            ) {
                val chartHeight = size.height
                val chartWidth = size.width
                val barCount = displayData.size
                val totalSpacing = chartWidth * 0.3f
                val totalBarWidth = chartWidth - totalSpacing
                val barWidth = totalBarWidth / barCount.toFloat()
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
                            color = if (selectedIndex == index) primaryColor else primaryColor.copy(alpha = 0.8f),
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

@Composable
private fun SleepDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

private data class WeeklySleepChartData(
    val weekLabel: String,
    val weekNum: Int,
    val avgSleepHour: Float,
    val avgWakeHour: Float,
    val avgDuration: Long,
    val records: List<SleepRecordEntity>
)

private data class MonthlySleepChartData(
    val monthLabel: String,
    val monthIndex: Int,
    val avgSleepHour: Float,
    val avgWakeHour: Float,
    val avgDuration: Long,
    val records: List<SleepRecordEntity>
)

private fun formatSleepDuration(durationMinutes: Long): String {
    if (durationMinutes <= 0) return "0分钟"
    val hours = durationMinutes / 60
    val minutes = durationMinutes % 60
    return if (hours > 0) "${hours}小时${minutes}分钟" else "${minutes}分钟"
}