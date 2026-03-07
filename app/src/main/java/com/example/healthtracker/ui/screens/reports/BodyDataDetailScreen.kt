package com.example.healthtracker.ui.screens.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.healthtracker.util.DateTimeUtils
import java.util.Calendar
import java.util.Locale

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
                },
                actions = {
                    // 数据类型切换按钮
                    TextButton(onClick = { viewModel.setDataType(if (uiState.dataType == 0) 1 else 0) }) {
                        Text(
                            text = if (uiState.dataType == 0) "三围" else "体重体脂",
                            fontSize = 13.sp
                        )
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
                // 筛选模式切换
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.filterMode == 0,
                            onClick = { viewModel.setFilterMode(0) },
                            label = { Text("自定义时间") }
                        )
                        FilterChip(
                            selected = uiState.filterMode == 1,
                            onClick = { viewModel.setFilterMode(1) },
                            label = { Text("以周为点") }
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

                // 折线图区块
                item {
                    if (uiState.filterMode == 0) {
                        BodyLineChartCard(
                            data = uiState.rawData,
                            dataType = uiState.dataType
                        )
                    } else {
                        WeeklyBodyLineChartCard(
                            data = uiState.weeklyData,
                            dataType = uiState.dataType
                        )
                    }
                }

                // 数据变化总结区块
                item {
                    if (uiState.dataType == 0) {
                        BodyChangeCard(
                            weightChange = viewModel.getChangeData().first,
                            bodyFatChange = viewModel.getChangeData().second,
                            muscleChange = viewModel.getChangeData().third
                        )
                    } else {
                        MeasurementsChangeCard(
                            chestChange = viewModel.getMeasurementsChangeData().first,
                            waistChange = viewModel.getMeasurementsChangeData().second,
                            hipChange = viewModel.getMeasurementsChangeData().third
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
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

@Composable
private fun BodyLineChartCard(
    data: List<BodyRecordEntity>,
    dataType: Int
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    val (line1Label, line2Label, line3Label) = if (dataType == 0) {
        Triple("体重", "体脂", "肌肉")
    } else {
        Triple("胸围", "腰围", "臀围")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "数据趋势",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 图例
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LegendItem(line1Label, primaryColor)
                LegendItem(line2Label, secondaryColor)
                LegendItem(line3Label, tertiaryColor)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (data.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                // 计算数据范围
                val values1 = if (dataType == 0) data.mapNotNull { it.weight } else data.mapNotNull { it.chest }
                val values2 = if (dataType == 0) data.mapNotNull { it.bodyFatRate } else data.mapNotNull { it.waist }
                val values3 = if (dataType == 0) data.mapNotNull { it.muscleMass } else data.mapNotNull { it.hip }

                val max1 = (values1.maxOrNull() ?: 100.0).toFloat().coerceAtLeast(1f)
                val max2 = (values2.maxOrNull() ?: 100.0).toFloat().coerceAtLeast(1f)
                val max3 = (values3.maxOrNull() ?: 100.0).toFloat().coerceAtLeast(1f)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    val chartHeight = size.height - 20f
                    val pointSpacing = size.width / (data.size - 1).coerceAtLeast(1)

                    // 绘制三条折线
                    drawSmoothLine(values1, max1, chartHeight, pointSpacing, primaryColor)
                    drawSmoothLine(values2, max2, chartHeight, pointSpacing, secondaryColor)
                    drawSmoothLine(values3, max3, chartHeight, pointSpacing, tertiaryColor)
                }
            }
        }
    }
}

@Composable
private fun WeeklyBodyLineChartCard(
    data: List<WeeklyBodyData>,
    dataType: Int
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    val (line1Label, line2Label, line3Label) = if (dataType == 0) {
        Triple("体重", "体脂", "肌肉")
    } else {
        Triple("胸围", "腰围", "臀围")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "数据趋势（周中位数）",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 图例
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LegendItem(line1Label, primaryColor)
                LegendItem(line2Label, secondaryColor)
                LegendItem(line3Label, tertiaryColor)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (data.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                val values1 = if (dataType == 0) data.mapNotNull { it.medianWeight } else data.mapNotNull { it.medianChest }
                val values2 = if (dataType == 0) data.mapNotNull { it.medianBodyFat } else data.mapNotNull { it.medianWaist }
                val values3 = if (dataType == 0) data.mapNotNull { it.medianMuscle } else data.mapNotNull { it.medianHip }

                val max1 = (values1.maxOrNull() ?: 100.0).toFloat().coerceAtLeast(1f)
                val max2 = (values2.maxOrNull() ?: 100.0).toFloat().coerceAtLeast(1f)
                val max3 = (values3.maxOrNull() ?: 100.0).toFloat().coerceAtLeast(1f)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    val chartHeight = size.height - 20f
                    val pointSpacing = size.width / (data.size - 1).coerceAtLeast(1)

                    drawSmoothLine(values1, max1, chartHeight, pointSpacing, primaryColor)
                    drawSmoothLine(values2, max2, chartHeight, pointSpacing, secondaryColor)
                    drawSmoothLine(values3, max3, chartHeight, pointSpacing, tertiaryColor)
                }

                // X轴标签
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    data.forEach { week ->
                        Text(
                            text = "第${week.weekOfYear}周",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSmoothLine(
    values: List<Double>,
    maxValue: Float,
    chartHeight: Float,
    pointSpacing: Float,
    color: Color
) {
    if (values.size < 2) return

    val path = Path()
    val points = values.mapIndexed { index, value ->
        val x = index * pointSpacing
        val y = chartHeight - (value.toFloat() / maxValue * chartHeight)
        Offset(x, y)
    }

    // 使用贝塞尔曲线绘制平滑曲线
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
        color = color,
        style = Stroke(width = 2.5f)
    )

    // 绘制数据点
    points.forEach { point ->
        drawCircle(
            color = color,
            radius = 4f,
            center = point
        )
    }
}

@Composable
private fun BodyChangeCard(
    weightChange: String,
    bodyFatChange: String,
    muscleChange: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "数据变化",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ChangeItem(label = "体重", change = weightChange, unit = "kg")
                ChangeItem(label = "体脂", change = bodyFatChange, unit = "%")
                ChangeItem(label = "肌肉", change = muscleChange, unit = "kg")
            }
        }
    }
}

@Composable
private fun MeasurementsChangeCard(
    chestChange: String,
    waistChange: String,
    hipChange: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "三围变化",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ChangeItem(label = "胸围", change = chestChange, unit = "cm")
                ChangeItem(label = "腰围", change = waistChange, unit = "cm")
                ChangeItem(label = "臀围", change = hipChange, unit = "cm")
            }
        }
    }
}

@Composable
private fun ChangeItem(label: String, change: String, unit: String) {
    val isPositive = change.startsWith("+")
    val isNegative = change.startsWith("-")
    val color = when {
        isNegative -> MaterialTheme.colorScheme.primary
        isPositive -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = change,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(16.dp)
                .height(2.dp)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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