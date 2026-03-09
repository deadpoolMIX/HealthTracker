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
                            period = uiState.selectedPeriod,
                            onClick = onNavigateToNutritionDetail
                        )
                    }
                }

                // 身体数据折线图
                if (uiState.showBodyChart && uiState.bodyData.isNotEmpty()) {
                    item {
                        BodyDataChartCard(
                            data = uiState.bodyData,
                            period = uiState.selectedPeriod,
                            onClick = onNavigateToBodyDataDetail
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
 * 营养素柱状图卡片
 */
@Composable
private fun NutritionChartCard(
    data: List<DailyNutrition>,
    period: Int,
    onClick: () -> Unit = {}
) {
    // 使用统一的营养素颜色
    val carbsColor = NutrientColors.Carbs
    val proteinColor = NutrientColors.Protein
    val fatColor = NutrientColors.Fat

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

            // 图例
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LegendItem("碳水", carbsColor)
                LegendItem("蛋白质", proteinColor)
                LegendItem("脂肪", fatColor)
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
            } else if (period == 0) {
                // 周模式：只显示最近7天
                WeeklyNutritionChartContent(
                    data = data.sortedBy { it.date }.takeLast(7),
                    carbsColor = carbsColor,
                    proteinColor = proteinColor,
                    fatColor = fatColor
                )
            } else {
                // 月模式：按周聚合
                val weeklyData = aggregateByWeek(data)
                MonthlyNutritionChartContent(
                    weeklyData = weeklyData,
                    carbsColor = carbsColor,
                    proteinColor = proteinColor,
                    fatColor = fatColor
                )
            }
        }
    }
}

/**
 * 周模式营养素图表内容
 * 柱子和标签都使用Compose布局，确保精确对齐
 */
@Composable
private fun WeeklyNutritionChartContent(
    data: List<DailyNutrition>,
    carbsColor: Color,
    proteinColor: Color,
    fatColor: Color
) {
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    // 数据汇总（基于实际数据）
    val avgCalories = if (data.isNotEmpty()) data.sumOf { it.calories } / data.size else 0.0
    val avgCarbs = if (data.isNotEmpty()) data.sumOf { it.carbs } / data.size else 0.0
    val avgProtein = if (data.isNotEmpty()) data.sumOf { it.protein } / data.size else 0.0
    val avgFat = if (data.isNotEmpty()) data.sumOf { it.fat } / data.size else 0.0

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItemCompact("热量", String.format("%.0f", avgCalories), "kcal", MaterialTheme.colorScheme.error)
        StatItemCompact("碳水", String.format("%.0f", avgCarbs), "g", carbsColor)
        StatItemCompact("蛋白", String.format("%.0f", avgProtein), "g", proteinColor)
        StatItemCompact("脂肪", String.format("%.0f", avgFat), "g", fatColor)
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 堆叠柱状图 - 带y轴（热量）
    val maxCalories = maxOf(
        data.maxOfOrNull { it.calories } ?: 500.0,
        500.0
    )

    Row(modifier = Modifier.fillMaxWidth()) {
        // Y轴标签
        Column(
            modifier = Modifier.width(32.dp).height(200.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(String.format("%.0f", maxCalories), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Text(String.format("%.0f", maxCalories * 0.5), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Text("0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }

        // 图表区域 - 固定7格布局
        Column(modifier = Modifier.weight(1f)) {
            // 柱状图
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // 固定7格，柱子和标签天然对齐
                repeat(7) { slotIndex ->
                    val day = data.getOrNull(slotIndex)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        if (day != null) {
                            val maxHeight = 200f
                            val carbsHeight = (day.carbs * 4 / maxCalories * maxHeight).toFloat()
                            val proteinHeight = (day.protein * 4 / maxCalories * maxHeight).toFloat()
                            val fatHeight = (day.fat * 9 / maxCalories * maxHeight).toFloat()
                            val totalHeight = carbsHeight + proteinHeight + fatHeight

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .height(totalHeight.dp.coerceAtMost(200.dp)),
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                if (fatHeight > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(fatHeight.dp)
                                            .background(fatColor)
                                    )
                                }
                                if (proteinHeight > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(proteinHeight.dp)
                                            .background(proteinColor)
                                    )
                                }
                                if (carbsHeight > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(carbsHeight.dp)
                                            .background(carbsColor)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // X轴标签 - 与柱子相同的布局
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { slotIndex ->
                    val day = data.getOrNull(slotIndex)

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day != null) {
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = day.date
                            Text(
                                text = "${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.DAY_OF_MONTH)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 月模式营养素图表内容
 */
@Composable
private fun MonthlyNutritionChartContent(
    weeklyData: List<WeeklyNutritionDisplay>,
    carbsColor: Color,
    proteinColor: Color,
    fatColor: Color
) {
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    if (weeklyData.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("暂无摄入数据", color = onSurfaceVariantColor)
        }
        return
    }

    // 数据汇总
    val avgCalories = weeklyData.sumOf { it.calories } / weeklyData.size
    val avgCarbs = weeklyData.sumOf { it.carbs } / weeklyData.size
    val avgProtein = weeklyData.sumOf { it.protein } / weeklyData.size
    val avgFat = weeklyData.sumOf { it.fat } / weeklyData.size

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItemCompact("热量", String.format("%.0f", avgCalories), "kcal", MaterialTheme.colorScheme.error)
        StatItemCompact("碳水", String.format("%.0f", avgCarbs), "g", carbsColor)
        StatItemCompact("蛋白", String.format("%.0f", avgProtein), "g", proteinColor)
        StatItemCompact("脂肪", String.format("%.0f", avgFat), "g", fatColor)
    }

    Spacer(modifier = Modifier.height(12.dp))

    val maxCalories = maxOf(
        weeklyData.maxOfOrNull { it.calories } ?: 500.0,
        500.0
    )

    Row(modifier = Modifier.fillMaxWidth()) {
        // Y轴标签（热量，不带单位）
        Column(
            modifier = Modifier.width(32.dp).height(200.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(String.format("%.0f", maxCalories), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Text(String.format("%.0f", maxCalories * 0.5), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Text("0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }

        // 图表区域
        Column(modifier = Modifier.weight(1f)) {
            Canvas(
                modifier = Modifier.fillMaxWidth().height(200.dp)
            ) {
                val barCount = weeklyData.size
                val totalSpacing = size.width * 0.3f
                val totalBarWidth = size.width - totalSpacing
                val barWidth = totalBarWidth / barCount
                val spacing = totalSpacing / (barCount + 1)
                val chartHeight = size.height
                val chartWidth = size.width

                // 先绘制虚线（在柱状图下面）
                val linePositions = listOf(0f, 0.5f, 1f)
                linePositions.forEach { ratio ->
                    val lineY = chartHeight * ratio * 0.95f
                    drawLine(
                        color = onSurfaceVariantColor.copy(alpha = 0.2f),
                        start = Offset(0f, lineY),
                        end = Offset(chartWidth, lineY),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                    )
                }

                weeklyData.forEachIndexed { index, week ->
                    val x = spacing + index * (barWidth + spacing)

                    // 按热量比例计算高度
                    val carbsHeight = (week.carbs * 4 / maxCalories * chartHeight * 0.95f).toFloat()
                    val proteinHeight = (week.protein * 4 / maxCalories * chartHeight * 0.95f).toFloat()
                    val fatHeight = (week.fat * 9 / maxCalories * chartHeight * 0.95f).toFloat()

                    drawRect(carbsColor, Offset(x, chartHeight - carbsHeight), Size(barWidth, carbsHeight))
                    drawRect(proteinColor, Offset(x, chartHeight - carbsHeight - proteinHeight), Size(barWidth, proteinHeight))
                    drawRect(fatColor, Offset(x, chartHeight - carbsHeight - proteinHeight - fatHeight), Size(barWidth, fatHeight))
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

/**
 * 按周聚合营养数据
 */
private fun aggregateByWeek(data: List<DailyNutrition>): List<WeeklyNutritionDisplay> {
    val calendar = Calendar.getInstance()
    val now = System.currentTimeMillis()
    calendar.timeInMillis = now
    calendar.add(Calendar.WEEK_OF_YEAR, -3)
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    val result = mutableListOf<WeeklyNutritionDisplay>()

    for (i in 0 until 4) {
        val weekStart = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 6)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val weekEnd = calendar.timeInMillis

        val weekData = data.filter { it.date in weekStart..weekEnd }

        result.add(WeeklyNutritionDisplay(
            date = weekStart,
            weekLabel = "第${i + 1}周",
            calories = weekData.sumOf { it.calories },
            carbs = weekData.sumOf { it.carbs },
            protein = weekData.sumOf { it.protein },
            fat = weekData.sumOf { it.fat }
        ))

        calendar.add(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
    }

    return result
}

/**
 * 周营养数据显示类
 */
private data class WeeklyNutritionDisplay(
    val date: Long,
    val weekLabel: String,
    val calories: Double,
    val carbs: Double,
    val protein: Double,
    val fat: Double
)

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
    period: Int,
    onClick: () -> Unit = {}
) {
    // 数据类型选择：0=体重, 1=体脂, 2=肌肉
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
            }

            // 数据类型选择（单选）
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("体重", "体脂", "肌肉").forEachIndexed { index, label ->
                    FilterChip(
                        selected = selectedDataType == index,
                        onClick = { selectedDataType = index },
                        label = { Text(label, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 折线图
            val sortedData = data.sortedBy { it.date }

            // 提取选中类型的数据值
            val values = sortedData.mapNotNull { entity ->
                when (selectedDataType) {
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
                // 动态自适应缩放：以数据范围为中心，添加10%边距
                val dataMin = values.minOrNull() ?: 0.0
                val dataMax = values.maxOrNull() ?: 100.0
                val dataRange = (dataMax - dataMin).coerceAtLeast(0.1)

                val padding = dataRange * 0.1
                val minVal = dataMin - padding
                val maxVal = dataMax + padding
                val range = (maxVal - minVal).coerceAtLeast(0.1)

                // Y轴刻度（5个刻度）
                val yLabels = (0..4).map { i ->
                    val value = maxVal - (range * i / 4)
                    String.format("%.1f", value)
                }

                // X轴标签（智能选择显示）
                val xLabels = remember(sortedData) {
                    if (sortedData.size <= 7) {
                        sortedData.map { entity ->
                            val calendar = Calendar.getInstance()
                            calendar.timeInMillis = entity.date
                            "${calendar.get(Calendar.DAY_OF_MONTH)}日"
                        }
                    } else {
                        // 数据量较大，选择3个关键日期
                        val first = sortedData.first()
                        val middle = sortedData[sortedData.size / 2]
                        val last = sortedData.last()

                        listOf(first, middle, last).map { entity ->
                            val calendar = Calendar.getInstance()
                            calendar.timeInMillis = entity.date
                            "${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.DAY_OF_MONTH)}"
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Y轴标签 - 宽度32dp，与营养素摄入区块一致
                        Column(
                            modifier = Modifier
                                .width(32.dp)
                                .padding(top = 10.dp, bottom = 30.dp)
                                .height(200.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            yLabels.forEach { label ->
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            }
                        }

                        // 图表区域
                        Column(modifier = Modifier.weight(1f)) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(top = 10.dp, end = 10.dp, bottom = 30.dp)
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
                                    // 数据点和标签使用相同的坐标计算：SpaceBetween 布局
                                    val points = values.mapIndexed { index, value ->
                                        // SpaceBetween 布局：第一个点在最左边，最后一个点在最右边
                                        val x = if (values.size > 1) {
                                            (index.toFloat() / (values.size - 1)) * chartWidth
                                        } else {
                                            0f // 单个数据点放在最左边
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
                                    // 单个数据点放在最左边，与标签对齐
                                    val x = 0f
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

                            // X轴标签
                            if (xLabels.isNotEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(end = 10.dp),
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

                // 最新数据统计
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val latestValue = values.lastOrNull()
                    latestValue?.let {
                        StatItem(dataLabel, "${String.format("%.1f", it)} $unit")
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

// 睡眠范围条形图卡片 - 竖着的柱状图，y轴显示时间
@Composable
private fun SleepChartCard(
    data: List<SleepRecordEntity>,
    period: Int,
    avgSleepTime: String,
    avgWakeTime: String,
    avgDuration: Long,
    onClick: () -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

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

            if (period == 0) {
                // 周模式：显示最近七天的睡眠柱状图
                WeekSleepChartWithTimeAxis(data = data, primaryColor = primaryColor, onSurfaceVariantColor = onSurfaceVariantColor)
            } else {
                // 月模式：显示四周的睡眠柱状图
                MonthSleepChartWithTimeAxis(data = data, primaryColor = primaryColor, onSurfaceVariantColor = onSurfaceVariantColor)
            }
        }
    }
}

/**
 * 周模式睡眠图表 - y轴显示时间，柱状图顶部是入睡时间，底部是起床时间
 * 柱子和标签都使用Compose布局，确保精确对齐
 */
@Composable
private fun WeekSleepChartWithTimeAxis(
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

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Y轴时间标签
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

        // 图表区域 - 固定7格布局
        Column(modifier = Modifier.weight(1f)) {
            // 柱状图 - 使用Canvas绘制（睡眠图表需要精确的时间坐标）
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val chartHeight = size.height
                val chartWidth = size.width

                // 固定柱子宽度
                val fixedBarWidth = chartWidth / 7f * 0.7f
                val slotWidth = chartWidth / 7f

                // 先绘制虚线
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

                // 绘制柱子 - 固定在7个位置
                sortedData.forEachIndexed { index, sleep ->
                    val calSleep = Calendar.getInstance()
                    calSleep.timeInMillis = sleep.sleepTime
                    val sleepHour = calSleep.get(Calendar.HOUR_OF_DAY)
                    val sleepMinute = calSleep.get(Calendar.MINUTE)
                    var sleepHourContinuous = sleepHour + sleepMinute / 60f
                    if (sleepHourContinuous < startHour) {
                        sleepHourContinuous += 24
                    }

                    val calWake = Calendar.getInstance()
                    calWake.timeInMillis = sleep.wakeTime
                    val wakeHour = calWake.get(Calendar.HOUR_OF_DAY)
                    val wakeMinute = calWake.get(Calendar.MINUTE)
                    var wakeHourContinuous = wakeHour + wakeMinute / 60f
                    if (wakeHourContinuous < startHour) {
                        wakeHourContinuous += 24
                    }
                    if (wakeHourContinuous > endHour) {
                        wakeHourContinuous = endHour.toFloat()
                    }

                    // 柱子位置：固定在对应的slot中
                    val x = index * slotWidth + (slotWidth - fixedBarWidth) / 2
                    val sleepY = (sleepHourContinuous - startHour) / totalHours * chartHeight
                    val wakeY = (wakeHourContinuous - startHour) / totalHours * chartHeight

                    drawRoundRect(
                        color = primaryColor.copy(alpha = 0.8f),
                        topLeft = Offset(x, sleepY),
                        size = Size(fixedBarWidth, wakeY - sleepY),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                    )
                }
            }

            // X轴标签 - 与柱子相同的布局
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { slotIndex ->
                    val sleep = sortedData.getOrNull(slotIndex)

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (sleep != null) {
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = sleep.date
                            Text(
                                text = "${cal.get(Calendar.DAY_OF_MONTH)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = onSurfaceVariantColor
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 月模式睡眠图表 - 四周的平均睡眠时间
 */
@Composable
private fun MonthSleepChartWithTimeAxis(
    data: List<SleepRecordEntity>,
    primaryColor: Color,
    onSurfaceVariantColor: Color
) {
    // 按周分组计算平均入睡和起床时间
    val calendar = Calendar.getInstance()
    val now = System.currentTimeMillis()
    calendar.timeInMillis = now
    calendar.add(Calendar.WEEK_OF_YEAR, -3)
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    val weeklyData = mutableListOf<WeeklySleepTime>()

    // 时间范围：22:00 到 12:00（次日中午）
    val startHour = 22

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
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                val minute = cal.get(Calendar.MINUTE)
                var h = hour + minute / 60f
                if (h < startHour) h += 24 // 跨天
                h
            }.average().toFloat()
        } else 23f

        val avgWakeHour = if (weekRecords.isNotEmpty()) {
            weekRecords.map { record ->
                val cal = Calendar.getInstance()
                cal.timeInMillis = record.wakeTime
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                val minute = cal.get(Calendar.MINUTE)
                hour + minute / 60f
            }.average().toFloat()
        } else 7f

        weeklyData.add(WeeklySleepTime(
            weekLabel = "第${i + 1}周",
            avgSleepHour = avgSleepHour,
            avgWakeHour = avgWakeHour
        ))

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

    // 时间范围：22:00 到 12:00（次日中午）
    val endHour = 36
    val totalHours = endHour - startHour

    // 8个时间点（从上到下：22:00, 00:00, 02:00, 04:00, 06:00, 08:00, 10:00, 12:00）
    // Canvas y=0 是顶部，所以 22:00 对应 y=0，12:00 对应 y=chartHeight
    val timePoints = listOf(
        22 to "22:00", // 22:00 - 在图表顶部
        24 to "00:00", // 00:00（午夜）
        26 to "02:00", // 02:00
        28 to "04:00", // 04:00
        30 to "06:00", // 06:00
        32 to "08:00", // 08:00
        34 to "10:00", // 10:00
        36 to "12:00"  // 12:00（次日中午）- 在图表底部
    )

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Y轴时间标签 - 8个时间点
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

        // 图表区域
        Column(
            modifier = Modifier.weight(1f)
        ) {
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

                // 先绘制所有时间点的虚线（在柱状图下面）
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
                    var wakeHour = week.avgWakeHour
                    if (wakeHour < startHour) wakeHour += 24f
                    if (wakeHour > endHour) wakeHour = endHour.toFloat()
                    val wakeY = (wakeHour - startHour) / totalHours * chartHeight

                    // 绘制睡眠柱状图
                    drawRoundRect(
                        color = primaryColor.copy(alpha = 0.8f),
                        topLeft = Offset(x, sleepY),
                        size = Size(barWidth, wakeY - sleepY),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                    )
                }
            }

            // X轴周标签
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

/**
 * 每周睡眠时间数据
 */
private data class WeeklySleepTime(
    val weekLabel: String,
    val avgSleepHour: Float,
    val avgWakeHour: Float
)

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