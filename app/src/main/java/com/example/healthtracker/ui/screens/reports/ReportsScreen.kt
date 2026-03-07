package com.example.healthtracker.ui.screens.reports

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.healthtracker.util.DateTimeUtils
import java.util.Calendar
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel(),
    onNavigateToDataExport: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val periods = listOf("天", "周", "月", "年")

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
                actions = {
                    IconButton(onClick = { viewModel.showSettingsDialog() }) {
                        Icon(Icons.Default.Settings, contentDescription = "报表设置")
                    }
                    IconButton(onClick = onNavigateToDataExport) {
                        Icon(Icons.Default.Download, contentDescription = "导出数据")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
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
                    repeat(4) {
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
                if (uiState.showNutritionChart) {
                    item {
                        NutritionChartCard(
                            data = uiState.intakeData,
                            period = uiState.selectedPeriod
                        )
                    }
                }

                // 身体数据折线图
                if (uiState.showBodyChart && uiState.bodyData.isNotEmpty()) {
                    item {
                        BodyDataChartCard(
                            data = uiState.bodyData,
                            period = uiState.selectedPeriod
                        )
                    }
                }

                // 睡眠范围条形图
                if (uiState.showSleepChart && uiState.sleepData.isNotEmpty()) {
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
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
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
    // 数据类型选择：0=体重体脂肌肉，1=三围
    var dataType by remember { mutableIntStateOf(0) }

    val weightColor = MaterialTheme.colorScheme.primary
    val bodyFatColor = MaterialTheme.colorScheme.error
    val muscleColor = MaterialTheme.colorScheme.tertiary
    val chestColor = MaterialTheme.colorScheme.primary
    val waistColor = MaterialTheme.colorScheme.secondary
    val hipColor = MaterialTheme.colorScheme.tertiary

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
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
                    text = "身体数据趋势",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                // 数据类型切换
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = dataType == 0,
                        onClick = { dataType = 0 },
                        label = { Text("体重体脂", fontSize = 12.sp) },
                        modifier = Modifier.height(28.dp)
                    )
                    FilterChip(
                        selected = dataType == 1,
                        onClick = { dataType = 1 },
                        label = { Text("三围", fontSize = 12.sp) },
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
                if (dataType == 0) {
                    LegendItem("体重", weightColor)
                    LegendItem("体脂", bodyFatColor)
                    LegendItem("肌肉", muscleColor)
                } else {
                    LegendItem("胸围", chestColor)
                    LegendItem("腰围", waistColor)
                    LegendItem("臀围", hipColor)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 折线图
            val sortedData = data.sortedBy { it.date }

            if (dataType == 0) {
                // 体重、体脂、肌肉量
                val weights = sortedData.mapNotNull { it.weight }
                val bodyFats = sortedData.mapNotNull { it.bodyFatRate }
                val muscles = sortedData.mapNotNull { it.muscleMass }

                if (weights.isEmpty() && bodyFats.isEmpty() && muscles.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无身体数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    // 计算范围
                    val minWeight = (weights.minOrNull() ?: 50.0) - 2
                    val maxWeight = (weights.maxOrNull() ?: 80.0) + 2
                    val minBodyFat = (bodyFats.minOrNull() ?: 15.0) - 2
                    val maxBodyFat = (bodyFats.maxOrNull() ?: 25.0) + 2
                    val minMuscle = (muscles.minOrNull() ?: 30.0) - 2
                    val maxMuscle = (muscles.maxOrNull() ?: 50.0) + 2

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        val chartHeight = size.height - 40f
                        val chartWidth = size.width

                        // 绘制平滑曲线
                        // 体重折线
                        if (weights.size >= 2) {
                            drawSmoothLine(
                                points = weights.mapIndexed { index, weight ->
                                    val x = (index.toFloat() / (weights.size - 1)) * chartWidth
                                    val y = chartHeight - ((weight - minWeight) / (maxWeight - minWeight) * chartHeight).toFloat() + 20
                                    Offset(x, y)
                                },
                                color = weightColor
                            )
                            // 绘制数据点
                            weights.forEachIndexed { index, weight ->
                                val x = (index.toFloat() / (weights.size - 1)) * chartWidth
                                val y = chartHeight - ((weight - minWeight) / (maxWeight - minWeight) * chartHeight).toFloat() + 20
                                drawCircle(color = weightColor, radius = 4f, center = Offset(x, y))
                            }
                        }

                        // 体脂折线
                        if (bodyFats.size >= 2) {
                            drawSmoothLine(
                                points = bodyFats.mapIndexed { index, bodyFat ->
                                    val x = (index.toFloat() / (bodyFats.size - 1)) * chartWidth
                                    val y = chartHeight - ((bodyFat - minBodyFat) / (maxBodyFat - minBodyFat) * chartHeight).toFloat() + 20
                                    Offset(x, y)
                                },
                                color = bodyFatColor
                            )
                            bodyFats.forEachIndexed { index, bodyFat ->
                                val x = (index.toFloat() / (bodyFats.size - 1)) * chartWidth
                                val y = chartHeight - ((bodyFat - minBodyFat) / (maxBodyFat - minBodyFat) * chartHeight).toFloat() + 20
                                drawCircle(color = bodyFatColor, radius = 4f, center = Offset(x, y))
                            }
                        }

                        // 肌肉量折线
                        if (muscles.size >= 2) {
                            drawSmoothLine(
                                points = muscles.mapIndexed { index, muscle ->
                                    val x = (index.toFloat() / (muscles.size - 1)) * chartWidth
                                    val y = chartHeight - ((muscle - minMuscle) / (maxMuscle - minMuscle) * chartHeight).toFloat() + 20
                                    Offset(x, y)
                                },
                                color = muscleColor
                            )
                            muscles.forEachIndexed { index, muscle ->
                                val x = (index.toFloat() / (muscles.size - 1)) * chartWidth
                                val y = chartHeight - ((muscle - minMuscle) / (maxMuscle - minMuscle) * chartHeight).toFloat() + 20
                                drawCircle(color = muscleColor, radius = 4f, center = Offset(x, y))
                            }
                        }
                    }

                    // 统计信息
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        weights.lastOrNull()?.let { StatItem("体重", "${String.format("%.1f", it)} kg") }
                        bodyFats.lastOrNull()?.let { StatItem("体脂", "${String.format("%.1f", it)}%") }
                        muscles.lastOrNull()?.let { StatItem("肌肉", "${String.format("%.1f", it)} kg") }
                    }
                }
            } else {
                // 三围数据
                val chests = sortedData.mapNotNull { it.chest }
                val waists = sortedData.mapNotNull { it.waist }
                val hips = sortedData.mapNotNull { it.hip }

                if (chests.isEmpty() && waists.isEmpty() && hips.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无三围数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    // 计算范围（三围使用统一范围）
                    val allMeasurements = chests + waists + hips
                    val minMeasure = (allMeasurements.minOrNull() ?: 60.0) - 5
                    val maxMeasure = (allMeasurements.maxOrNull() ?: 100.0) + 5

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        val chartHeight = size.height - 40f
                        val chartWidth = size.width

                        // 胸围折线
                        if (chests.size >= 2) {
                            drawSmoothLine(
                                points = chests.mapIndexed { index, chest ->
                                    val x = (index.toFloat() / (chests.size - 1)) * chartWidth
                                    val y = chartHeight - ((chest - minMeasure) / (maxMeasure - minMeasure) * chartHeight).toFloat() + 20
                                    Offset(x, y)
                                },
                                color = chestColor
                            )
                            chests.forEachIndexed { index, chest ->
                                val x = (index.toFloat() / (chests.size - 1)) * chartWidth
                                val y = chartHeight - ((chest - minMeasure) / (maxMeasure - minMeasure) * chartHeight).toFloat() + 20
                                drawCircle(color = chestColor, radius = 4f, center = Offset(x, y))
                            }
                        }

                        // 腰围折线
                        if (waists.size >= 2) {
                            drawSmoothLine(
                                points = waists.mapIndexed { index, waist ->
                                    val x = (index.toFloat() / (waists.size - 1)) * chartWidth
                                    val y = chartHeight - ((waist - minMeasure) / (maxMeasure - minMeasure) * chartHeight).toFloat() + 20
                                    Offset(x, y)
                                },
                                color = waistColor
                            )
                            waists.forEachIndexed { index, waist ->
                                val x = (index.toFloat() / (waists.size - 1)) * chartWidth
                                val y = chartHeight - ((waist - minMeasure) / (maxMeasure - minMeasure) * chartHeight).toFloat() + 20
                                drawCircle(color = waistColor, radius = 4f, center = Offset(x, y))
                            }
                        }

                        // 臀围折线
                        if (hips.size >= 2) {
                            drawSmoothLine(
                                points = hips.mapIndexed { index, hip ->
                                    val x = (index.toFloat() / (hips.size - 1)) * chartWidth
                                    val y = chartHeight - ((hip - minMeasure) / (maxMeasure - minMeasure) * chartHeight).toFloat() + 20
                                    Offset(x, y)
                                },
                                color = hipColor
                            )
                            hips.forEachIndexed { index, hip ->
                                val x = (index.toFloat() / (hips.size - 1)) * chartWidth
                                val y = chartHeight - ((hip - minMeasure) / (maxMeasure - minMeasure) * chartHeight).toFloat() + 20
                                drawCircle(color = hipColor, radius = 4f, center = Offset(x, y))
                            }
                        }
                    }

                    // 统计信息
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        chests.lastOrNull()?.let { StatItem("胸围", "${String.format("%.1f", it)} cm") }
                        waists.lastOrNull()?.let { StatItem("腰围", "${String.format("%.1f", it)} cm") }
                        hips.lastOrNull()?.let { StatItem("臀围", "${String.format("%.1f", it)} cm") }
                    }
                }
            }
        }
    }
}

/**
 * 绘制平滑曲线（贝塞尔曲线）
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSmoothLine(
    points: List<Offset>,
    color: Color
) {
    if (points.size < 2) return

    val path = Path()
    path.moveTo(points[0].x, points[0].y)

    for (i in 1 until points.size) {
        val prevPoint = points[i - 1]
        val currentPoint = points[i]

        // 使用二次贝塞尔曲线
        val midX = (prevPoint.x + currentPoint.x) / 2
        path.quadraticTo(
            prevPoint.x, prevPoint.y,
            midX, (prevPoint.y + currentPoint.y) / 2
        )
        path.quadraticTo(
            currentPoint.x, currentPoint.y,
            currentPoint.x, currentPoint.y
        )
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 2.5f)
    )
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
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
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

            // 图表区域 - 左侧日期 + 右侧条形图
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                // 日期标签列（左侧）
                Column(
                    modifier = Modifier
                        .width(36.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    sortedData.forEach { sleep ->
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = sleep.date
                        val dayStr = "${cal.get(Calendar.DAY_OF_MONTH)}日"
                        Text(
                            text = dayStr,
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurfaceVariantColor,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                // 图表区域（右侧）
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    // 时间轴：18:00 - 12:00（次日中午）
                    val startHour = 18f
                    val totalHours = 18f
                    val chartHeight = size.height
                    val chartWidth = size.width

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

                        val barHeight = chartHeight / sortedData.size * 0.65f
                        val y = index * (chartHeight / sortedData.size) + barHeight * 0.2f

                        // 绘制睡眠条
                        drawRoundRect(
                            color = primaryColor.copy(alpha = 0.7f),
                            topLeft = Offset(sleepOffset / totalHours * chartWidth, y),
                            size = Size(barWidth / totalHours * chartWidth, barHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                        )
                    }

                    // 绘制午夜线
                    val midnightX = (24f - startHour) / totalHours * chartWidth
                    drawLine(
                        color = onSurfaceVariantColor.copy(alpha = 0.5f),
                        start = Offset(midnightX, 0f),
                        end = Offset(midnightX, chartHeight),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                    )

                    // 绘制6点线
                    val sixAmX = (30f - startHour) / totalHours * chartWidth
                    drawLine(
                        color = onSurfaceVariantColor.copy(alpha = 0.3f),
                        start = Offset(sixAmX, 0f),
                        end = Offset(sixAmX, chartHeight),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 4f))
                    )
                }
            }

            // 时间轴标签
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 36.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("18:00", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariantColor)
                Text("00:00", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariantColor)
                Text("06:00", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariantColor)
                Text("12:00", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariantColor)
            }

            // 睡眠时长列表
            Spacer(modifier = Modifier.height(12.dp))
            sortedData.takeLast(5).forEach { sleep ->
                val cal = Calendar.getInstance()
                cal.timeInMillis = sleep.date
                val dateStr = "${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.DAY_OF_MONTH)}"
                val durationStr = formatDuration(sleep.duration)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceVariantColor
                    )
                    Text(
                        text = "睡眠 $durationStr",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
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

/**
 * 动画图表卡片容器
 */
@Composable
private fun AnimatedChartCard(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
            animationSpec = tween(300),
            initialOffsetY = { it / 4 }
        )
    ) {
        content()
    }
}