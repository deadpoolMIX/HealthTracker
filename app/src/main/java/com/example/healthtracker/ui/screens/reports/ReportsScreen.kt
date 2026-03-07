package com.example.healthtracker.ui.screens.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.healthtracker.data.local.entity.BodyRecordEntity
import com.example.healthtracker.data.local.entity.SleepRecordEntity
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

/**
 * 营养素柱状图卡片
 * 支持堆叠柱状图 + 热量折线叠加
 */
@Composable
private fun NutritionChartCard(
    data: List<DailyNutrition>,
    period: Int
) {
    // 图表类型：0=堆叠柱状图，1=并排柱状图
    var chartType by remember { mutableIntStateOf(0) }
    // 是否显示热量折线
    var showCaloriesLine by remember { mutableStateOf(true) }

    val carbsColor = MaterialTheme.colorScheme.primary
    val proteinColor = MaterialTheme.colorScheme.secondary
    val fatColor = MaterialTheme.colorScheme.tertiary
    val caloriesColor = MaterialTheme.colorScheme.error

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 标题行
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
                // 图表类型切换
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = chartType == 0,
                        onClick = { chartType = 0 },
                        label = { Text("堆叠", fontSize = 12.sp) },
                        modifier = Modifier.height(28.dp)
                    )
                    FilterChip(
                        selected = chartType == 1,
                        onClick = { chartType = 1 },
                        label = { Text("并排", fontSize = 12.sp) },
                        modifier = Modifier.height(28.dp)
                    )
                }
            }

            // 图例
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LegendItem("碳水", carbsColor)
                LegendItem("蛋白质", proteinColor)
                LegendItem("脂肪", fatColor)
                if (showCaloriesLine) {
                    LegendItem("热量", caloriesColor, isLine = true)
                }
            }

            // 热量折线开关
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { showCaloriesLine = !showCaloriesLine }
            ) {
                Checkbox(
                    checked = showCaloriesLine,
                    onCheckedChange = { showCaloriesLine = it },
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "显示热量折线",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

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
                // 数据汇总
                val totalCalories = data.sumOf { it.calories }
                val avgCalories = if (data.isNotEmpty()) totalCalories / data.size else 0.0
                val avgCarbs = if (data.isNotEmpty()) data.sumOf { it.carbs } / data.size else 0.0
                val avgProtein = if (data.isNotEmpty()) data.sumOf { it.protein } / data.size else 0.0
                val avgFat = if (data.isNotEmpty()) data.sumOf { it.fat } / data.size else 0.0

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("平均热量", "${String.format("%.0f", avgCalories)} kcal")
                    StatItem("碳水", "${String.format("%.0f", avgCarbs)}g")
                    StatItem("蛋白质", "${String.format("%.0f", avgProtein)}g")
                    StatItem("脂肪", "${String.format("%.0f", avgFat)}g")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 图表
                val sortedData = data.sortedBy { it.date }
                val maxNutrient = maxOf(
                    sortedData.maxOfOrNull { it.carbs + it.protein + it.fat } ?: 100.0,
                    100.0
                )
                val maxCaloriesValue = sortedData.maxOfOrNull { it.calories } ?: 1.0

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    val barWidth = size.width / (sortedData.size * 1.8f)
                    val spacing = barWidth * 0.8f
                    val chartHeight = size.height - 30f

                    sortedData.forEachIndexed { index, day ->
                        val x = index * (barWidth + spacing) + spacing / 2

                        if (chartType == 0) {
                            // 堆叠柱状图：碳水(下) -> 蛋白质(中) -> 脂肪(上)
                            val carbsHeight = (day.carbs / maxNutrient * chartHeight).toFloat()
                            val proteinHeight = (day.protein / maxNutrient * chartHeight).toFloat()
                            val fatHeight = (day.fat / maxNutrient * chartHeight).toFloat()

                            // 碳水 - 底部
                            drawRect(
                                color = carbsColor,
                                topLeft = Offset(x, chartHeight - carbsHeight),
                                size = Size(barWidth, carbsHeight)
                            )
                            // 蛋白质 - 中间
                            drawRect(
                                color = proteinColor,
                                topLeft = Offset(x, chartHeight - carbsHeight - proteinHeight),
                                size = Size(barWidth, proteinHeight)
                            )
                            // 脂肪 - 顶部
                            drawRect(
                                color = fatColor,
                                topLeft = Offset(x, chartHeight - carbsHeight - proteinHeight - fatHeight),
                                size = Size(barWidth, fatHeight)
                            )
                        } else {
                            // 并排柱状图
                            val singleBarWidth = barWidth / 3.2f
                            val maxValue = maxOf(
                                sortedData.maxOfOrNull { it.carbs } ?: 1.0,
                                sortedData.maxOfOrNull { it.protein } ?: 1.0,
                                sortedData.maxOfOrNull { it.fat } ?: 1.0,
                                1.0
                            )

                            val carbsHeight = (day.carbs / maxValue * chartHeight).toFloat()
                            val proteinHeight = (day.protein / maxValue * chartHeight).toFloat()
                            val fatHeight = (day.fat / maxValue * chartHeight).toFloat()

                            // 碳水
                            drawRect(
                                color = carbsColor,
                                topLeft = Offset(x, chartHeight - carbsHeight),
                                size = Size(singleBarWidth, carbsHeight)
                            )
                            // 蛋白质
                            drawRect(
                                color = proteinColor,
                                topLeft = Offset(x + singleBarWidth + 2.dp.toPx(), chartHeight - proteinHeight),
                                size = Size(singleBarWidth, proteinHeight)
                            )
                            // 脂肪
                            drawRect(
                                color = fatColor,
                                topLeft = Offset(x + (singleBarWidth + 2.dp.toPx()) * 2, chartHeight - fatHeight),
                                size = Size(singleBarWidth, fatHeight)
                            )
                        }
                    }

                    // 热量折线
                    if (showCaloriesLine && sortedData.size >= 2) {
                        val path = Path()
                        sortedData.forEachIndexed { index, day ->
                            val x = index * (barWidth + spacing) + spacing / 2 + barWidth / 2
                            val y = chartHeight - (day.calories / maxCaloriesValue * chartHeight).toFloat()

                            if (index == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                        }

                        drawPath(
                            path = path,
                            color = caloriesColor,
                            style = Stroke(width = 2.5f)
                        )

                        // 绘制数据点
                        sortedData.forEachIndexed { index, day ->
                            val x = index * (barWidth + spacing) + spacing / 2 + barWidth / 2
                            val y = chartHeight - (day.calories / maxCaloriesValue * chartHeight).toFloat()
                            drawCircle(
                                color = caloriesColor,
                                radius = 4f,
                                center = Offset(x, y)
                            )
                        }
                    }
                }

                // 日期标签
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    sortedData.takeLast(7).forEach { day ->
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = day.date
                        Text(
                            text = "${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.DAY_OF_MONTH)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
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