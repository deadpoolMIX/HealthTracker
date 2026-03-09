package com.example.healthtracker.ui.screens.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.healthtracker.data.local.entity.BodyRecordEntity
import java.util.Calendar
import kotlin.math.abs

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
                            selected = uiState.filterMode == 1,
                            onClick = { viewModel.setFilterMode(1) },
                            label = { Text("周") }
                        )
                        FilterChip(
                            selected = uiState.filterMode == 0,
                            onClick = { viewModel.setFilterMode(0) },
                            label = { Text("自定义时间段") }
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
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "前一组")
                            }
                            Text(
                                text = "显示 ${uiState.weeklyData.size} 周数据",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            IconButton(onClick = { viewModel.navigateWeek(1) }) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "后一组")
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
                    val statistics = viewModel.getStatistics()
                    DataSummaryCard(
                        dataType = uiState.selectedDataType,
                        statistics = statistics
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
                text = "${dataLabel}趋势",
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
                    // 动态自适应缩放：以数据范围为中心，添加5%边距
                    val dataMin = values.minOrNull() ?: 0.0
                    val dataMax = values.maxOrNull() ?: 100.0
                    val dataRange = (dataMax - dataMin).coerceAtLeast(0.1) // 最小范围防止除零

                    // 使用数据范围的10%作为上下边距，确保波动清晰可见
                    val padding = dataRange * 0.1
                    val minVal = dataMin - padding
                    val maxVal = dataMax + padding
                    val range = (maxVal - minVal).coerceAtLeast(0.1)

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
                                .padding(start = 40.dp, top = 10.dp, end = 10.dp, bottom = 30.dp)
                        ) {
                            val chartWidth = size.width
                            val chartHeight = size.height

                            // 绘制Y轴虚线参考线（更淡的透明度）
                            val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                            repeat(5) { i ->
                                val y = (i * chartHeight / 4)
                                drawLine(
                                    color = Color.Gray.copy(alpha = 0.15f),
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

                                // 绘制数据点（带发光效果）
                                points.forEach { point ->
                                    // 外圈光晕
                                    drawCircle(
                                        color = lineColor.copy(alpha = 0.2f),
                                        radius = 8f,
                                        center = point
                                    )
                                    // 内圈实心点
                                    drawCircle(
                                        color = lineColor,
                                        radius = 4f,
                                        center = point
                                    )
                                    // 白色中心高光
                                    drawCircle(
                                        color = Color.White.copy(alpha = 0.6f),
                                        radius = 1.5f,
                                        center = point
                                    )
                                }
                            } else if (chartPoints.size == 1) {
                                // 只有一个数据点
                                val point = chartPoints.first()
                                val x = chartWidth / 2
                                val y = chartHeight - ((point.second - minVal) / range * chartHeight).toFloat()
                                // 外圈光晕
                                drawCircle(
                                    color = lineColor.copy(alpha = 0.2f),
                                    radius = 10f,
                                    center = Offset(x, y)
                                )
                                drawCircle(
                                    color = lineColor,
                                    radius = 6f,
                                    center = Offset(x, y)
                                )
                            }
                        }

                        // Y轴标签（更紧凑）
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(top = 10.dp, bottom = 30.dp)
                                .width(40.dp)
                                .height(180.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            yLabels.forEach { label ->
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 9.sp,
                                    maxLines = 1,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // X轴标签
                        if (xLabels.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(start = 40.dp, end = 10.dp)
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

    // 点击的数据点索引
    var selectedPointIndex by remember { mutableStateOf(-1) }

    // 详情弹窗
    if (selectedPointIndex >= 0 && selectedPointIndex < weeklyData.size) {
        val selectedWeek = weeklyData[selectedPointIndex]
        val cal = Calendar.getInstance()
        cal.timeInMillis = selectedWeek.startDate

        val (medianValue, avgValue) = when (dataType) {
            0 -> selectedWeek.medianWeight to selectedWeek.avgWeight
            1 -> selectedWeek.medianBodyFat to selectedWeek.avgBodyFat
            2 -> selectedWeek.medianMuscle to selectedWeek.avgMuscle
            else -> null to null
        }

        AlertDialog(
            onDismissRequest = { selectedPointIndex = -1 },
            title = { Text("第${selectedWeek.weekOfYear}周", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "中位数",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (medianValue != null) "${String.format("%.1f", medianValue)} $unit" else "--",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = lineColor
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "平均数",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (avgValue != null) "${String.format("%.1f", avgValue)} $unit" else "--",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedPointIndex = -1 }) {
                    Text("关闭")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${dataLabel}趋势（周中位数）",
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
                    // 动态自适应缩放：以数据范围为中心，添加10%边距
                    val dataMin = values.minOrNull() ?: 0.0
                    val dataMax = values.maxOrNull() ?: 100.0
                    val dataRange = (dataMax - dataMin).coerceAtLeast(0.1)

                    val padding = dataRange * 0.1
                    val minVal = dataMin - padding
                    val maxVal = dataMax + padding
                    val range = (maxVal - minVal).coerceAtLeast(0.1)

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
                                .padding(start = 40.dp, top = 10.dp, end = 10.dp, bottom = 30.dp)
                                .pointerInput(weeklyData) {
                                    detectTapGestures { offset ->
                                        // 计算点击位置对应的数据点
                                        val chartWidth = size.width
                                        val chartHeight = size.height
                                        val paddingStart = 40.dp.toPx()

                                        // 检查点击是否在图表区域内
                                        if (offset.x >= paddingStart && values.size >= 1) {
                                            val adjustedX = offset.x - paddingStart
                                            val pointSpacing: Float = if (values.size > 1) chartWidth.toFloat() / (values.size - 1) else 0f

                                            values.forEachIndexed { index, _ ->
                                                val pointX: Float = if (values.size > 1) {
                                                    index * pointSpacing
                                                } else {
                                                    chartWidth / 2f
                                                }
                                                // 检查是否点击在数据点附近（30px范围内）
                                                if (abs(adjustedX - pointX) < 30f) {
                                                    selectedPointIndex = index
                                                    return@detectTapGestures
                                                }
                                            }
                                        }
                                    }
                                }
                        ) {
                            val chartWidth = size.width
                            val chartHeight = size.height

                            // 绘制Y轴虚线参考线（更淡的透明度）
                            val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                            repeat(5) { i ->
                                val y = (i * chartHeight / 4)
                                drawLine(
                                    color = Color.Gray.copy(alpha = 0.15f),
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

                                // 绘制数据点（带发光效果）
                                points.forEach { point ->
                                    // 外圈光晕
                                    drawCircle(
                                        color = lineColor.copy(alpha = 0.2f),
                                        radius = 8f,
                                        center = point
                                    )
                                    // 内圈实心点
                                    drawCircle(
                                        color = lineColor,
                                        radius = 4f,
                                        center = point
                                    )
                                    // 白色中心高光
                                    drawCircle(
                                        color = Color.White.copy(alpha = 0.6f),
                                        radius = 1.5f,
                                        center = point
                                    )
                                }
                            } else if (values.size == 1) {
                                val x = chartWidth / 2
                                val y = chartHeight - ((values[0] - minVal) / range * chartHeight).toFloat()
                                drawCircle(
                                    color = lineColor.copy(alpha = 0.2f),
                                    radius = 10f,
                                    center = Offset(x, y)
                                )
                                drawCircle(
                                    color = lineColor,
                                    radius = 6f,
                                    center = Offset(x, y)
                                )
                            }
                        }

                        // Y轴标签（更紧凑）
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(top = 10.dp, bottom = 30.dp)
                                .width(40.dp)
                                .height(180.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            yLabels.forEach { label ->
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 9.sp,
                                    maxLines = 1,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // X轴标签（周数）
                        if (weeklyData.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(start = 40.dp, end = 10.dp)
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
                }
            }
        }
    }
}

@Composable
private fun DataSummaryCard(
    dataType: Int,
    statistics: StatisticsData
) {
    val (dataLabel, unit) = when (dataType) {
        0 -> "体重" to "kg"
        1 -> "体脂" to "%"
        2 -> "肌肉" to "kg"
        else -> "体重" to "kg"
    }

    val isPositive = statistics.change.startsWith("+")
    val isNegative = statistics.change.startsWith("-")
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
                .padding(16.dp)
        ) {
            Text(
                text = "数据总结",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 第一行：变化和平均
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 变化
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = statistics.change,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = changeColor
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = unit,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "变化",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 平均
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = if (statistics.avg != null) String.format("%.1f", statistics.avg) else "--",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = unit,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "平均",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 第二行：最高和最低
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 最高
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = if (statistics.max != null) String.format("%.1f", statistics.max) else "--",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = unit,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "最高",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 最低
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = if (statistics.min != null) String.format("%.1f", statistics.min) else "--",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = unit,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "最低",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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