package com.example.healthtracker.ui.screens.reports

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.healthtracker.data.local.entity.BodyRecordEntity
import com.example.healthtracker.data.local.entity.SleepRecordEntity
import com.example.healthtracker.ui.theme.NutrientColors
import java.util.Calendar
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel(),
    onNavigateToDataExport: () -> Unit,
    onNavigateToNutritionDetail: () -> Unit = {},
    onNavigateToBodyDataDetail: () -> Unit = {},
    onNavigateToSleepDetail: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // 报表设置对话框
    if (uiState.showSettingsDialog) {
        ReportSettingsDialog(
            showNutritionChart = uiState.showNutritionChart,
            showBodyChart = uiState.showBodyChart,
            showSleepChart = uiState.showSleepChart,
            defaultChartPeriod = uiState.defaultChartPeriod,
            onDismiss = { viewModel.hideSettingsDialog() },
            onSave = { showNutrition, showBody, showSleep, defaultPeriod ->
                viewModel.updateReportSettings(showNutrition, showBody, showSleep, defaultPeriod)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("数据报表", fontWeight = FontWeight.Medium) },
                windowInsets = WindowInsets(0, 0, 0, 0),
                actions = {
                    // 上周/上周切换按钮
                    IconButton(
                        onClick = { viewModel.setPeriodOffset(uiState.periodOffset + 1) },
                        enabled = uiState.periodOffset < 12
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "上一周期")
                    }
                    Text(
                        text = viewModel.getPeriodLabel(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    IconButton(
                        onClick = { viewModel.setPeriodOffset(uiState.periodOffset - 1) },
                        enabled = uiState.periodOffset > 0
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "下一周期")
                    }
                }
            )
        }
    ) { paddingValues ->
        // 只在首次加载且无数据时显示骨架屏
        if (uiState.isLoading && uiState.intakeData.isEmpty() && uiState.bodyData.isEmpty() && uiState.sleepData.isEmpty()) {
            // 骨架屏加载
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 周期选择骨架
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(2) {
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .width(60.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        )
                    }
                }
                // 图表卡片骨架
                repeat(3) {
                    ShimmerChartCard()
                }
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
                        text = "请先记录数据",
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
                // 营养素堆叠柱状图
                if (uiState.showNutritionChart) {
                    item {
                        NutritionChartCard(
                            data = uiState.intakeData,
                            targetCalories = uiState.targetCalories,
                            period = uiState.selectedPeriod,
                            onClick = onNavigateToNutritionDetail
                        )
                    }
                }

                // 身体数据折线图
                if (uiState.showBodyChart) {
                    item {
                        BodyDataChartCard(
                            data = uiState.bodyData,
                            weeklyData = uiState.weeklyBodyData,
                            period = uiState.selectedPeriod,
                            weekDates = uiState.weekDates,
                            onClick = onNavigateToBodyDataDetail
                        )
                    }
                }

                // 睡眠范围条形图
                if (uiState.showSleepChart) {
                    item {
                        SleepChartCard(
                            data = uiState.sleepData,
                            weeklyData = uiState.weeklySleepData,
                            period = uiState.selectedPeriod,
                            weekDates = uiState.weekDates,
                            avgSleepTime = viewModel.getAverageSleepTime(),
                            avgWakeTime = viewModel.getAverageWakeTime(),
                            avgDuration = viewModel.getAverageSleepDuration(),
                            onClick = onNavigateToSleepDetail
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

/**
 * 报表设置对话框
 */
@Composable
private fun ReportSettingsDialog(
    showNutritionChart: Boolean,
    showBodyChart: Boolean,
    showSleepChart: Boolean,
    defaultChartPeriod: Int,
    onDismiss: () -> Unit,
    onSave: (Boolean, Boolean, Boolean, Int) -> Unit
) {
    var showNutrition by remember { mutableStateOf(showNutritionChart) }
    var showBody by remember { mutableStateOf(showBodyChart) }
    var showSleep by remember { mutableStateOf(showSleepChart) }
    var selectedPeriod by remember { mutableIntStateOf(defaultChartPeriod) }
    val periods = listOf("天", "周", "月", "年")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("报表设置") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "显示报表",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showNutrition = !showNutrition },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = showNutrition,
                        onCheckedChange = { showNutrition = it }
                    )
                    Text("营养素图表")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showBody = !showBody },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = showBody,
                        onCheckedChange = { showBody = it }
                    )
                    Text("身体数据图表")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSleep = !showSleep },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = showSleep,
                        onCheckedChange = { showSleep = it }
                    )
                    Text("睡眠图表")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "默认周期",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    periods.forEachIndexed { index, period ->
                        FilterChip(
                            selected = selectedPeriod == index,
                            onClick = { selectedPeriod = index },
                            label = { Text(period) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(showNutrition, showBody, showSleep, selectedPeriod) }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 营养素柱状图卡片 (与子页面同步)
 */
@Composable
private fun NutritionChartCard(
    data: List<DailyNutritionData>,
    targetCalories: Float,
    period: Int,
    onClick: () -> Unit = {}
) {
    var selectedItem by remember { mutableStateOf<DailyNutritionData?>(null) }
    
    // Popup
    selectedItem?.let { dayData ->
        AlertDialog(
            onDismissRequest = { selectedItem = null },
            title = {
                val cal = Calendar.getInstance().apply { timeInMillis = dayData.timestamp }
                val year = cal.get(Calendar.YEAR)
                val month = cal.get(Calendar.MONTH) + 1
                val day = cal.get(Calendar.DAY_OF_MONTH)
                Text("${year}年${month}月${day}日", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("热量: ${dayData.calories.toInt()} kcal", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    Text("碳水: ${String.format(Locale.getDefault(), "%.1f", dayData.carbs)} g", color = NutrientColors.Carbs)
                    Text("蛋白质: ${String.format(Locale.getDefault(), "%.1f", dayData.protein)} g", color = NutrientColors.Protein)
                    Text("脂肪: ${String.format(Locale.getDefault(), "%.1f", dayData.fat)} g", color = NutrientColors.Fat)
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedItem = null }) {
                    Text("关闭")
                }
            }
        )
    }

    val carbsColor = NutrientColors.Carbs
    val proteinColor = NutrientColors.Protein
    val fatColor = NutrientColors.Fat
    val targetColor = Color.Red
    val axisColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val textColor = MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 标题行
            Text(
                text = "营养素摄入",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

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
                // 统一显示Canvas柱状图，与子页面保持"一模一样"
                val textMeasurer = rememberTextMeasurer()
                val barCount = data.size
                
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .pointerInput(data) {
                            detectTapGestures { offset ->
                                val width = size.width
                                val yAxisWidth = 40.dp.toPx()
                                val chartWidth = width - yAxisWidth
                                val barWidth = chartWidth / (barCount * 2f)
                                val spacing = chartWidth / barCount.toFloat()

                                val x = offset.x
                                if (x > yAxisWidth) {
                                    val index = ((x - yAxisWidth) / spacing).toInt()
                                    if (index in data.indices) {
                                        val barCenterX = yAxisWidth + index * spacing + spacing / 2f
                                        if (kotlin.math.abs(x - barCenterX) <= barWidth) {
                                            selectedItem = data[index]
                                        }
                                    }
                                }
                            }
                        }
                ) {
                    val maxCals = maxOf(
                        targetCalories,
                        data.maxOfOrNull { it.calories } ?: 0f
                    ) * 1.2f
                    
                    val maxAxisValue = if (maxCals > 0) maxCals else 2000f

                    val yAxisWidth = 40.dp.toPx()
                    val xAxisHeight = 24.dp.toPx()
                    val chartHeight = size.height - xAxisHeight
                    val chartWidth = size.width - yAxisWidth

                    // Draw Y Axis and horizontal lines
                    val ySteps = 4
                    for (i in 0..ySteps) {
                        val yVal = maxAxisValue * i / ySteps
                        val yPos = chartHeight - (chartHeight * i / ySteps)
                        
                        if (yVal > 0) {
                            drawLine(
                                color = axisColor.copy(alpha = 0.2f),
                                start = Offset(yAxisWidth, yPos),
                                end = Offset(size.width, yPos),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        drawText(
                            textMeasurer = textMeasurer,
                            text = yVal.toInt().toString(),
                            topLeft = Offset(0f, yPos - 8.dp.toPx()),
                            style = TextStyle(color = axisColor, fontSize = 10.sp, textAlign = TextAlign.End)
                        )
                    }

                    // Draw target red line
                    if (targetCalories > 0) {
                        val targetY = chartHeight - (chartHeight * targetCalories / maxAxisValue)
                        drawLine(
                            color = targetColor,
                            start = Offset(yAxisWidth, targetY),
                            end = Offset(size.width, targetY),
                            strokeWidth = 2.dp.toPx()
                        )
                    }

                    // Draw bars and X labels
                    val spacing = chartWidth / barCount.toFloat()
                    val barWidth = chartWidth / (barCount * 2f)

                    data.forEachIndexed { index, day ->
                        val xCenter = yAxisWidth + index * spacing + spacing / 2f
                        val barLeft = xCenter - barWidth / 2f

                        // Draw X Label
                        val labelResult = textMeasurer.measure(
                            day.dateLabel,
                            style = TextStyle(color = textColor, fontSize = 12.sp)
                        )
                        drawText(
                            textLayoutResult = labelResult,
                            topLeft = Offset(xCenter - labelResult.size.width / 2f, chartHeight + 8.dp.toPx())
                        )

                        if (day.calories > 0) {
                            val totalMacros = day.carbs + day.protein + day.fat
                            val barTotalHeight = chartHeight * (day.calories / maxAxisValue)
                            val barTop = chartHeight - barTotalHeight

                            if (totalMacros > 0) {
                                val carbsHeight = barTotalHeight * (day.carbs / totalMacros)
                                val proteinHeight = barTotalHeight * (day.protein / totalMacros)
                                val fatHeight = barTotalHeight * (day.fat / totalMacros)

                                var currentY = chartHeight
                                // Draw Carbs (bottom)
                                drawRect(
                                    color = carbsColor,
                                    topLeft = Offset(barLeft, currentY - carbsHeight),
                                    size = Size(barWidth, carbsHeight)
                                )
                                currentY -= carbsHeight

                                // Draw Protein (middle)
                                drawRect(
                                    color = proteinColor,
                                    topLeft = Offset(barLeft, currentY - proteinHeight),
                                    size = Size(barWidth, proteinHeight)
                                )
                                currentY -= proteinHeight

                                // Draw Fat (top)
                                drawRect(
                                    color = fatColor,
                                    topLeft = Offset(barLeft, currentY - fatHeight),
                                    size = Size(barWidth, fatHeight)
                                )
                            } else {
                                // Only calories
                                drawRect(
                                    color = Color.Gray,
                                    topLeft = Offset(barLeft, barTop),
                                    size = Size(barWidth, barTotalHeight)
                                )
                            }

                            // Top calories text
                            val calResult = textMeasurer.measure(
                                day.calories.toInt().toString(),
                                style = TextStyle(color = textColor, fontSize = 10.sp)
                            )
                            drawText(
                                textLayoutResult = calResult,
                                topLeft = Offset(xCenter - calResult.size.width / 2f, barTop - calResult.size.height - 2.dp.toPx())
                            )
                        }
                    }
                }
                
                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem("碳水", carbsColor)
                    Spacer(Modifier.width(16.dp))
                    LegendItem("蛋白质", proteinColor)
                    Spacer(Modifier.width(16.dp))
                    LegendItem("脂肪", fatColor)
                    Spacer(Modifier.width(16.dp))
                    LegendItem("目标热量", targetColor, isLine = true)
                }
            }
        }
    }
}

@Composable
private fun StatItemCompact(
    label: String,
    value: String,
    unit: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun LegendItem(text: String, color: Color, isLine: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isLine) {
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(2.dp)
                    .background(color)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )
        }
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
    weeklyData: List<Float?>,
    period: Int,
    weekDates: List<Long> = emptyList(),
    onClick: () -> Unit = {}
) {
    var selectedDataType by remember { mutableIntStateOf(0) }
    val lineColor = when (selectedDataType) {
        0 -> MaterialTheme.colorScheme.primary
        1 -> MaterialTheme.colorScheme.error
        2 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    val (dataLabel, unit) = when (selectedDataType) {
        0 -> "体重" to "kg"
        1 -> "体脂" to "%"
        2 -> "肌肉" to "kg"
        else -> "体重" to "kg"
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("身体数据趋势", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("体重", "体脂", "肌肉").forEachIndexed { index, label ->
                    FilterChip(
                        selected = selectedDataType == index,
                        onClick = { selectedDataType = index },
                        label = { Text(label, fontSize = 12.sp) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            val axisColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            val textColor = MaterialTheme.colorScheme.onSurface

            // 统一数据源：周模式取 7 天，月模式取 4 周
            val chartPoints = if (period == 0) {
                val dataByDate: Map<Long, BodyRecordEntity> = data.associateBy {
                    Calendar.getInstance().apply { 
                        timeInMillis = it.date
                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                }
                weekDates.map { date ->
                    val record = dataByDate[date]
                    when (selectedDataType) {
                        0 -> record?.weight?.toFloat()
                        1 -> record?.bodyFatRate?.toFloat()
                        2 -> record?.muscleMass?.toFloat()
                        else -> null
                    }
                }
            } else {
                weeklyData
            }

            val xLabels = if (period == 0) {
                weekDates.map {
                    val cal = Calendar.getInstance().apply { timeInMillis = it }
                    "${cal.get(Calendar.MONTH) + 1}.${cal.get(Calendar.DAY_OF_MONTH)}"
                }
            } else {
                (0 until weeklyData.size).map { "W${it + 1}" }
            }

            if (chartPoints.all { it == null }) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("暂无数据", color = axisColor)
                }
            } else {
                val validValues = chartPoints.filterNotNull()
                val minVal = (validValues.minOrNull() ?: 0f) * 0.95f
                val maxVal = (validValues.maxOrNull() ?: 100f) * 1.05f
                val range = if (maxVal - minVal == 0f) 10f else maxVal - minVal

                Row(modifier = Modifier.fillMaxWidth()) {
                    // Y Axis (40dp)
                    Column(modifier = Modifier.width(40.dp).height(200.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        repeat(5) { i ->
                            val yVal = maxVal - (range * i / 4)
                            Text(String.format("%.1f", yVal), style = TextStyle(color = axisColor, fontSize = 10.sp), textAlign = TextAlign.End, maxLines = 1)
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                            val chartHeight = size.height
                            val chartWidth = size.width
                            val stepX = chartWidth / (chartPoints.size.toFloat())
                            
                            // Background lines
                            repeat(5) { i ->
                                val y = i * chartHeight / 4
                                drawLine(color = axisColor.copy(alpha = 0.1f), start = Offset(0f, y), end = Offset(chartWidth, y), strokeWidth = 1.dp.toPx())
                            }

                            val points = mutableListOf<Offset>()
                            chartPoints.forEachIndexed { index, value ->
                                val x = index * stepX + stepX / 2f
                                if (value != null) {
                                    val y = chartHeight - ((value - minVal) / range * chartHeight)
                                    points.add(Offset(x, y))
                                    drawCircle(color = lineColor, radius = 4.dp.toPx(), center = Offset(x, y))
                                }
                            }

                            if (points.size >= 2) {
                                val path = Path().apply {
                                    moveTo(points.first().x, points.first().y)
                                    for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
                                }
                                drawPath(path, color = lineColor, style = Stroke(width = 2.dp.toPx()))
                            }
                        }
                        // X Axis Labels
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            xLabels.forEach { label ->
                                Text(label, modifier = Modifier.weight(1f), style = TextStyle(color = textColor, fontSize = 10.sp), textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }
    }
}

// 睡眠范围条形图卡片
@Composable
private fun SleepChartCard(
    data: List<SleepRecordEntity>,
    weeklyData: List<WeeklySleepTime?>,
    period: Int,
    weekDates: List<Long> = emptyList(),
    avgSleepTime: String,
    avgWakeTime: String,
    avgDuration: Long,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("睡眠记录", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem("平均入睡", avgSleepTime)
                StatItem("平均起床", avgWakeTime)
                StatItem("平均时长", formatDuration(avgDuration))
            }
            Spacer(modifier = Modifier.height(16.dp))

            val primaryColor = MaterialTheme.colorScheme.primary
            val axisColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            val textColor = MaterialTheme.colorScheme.onSurface
            val startHour = 22
            val endHour = 36
            val totalHours = endHour - startHour

            val timePoints = listOf(22 to "22:00", 24 to "00:00", 26 to "02:00", 28 to "04:00", 30 to "06:00", 32 to "08:00", 34 to "10:00", 36 to "12:00")

            Row(modifier = Modifier.fillMaxWidth()) {
                // Y Axis (40dp)
                Column(modifier = Modifier.width(40.dp).height(200.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    timePoints.forEach { (_, label) ->
                        Text(label, style = TextStyle(color = axisColor, fontSize = 10.sp), maxLines = 1)
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                        val chartHeight = size.height
                        val chartWidth = size.width
                        val itemCount = if (period == 0) 7 else weeklyData.size
                        val stepX = chartWidth / itemCount
                        val barWidth = stepX * 0.6f

                        // Grid lines
                        timePoints.forEach { (h, _) ->
                            val y = (h - startHour).toFloat() / totalHours * chartHeight
                            drawLine(color = axisColor.copy(alpha = 0.1f), start = Offset(0f, y), end = Offset(chartWidth, y), strokeWidth = 0.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
                        }

                        if (period == 0) {
                            val dataByDate: Map<Long, SleepRecordEntity> = data.associateBy {
                                Calendar.getInstance().apply { 
                                    timeInMillis = it.date
                                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                                }.timeInMillis
                            }
                            weekDates.forEachIndexed { index, date ->
                                dataByDate[date]?.let { sleep ->
                                    val calS = Calendar.getInstance().apply { timeInMillis = sleep.sleepTime }
                                    var sH = calS.get(Calendar.HOUR_OF_DAY) + calS.get(Calendar.MINUTE) / 60f
                                    if (sH < 18) sH += 24f // Handle midnight cross
                                    
                                    val calW = Calendar.getInstance().apply { timeInMillis = sleep.wakeTime }
                                    var wH = calW.get(Calendar.HOUR_OF_DAY) + calW.get(Calendar.MINUTE) / 60f
                                    if (wH < 18 && sH >= 24f) wH += 24f

                                    val x = index * stepX + (stepX - barWidth) / 2f
                                    val yS = ((sH - startHour) / totalHours * chartHeight).coerceIn(0f, chartHeight)
                                    val yW = ((wH - startHour) / totalHours * chartHeight).coerceIn(0f, chartHeight)
                                    if (yW > yS) {
                                        drawRoundRect(color = primaryColor.copy(alpha = 0.7f), topLeft = Offset(x, yS), size = Size(barWidth, yW - yS), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()))
                                    }
                                }
                            }
                        } else {
                            weeklyData.forEachIndexed { index, week ->
                                week?.let {
                                    val x = index * stepX + (stepX - barWidth) / 2f
                                    val yS = ((it.avgSleepHour - startHour) / totalHours * chartHeight).coerceIn(0f, chartHeight)
                                    var wH = it.avgWakeHour
                                    if (wH < 18) wH += 24f
                                    val yW = ((wH - startHour) / totalHours * chartHeight).coerceIn(0f, chartHeight)
                                    if (yW > yS) {
                                        drawRoundRect(color = primaryColor.copy(alpha = 0.7f), topLeft = Offset(x, yS), size = Size(barWidth, yW - yS), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()))
                                    }
                                }
                            }
                        }
                    }
                    // X Axis Labels
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        val labels = if (period == 0) weekDates.map { val cal = Calendar.getInstance().apply { timeInMillis = it }; "${cal.get(Calendar.MONTH) + 1}.${cal.get(Calendar.DAY_OF_MONTH)}" } else (1..weeklyData.size).map { "W$it" }
                        labels.forEach { label ->
                            Text(label, modifier = Modifier.weight(1f), style = TextStyle(color = textColor, fontSize = 10.sp), textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatDuration(minutes: Long): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return if (hours > 0) "${hours}h${mins}m" else "${mins}m"
}

/**
 * 骨架屏卡片
 */
@Composable
private fun ShimmerChartCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
            )
        }
    }
}