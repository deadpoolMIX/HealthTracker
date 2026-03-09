package com.example.healthtracker.ui.screens.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyDataDetailScreen(
    viewModel: BodyDataDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // 日期选择对话框
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    if (showStartPicker) {
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            onDateSelected = { timestamp ->
                viewModel.setDateRange(timestamp, uiState.endDate)
                showStartPicker = false
            },
            initialDate = uiState.startDate
        )
    }

    if (showEndPicker) {
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            onDateSelected = { timestamp ->
                viewModel.setDateRange(uiState.startDate, timestamp)
                showEndPicker = false
            },
            initialDate = uiState.endDate
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("身体数据趋势", fontWeight = FontWeight.Medium) },
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
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 数据类型选择（单选）
                item {
                    DataTypeSelector(
                        selectedType = uiState.selectedDataType,
                        onTypeSelected = { viewModel.setSelectedDataType(it) }
                    )
                }

                // 筛选模式切换
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.filterMode == 0,
                            onClick = { viewModel.setFilterMode(0) },
                            label = { Text("自定义时间段") }
                        )
                        FilterChip(
                            selected = uiState.filterMode == 1,
                            onClick = { viewModel.setFilterMode(1) },
                            label = { Text("周") }
                        )
                    }
                }

                // 自定义时间范围选择
                if (uiState.filterMode == 0) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DateSelector(
                                label = "开始",
                                date = uiState.startDate,
                                onClick = { showStartPicker = true },
                                modifier = Modifier.weight(1f)
                            )
                            DateSelector(
                                label = "结束",
                                date = uiState.endDate,
                                onClick = { showEndPicker = true },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 周模式导航
                if (uiState.filterMode == 1) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.navigateWeek(-1) }) {
                                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "前一组")
                            }
                            Text(
                                text = "显示 ${uiState.weeklyData.size} 周数据",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            IconButton(onClick = { viewModel.navigateWeek(1) }) {
                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "后一组")
                            }
                        }
                    }
                }

                // 折线图
                item {
                    if (uiState.filterMode == 0) {
                        BodyTrendChart(
                            data = uiState.rawData,
                            dataType = uiState.selectedDataType
                        )
                    } else {
                        WeeklyBodyTrendChart(
                            weeklyData = uiState.weeklyData,
                            dataType = uiState.selectedDataType
                        )
                    }
                }

                // 数据变化卡片
                item {
                    DataChangeCard(
                        dataType = uiState.selectedDataType,
                        changeValue = viewModel.getChangeValue()
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun DataTypeSelector(
    selectedType: Int,
    onTypeSelected: (Int) -> Unit
) {
    val types = listOf("体重", "体脂", "肌肉")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        types.forEachIndexed { index, label ->
            FilterChip(
                selected = selectedType == index,
                onClick = { onTypeSelected(index) },
                label = { Text(label) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DateSelector(
    label: String,
    date: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cal = Calendar.getInstance()
    cal.timeInMillis = date

    OutlinedCard(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${cal.get(Calendar.YEAR)}年${cal.get(Calendar.MONTH) + 1}月${cal.get(Calendar.DAY_OF_MONTH)}日",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 身体数据趋势图表 - 自定义时间段模式
 */
@Composable
private fun BodyTrendChart(
    data: List<BodyRecordEntity>,
    dataType: Int
) {
    val lineColor = MaterialTheme.colorScheme.primary

    val (dataLabel, unit) = when (dataType) {
        0 -> "体重" to "kg"
        1 -> "体脂" to "%"
        2 -> "肌肉" to "kg"
        else -> "体重" to "kg"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "$dataLabel趋势",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (data.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                // 提取数据值
                val values = data.mapNotNull { entity ->
                    when (dataType) {
                        0 -> entity.weight
                        1 -> entity.bodyFatRate
                        2 -> entity.muscleMass
                        else -> entity.weight
                    }
                }

                if (values.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无${dataLabel}数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    // 计算数据范围（添加10%的边距）
                    val minVal = (values.minOrNull() ?: 0.0) * 0.9
                    val maxVal = (values.maxOrNull() ?: 100.0) * 1.1
                    val range = (maxVal - minVal).coerceAtLeast(1.0)

                    // 生成图表数据点
                    val chartPoints = values.mapIndexedNotNull { index, value ->
                        val entity = data.getOrNull(index) ?: return@mapIndexedNotNull null
                        val actualValue = when (dataType) {
                            0 -> entity.weight
                            1 -> entity.bodyFatRate
                            2 -> entity.muscleMass
                            else -> entity.weight
                        }
                        actualValue?.let { index to it }
                    }

                    // Y轴刻度（5个刻度）
                    val yLabels = (0..4).map { i ->
                        val value = maxVal - (range * i / 4)
                        String.format("%.1f", value)
                    }

                    // X轴标签（智能选择显示）
                    val xLabels = remember(data) {
                        generateXLabels(data)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 50.dp, top = 10.dp, end = 10.dp, bottom = 30.dp)
                        ) {
                            val chartWidth = size.width
                            val chartHeight = size.height

                            // 绘制Y轴虚线参考线
                            val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                            repeat(5) { i ->
                                val y = (i * chartHeight / 4)
                                drawLine(
                                    color = Color.Gray.copy(alpha = 0.3f),
                                    start = Offset(0f, y),
                                    end = Offset(chartWidth, y),
                                    strokeWidth = 1f,
                                    pathEffect = dashPathEffect
                                )
                            }

                            // 绘制折线
                            if (chartPoints.size >= 2) {
                                val points = chartPoints.map { (index, value) ->
                                    val x = if (values.size > 1) {
                                        (index.toFloat() / (values.size - 1)) * chartWidth
                                    } else {
                                        chartWidth / 2
                                    }
                                    val y = chartHeight - ((value - minVal) / range * chartHeight).toFloat()
                                    Offset(x, y)
                                }

                                // 绘制平滑曲线
                                val path = Path()
                                path.moveTo(points[0].x, points[0].y)

                                for (i in 1 until points.size) {
                                    val prev = points[i - 1]
                                    val curr = points[i]
                                    val midX = (prev.x + curr.x) / 2

                                    path.cubicTo(
                                        midX, prev.y,
                                        midX, curr.y,
                                        curr.x, curr.y
                                    )
                                }

                                drawPath(
                                    path = path,
                                    color = lineColor,
                                    style = Stroke(width = 2.5f)
                                )

                                // 绘制数据点
                                points.forEach { point ->
                                    drawCircle(
                                        color = lineColor,
                                        radius = 4f,
                                        center = point
                                    )
                                }
                            } else if (chartPoints.size == 1) {
                                // 只有一个数据点
                                val point = chartPoints.first()
                                val x = chartWidth / 2
                                val y = chartHeight - ((point.second - minVal) / range * chartHeight).toFloat()
                                drawCircle(
                                    color = lineColor,
                                    radius = 6f,
                                    center = Offset(x, y)
                                )
                            }
                        }

                        // Y轴标签
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(top = 10.dp, bottom = 30.dp)
                                .width(50.dp)
                                .height(180.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            yLabels.forEach { label ->
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // X轴标签
                        if (xLabels.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(start = 50.dp, end = 10.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                xLabels.forEach { label ->
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    // 单位标签
                    Text(
                        text = "单位: $unit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

/**
 * 生成智能X轴标签
 */
private fun generateXLabels(data: List<BodyRecordEntity>): List<String> {
    if (data.isEmpty()) return emptyList()

    val cal = Calendar.getInstance()

    if (data.size <= 7) {
        // 7天或更少，显示所有日期
        return data.map { entity ->
            cal.timeInMillis = entity.date
            "${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.DAY_OF_MONTH)}"
        }
    }

    // 数据量较大，选择3个关键日期显示
    val first = data.first()
    val middle = data[data.size / 2]
    val last = data.last()

    return listOf(
        formatDateLabel(first.date),
        formatDateLabel(middle.date),
        formatDateLabel(last.date)
    )
}

private fun formatDateLabel(timestamp: Long): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = timestamp
    return "${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.DAY_OF_MONTH)}"
}

/**
 * 身体数据趋势图表 - 周模式
 */
@Composable
private fun WeeklyBodyTrendChart(
    weeklyData: List<WeeklyBodyData>,
    dataType: Int
) {
    val lineColor = MaterialTheme.colorScheme.primary

    val (dataLabel, unit) = when (dataType) {
        0 -> "体重" to "kg"
        1 -> "体脂" to "%"
        2 -> "肌肉" to "kg"
        else -> "体重" to "kg"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "$dataLabel趋势（周中位数）",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (weeklyData.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                // 提取数据值
                val values = weeklyData.mapNotNull { week ->
                    when (dataType) {
                        0 -> week.medianWeight
                        1 -> week.medianBodyFat
                        2 -> week.medianMuscle
                        else -> week.medianWeight
                    }
                }

                if (values.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无${dataLabel}数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    // 计算数据范围
                    val minVal = (values.minOrNull() ?: 0.0) * 0.9
                    val maxVal = (values.maxOrNull() ?: 100.0) * 1.1
                    val range = (maxVal - minVal).coerceAtLeast(1.0)

                    // Y轴刻度
                    val yLabels = (0..4).map { i ->
                        val value = maxVal - (range * i / 4)
                        String.format("%.1f", value)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 50.dp, top = 10.dp, end = 10.dp, bottom = 30.dp)
                        ) {
                            val chartWidth = size.width
                            val chartHeight = size.height

                            // 绘制Y轴虚线参考线
                            val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                            repeat(5) { i ->
                                val y = (i * chartHeight / 4)
                                drawLine(
                                    color = Color.Gray.copy(alpha = 0.3f),
                                    start = Offset(0f, y),
                                    end = Offset(chartWidth, y),
                                    strokeWidth = 1f,
                                    pathEffect = dashPathEffect
                                )
                            }

                            // 绘制折线
                            if (values.size >= 2) {
                                val points = values.mapIndexed { index, value ->
                                    val x = if (values.size > 1) {
                                        (index.toFloat() / (values.size - 1)) * chartWidth
                                    } else {
                                        chartWidth / 2
                                    }
                                    val y = chartHeight - ((value - minVal) / range * chartHeight).toFloat()
                                    Offset(x, y)
                                }

                                // 绘制平滑曲线
                                val path = Path()
                                path.moveTo(points[0].x, points[0].y)

                                for (i in 1 until points.size) {
                                    val prev = points[i - 1]
                                    val curr = points[i]
                                    val midX = (prev.x + curr.x) / 2

                                    path.cubicTo(
                                        midX, prev.y,
                                        midX, curr.y,
                                        curr.x, curr.y
                                    )
                                }

                                drawPath(
                                    path = path,
                                    color = lineColor,
                                    style = Stroke(width = 2.5f)
                                )

                                // 绘制数据点
                                points.forEach { point ->
                                    drawCircle(
                                        color = lineColor,
                                        radius = 4f,
                                        center = point
                                    )
                                }
                            } else if (values.size == 1) {
                                val x = chartWidth / 2
                                val y = chartHeight - ((values[0] - minVal) / range * chartHeight).toFloat()
                                drawCircle(
                                    color = lineColor,
                                    radius = 6f,
                                    center = Offset(x, y)
                                )
                            }
                        }

                        // Y轴标签
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(top = 10.dp, bottom = 30.dp)
                                .width(50.dp)
                                .height(180.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            yLabels.forEach { label ->
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // X轴标签（周数）
                        if (weeklyData.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(start = 50.dp, end = 10.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                weeklyData.forEach { week ->
                                    Text(
                                        text = "W${week.weekOfYear}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = "单位: $unit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
private fun DataChangeCard(
    dataType: Int,
    changeValue: String
) {
    val (dataLabel, unit) = when (dataType) {
        0 -> "体重" to "kg"
        1 -> "体脂" to "%"
        2 -> "肌肉" to "kg"
        else -> "体重" to "kg"
    }

    val isPositive = changeValue.startsWith("+")
    val isNegative = changeValue.startsWith("-")
    val changeColor = when {
        isNegative -> MaterialTheme.colorScheme.primary // 下降用主色
        isPositive -> MaterialTheme.colorScheme.error // 上升用错误色
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$dataLabel变化",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = changeValue,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = changeColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (Long) -> Unit,
    initialDate: Long
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
    )

    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("取消")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}