package com.example.healthtracker.ui.screens.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.healthtracker.ui.theme.NutrientColors
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionDetailScreen(
    viewModel: NutritionDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val periods = listOf("周", "月")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("营养素摄入详情", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 周期切换按钮
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
                // 周期选择
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        periods.forEachIndexed { index, period ->
                            FilterChip(
                                selected = uiState.period == index,
                                onClick = { viewModel.setPeriod(index) },
                                label = { Text(period) }
                            )
                        }
                    }
                }

                // 柱状图区块
                item {
                    if (uiState.period == 0) {
                        NutritionChartCard(
                            title = "每日摄入",
                            data = uiState.dailyData,
                            period = uiState.period
                        )
                    } else {
                        NutritionWeeklyChartCard(
                            title = "每周摄入",
                            data = uiState.weeklyData
                        )
                    }
                }

                // 平均摄入量区块
                item {
                    AverageIntakeCard(
                        avgCalories = uiState.avgCalories,
                        avgCarbs = uiState.avgCarbs,
                        avgProtein = uiState.avgProtein,
                        avgFat = uiState.avgFat,
                        period = uiState.period
                    )
                }

                // 摄入总量区块
                item {
                    TotalIntakeCard(
                        totalCalories = uiState.totalCalories,
                        totalCarbs = uiState.totalCarbs,
                        totalProtein = uiState.totalProtein,
                        totalFat = uiState.totalFat,
                        period = uiState.period
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
private fun NutritionChartCard(
    title: String,
    data: List<DailyNutrition>,
    period: Int
) {
    // 使用统一的营养素颜色
    val carbsColor = NutrientColors.Carbs
    val proteinColor = NutrientColors.Protein
    val fatColor = NutrientColors.Fat

    // 点击的柱状图索引
    var selectedIndex by remember { mutableStateOf(-1) }

    // 详情弹窗
    if (selectedIndex >= 0 && selectedIndex < data.size) {
        val selectedDay = data[selectedIndex]
        val cal = Calendar.getInstance()
        cal.timeInMillis = selectedDay.date
        val dayStr = "${cal.get(Calendar.MONTH) + 1}月${cal.get(Calendar.DAY_OF_MONTH)}日"

        AlertDialog(
            onDismissRequest = { selectedIndex = -1 },
            title = { Text(dayStr, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    NutritionDetailRow("热量", String.format(Locale.getDefault(), "%.0f", selectedDay.calories), "kcal", MaterialTheme.colorScheme.error)
                    NutritionDetailRow("碳水", String.format(Locale.getDefault(), "%.1f", selectedDay.carbs), "g", carbsColor)
                    NutritionDetailRow("蛋白质", String.format(Locale.getDefault(), "%.1f", selectedDay.protein), "g", proteinColor)
                    NutritionDetailRow("脂肪", String.format(Locale.getDefault(), "%.1f", selectedDay.fat), "g", fatColor)
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedIndex = -1 }) {
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
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 图例
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LegendItem("碳水", carbsColor)
                LegendItem("蛋白质", proteinColor)
                LegendItem("脂肪", fatColor)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 柱状图
            if (data.isNotEmpty()) {
                // 找出所有数据的最大总量，用于等比例缩放
                val maxValue = data.maxOf { it.carbs + it.protein + it.fat }.toFloat().coerceAtLeast(1f)

                // 修复对齐问题：使用与标签相同的布局方式
                Column(modifier = Modifier.fillMaxWidth()) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .pointerInput(data) {
                                detectTapGestures { offset ->
                                    val barCount = data.size
                                    val totalSpacing = size.width * 0.3f
                                    val totalBarWidth = size.width - totalSpacing
                                    val barWidth = totalBarWidth / barCount
                                    val spacing = totalSpacing / (barCount + 1)

                                    data.forEachIndexed { index, _ ->
                                        val barStart = spacing + index * (barWidth + spacing)
                                        val barEnd = barStart + barWidth
                                        if (offset.x in barStart..barEnd) {
                                            selectedIndex = index
                                            return@detectTapGestures
                                        }
                                    }
                                }
                            }
                    ) {
                        val barCount = data.size
                        val totalSpacing = size.width * 0.3f // 总间距占30%
                        val totalBarWidth = size.width - totalSpacing // 柱状图总宽度占70%
                        val barWidth = totalBarWidth / barCount
                        val spacing = totalSpacing / (barCount + 1) // 每个间隔

                        data.forEachIndexed { index, day ->
                            val x = spacing + index * (barWidth + spacing)

                            // 堆叠柱状图 - 按实际数值等比例缩放
                            val dayTotal = day.carbs + day.protein + day.fat
                            val totalHeight: Float = (dayTotal / maxValue * size.height).toFloat()
                            val carbsHeight: Float = if (dayTotal > 0) (day.carbs / dayTotal * totalHeight).toFloat() else 0f
                            val proteinHeight: Float = if (dayTotal > 0) (day.protein / dayTotal * totalHeight).toFloat() else 0f
                            val fatHeight: Float = if (dayTotal > 0) (day.fat / dayTotal * totalHeight).toFloat() else 0f

                            // 绘制脂肪（底部）
                            drawRect(
                                color = fatColor,
                                topLeft = Offset(x, size.height - fatHeight),
                                size = Size(barWidth, fatHeight)
                            )

                            // 绘制蛋白质（中间）
                            drawRect(
                                color = proteinColor,
                                topLeft = Offset(x, size.height - fatHeight - proteinHeight),
                                size = Size(barWidth, proteinHeight)
                            )

                            // 绘制碳水（顶部）
                            drawRect(
                                color = carbsColor,
                                topLeft = Offset(x, size.height - fatHeight - proteinHeight - carbsHeight),
                                size = Size(barWidth, carbsHeight)
                            )
                        }
                    }

                    // X轴标签 - 与柱状图对齐
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        data.forEach { day ->
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = day.date
                            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                            val dayLabel = when (dayOfWeek) {
                                Calendar.MONDAY -> "一"
                                Calendar.TUESDAY -> "二"
                                Calendar.WEDNESDAY -> "三"
                                Calendar.THURSDAY -> "四"
                                Calendar.FRIDAY -> "五"
                                Calendar.SATURDAY -> "六"
                                Calendar.SUNDAY -> "日"
                                else -> ""
                            }
                            Text(
                                text = dayLabel,
                                style = MaterialTheme.typography.bodySmall,
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
}

@Composable
private fun NutritionDetailRow(
    label: String,
    value: String,
    unit: String,
    color: Color
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
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
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

@Composable
private fun NutritionWeeklyChartCard(
    title: String,
    data: List<WeeklyNutrition>
) {
    // 使用统一的营养素颜色
    val carbsColor = NutrientColors.Carbs
    val proteinColor = NutrientColors.Protein
    val fatColor = NutrientColors.Fat

    // 点击的柱状图索引
    var selectedIndex by remember { mutableStateOf(-1) }

    // 详情弹窗
    if (selectedIndex >= 0 && selectedIndex < data.size) {
        val selectedWeek = data[selectedIndex]

        AlertDialog(
            onDismissRequest = { selectedIndex = -1 },
            title = { Text(selectedWeek.weekLabel, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    NutritionDetailRow("热量", String.format(Locale.getDefault(), "%.0f", selectedWeek.calories), "kcal", MaterialTheme.colorScheme.error)
                    NutritionDetailRow("碳水", String.format(Locale.getDefault(), "%.1f", selectedWeek.carbs), "g", carbsColor)
                    NutritionDetailRow("蛋白质", String.format(Locale.getDefault(), "%.1f", selectedWeek.protein), "g", proteinColor)
                    NutritionDetailRow("脂肪", String.format(Locale.getDefault(), "%.1f", selectedWeek.fat), "g", fatColor)
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedIndex = -1 }) {
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
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 图例
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LegendItem("碳水", carbsColor)
                LegendItem("蛋白质", proteinColor)
                LegendItem("脂肪", fatColor)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 柱状图
            if (data.isNotEmpty()) {
                // 找出所有数据的最大总量，用于等比例缩放
                val maxValue = data.maxOf { it.carbs + it.protein + it.fat }.toFloat().coerceAtLeast(1f)

                // 修复对齐问题：使用与标签相同的布局方式
                Column(modifier = Modifier.fillMaxWidth()) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .pointerInput(data) {
                                detectTapGestures { offset ->
                                    val barCount = data.size
                                    val totalSpacing = size.width * 0.4f
                                    val totalBarWidth = size.width - totalSpacing
                                    val barWidth = totalBarWidth / barCount
                                    val spacing = totalSpacing / (barCount + 1)

                                    data.forEachIndexed { index, _ ->
                                        val barStart = spacing + index * (barWidth + spacing)
                                        val barEnd = barStart + barWidth
                                        if (offset.x in barStart..barEnd) {
                                            selectedIndex = index
                                            return@detectTapGestures
                                        }
                                    }
                                }
                            }
                    ) {
                        val barCount = data.size
                        val totalSpacing = size.width * 0.4f // 总间距占40%
                        val totalBarWidth = size.width - totalSpacing // 柱状图总宽度占60%
                        val barWidth = totalBarWidth / barCount
                        val spacing = totalSpacing / (barCount + 1) // 每个间隔

                        data.forEachIndexed { index, week ->
                            val x = spacing + index * (barWidth + spacing)

                            // 堆叠柱状图 - 按实际数值等比例缩放
                            val weekTotal = week.carbs + week.protein + week.fat
                            val totalHeight = (weekTotal / maxValue * size.height).toFloat()
                            val carbsHeight: Float = if (weekTotal > 0) (week.carbs / weekTotal * totalHeight).toFloat() else 0f
                            val proteinHeight: Float = if (weekTotal > 0) (week.protein / weekTotal * totalHeight).toFloat() else 0f
                            val fatHeight: Float = if (weekTotal > 0) (week.fat / weekTotal * totalHeight).toFloat() else 0f

                            // 绘制脂肪（底部）
                            drawRect(
                                color = fatColor,
                                topLeft = Offset(x, size.height - fatHeight),
                                size = Size(barWidth, fatHeight)
                            )

                            // 绘制蛋白质（中间）
                            drawRect(
                                color = proteinColor,
                                topLeft = Offset(x, size.height - fatHeight - proteinHeight),
                                size = Size(barWidth, proteinHeight)
                            )

                            // 绘制碳水（顶部）
                            drawRect(
                                color = carbsColor,
                                topLeft = Offset(x, size.height - fatHeight - proteinHeight - carbsHeight),
                                size = Size(barWidth, carbsHeight)
                            )
                        }
                    }

                    // X轴标签 - 显示"第一周、第二周"等
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        data.forEach { week ->
                            Text(
                                text = week.weekLabel,
                                style = MaterialTheme.typography.bodySmall,
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
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AverageIntakeCard(
    avgCalories: Double,
    avgCarbs: Double,
    avgProtein: Double,
    avgFat: Double,
    period: Int
) {
    val periodLabel = if (period == 0) "每日" else "每周"

    // 使用统一的营养素颜色
    val carbsColor = NutrientColors.Carbs
    val proteinColor = NutrientColors.Protein
    val fatColor = NutrientColors.Fat

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "平均${periodLabel}摄入量",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "热量",
                    value = String.format(Locale.getDefault(), "%.0f", avgCalories),
                    unit = "kcal",
                    color = MaterialTheme.colorScheme.error
                )
                StatItem(
                    label = "碳水",
                    value = String.format(Locale.getDefault(), "%.0f", avgCarbs),
                    unit = "g",
                    color = carbsColor
                )
                StatItem(
                    label = "蛋白质",
                    value = String.format(Locale.getDefault(), "%.0f", avgProtein),
                    unit = "g",
                    color = proteinColor
                )
                StatItem(
                    label = "脂肪",
                    value = String.format(Locale.getDefault(), "%.0f", avgFat),
                    unit = "g",
                    color = fatColor
                )
            }
        }
    }
}

@Composable
private fun TotalIntakeCard(
    totalCalories: Double,
    totalCarbs: Double,
    totalProtein: Double,
    totalFat: Double,
    period: Int
) {
    val periodLabel = if (period == 0) "7天" else "4周"

    // 使用统一的营养素颜色
    val carbsColor = NutrientColors.Carbs
    val proteinColor = NutrientColors.Protein
    val fatColor = NutrientColors.Fat

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${periodLabel}摄入总量",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "热量",
                    value = String.format(Locale.getDefault(), "%.0f", totalCalories),
                    unit = "kcal",
                    color = MaterialTheme.colorScheme.error
                )
                StatItem(
                    label = "碳水",
                    value = String.format(Locale.getDefault(), "%.0f", totalCarbs),
                    unit = "g",
                    color = carbsColor
                )
                StatItem(
                    label = "蛋白质",
                    value = String.format(Locale.getDefault(), "%.0f", totalProtein),
                    unit = "g",
                    color = proteinColor
                )
                StatItem(
                    label = "脂肪",
                    value = String.format(Locale.getDefault(), "%.0f", totalFat),
                    unit = "g",
                    color = fatColor
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    unit: String,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}