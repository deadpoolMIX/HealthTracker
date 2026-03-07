package com.example.healthtracker.ui.screens.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.healthtracker.util.DateTimeUtils
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
    val carbsColor = MaterialTheme.colorScheme.primary
    val proteinColor = MaterialTheme.colorScheme.secondary
    val fatColor = MaterialTheme.colorScheme.tertiary

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
                val maxValue = data.maxOf {
                    maxOf(it.carbs, it.protein, it.fat)
                }.toFloat().coerceAtLeast(1f)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    val barWidth = size.width / data.size * 0.6f
                    val spacing = size.width / data.size * 0.4f

                    data.forEachIndexed { index, day ->
                        val x = index * (barWidth + spacing) + spacing / 2

                        // 堆叠柱状图
                        val totalHeight = ((day.carbs + day.protein + day.fat) / maxValue * size.height).toFloat()
                        val carbsHeight = (day.carbs / maxValue * size.height).toFloat()
                        val proteinHeight = (day.protein / maxValue * size.height).toFloat()
                        val fatHeight = (day.fat / maxValue * size.height).toFloat()

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

                // X轴标签
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
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NutritionWeeklyChartCard(
    title: String,
    data: List<WeeklyNutrition>
) {
    val carbsColor = MaterialTheme.colorScheme.primary
    val proteinColor = MaterialTheme.colorScheme.secondary
    val fatColor = MaterialTheme.colorScheme.tertiary

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
                val maxValue = data.maxOf {
                    maxOf(it.carbs, it.protein, it.fat)
                }.toFloat().coerceAtLeast(1f)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    val barWidth = size.width / data.size * 0.5f
                    val spacing = size.width / data.size * 0.5f

                    data.forEachIndexed { index, week ->
                        val x = index * (barWidth + spacing) + spacing / 2

                        // 堆叠柱状图
                        val carbsHeight = (week.carbs / maxValue * size.height).toFloat()
                        val proteinHeight = (week.protein / maxValue * size.height).toFloat()
                        val fatHeight = (week.fat / maxValue * size.height).toFloat()

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

                // X轴标签
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
                            textAlign = TextAlign.Center
                        )
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
                    color = MaterialTheme.colorScheme.primary
                )
                StatItem(
                    label = "蛋白质",
                    value = String.format(Locale.getDefault(), "%.0f", avgProtein),
                    unit = "g",
                    color = MaterialTheme.colorScheme.secondary
                )
                StatItem(
                    label = "脂肪",
                    value = String.format(Locale.getDefault(), "%.0f", avgFat),
                    unit = "g",
                    color = MaterialTheme.colorScheme.tertiary
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
                    color = MaterialTheme.colorScheme.primary
                )
                StatItem(
                    label = "蛋白质",
                    value = String.format(Locale.getDefault(), "%.0f", totalProtein),
                    unit = "g",
                    color = MaterialTheme.colorScheme.secondary
                )
                StatItem(
                    label = "脂肪",
                    value = String.format(Locale.getDefault(), "%.0f", totalFat),
                    unit = "g",
                    color = MaterialTheme.colorScheme.tertiary
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
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}