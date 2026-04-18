package com.example.healthtracker.ui.screens.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
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
                    TextButton(onClick = { viewModel.goToCurrentWeek() }) {
                        Text("回到本周")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header (Week Navigator)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var showWeekPicker by remember { mutableStateOf(false) }

                    if (showWeekPicker) {
                        var targetWeekStr by remember { mutableStateOf(uiState.weekNumber.toString()) }
                        AlertDialog(
                            onDismissRequest = { showWeekPicker = false },
                            title = { Text("跳转到指定周", fontWeight = FontWeight.Bold) },
                            text = {
                                OutlinedTextField(
                                    value = targetWeekStr,
                                    onValueChange = { targetWeekStr = it },
                                    label = { Text("输入周数 (当前年)") },
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                    ),
                                    singleLine = true
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = { 
                                    val targetWeek = targetWeekStr.toIntOrNull()
                                    if (targetWeek != null && targetWeek > 0) {
                                        viewModel.jumpToWeek(targetWeek)
                                    }
                                    showWeekPicker = false 
                                }) { Text("确定") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showWeekPicker = false }) { Text("取消") }
                            }
                        )
                    }

                    IconButton(onClick = { viewModel.previousWeek() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "上一周")
                    }
                    Text(
                        text = "第 ${uiState.weekNumber} 周",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { showWeekPicker = true }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    IconButton(onClick = { viewModel.nextWeek() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "下一周")
                    }
                }

                // Chart
                IntakeChartCard(uiState)

                // Stats Section 1: Avg per day
                NutritionStatCard(
                    title = "日均摄入 (基于实际记录)",
                    calories = uiState.avgDailyCalories,
                    carbs = uiState.avgDailyCarbs,
                    protein = uiState.avgDailyProtein,
                    fat = uiState.avgDailyFat
                )

                // Stats Section 2: Avg per meal
                NutritionStatCard(
                    title = "餐均摄入 (基于实际记录)",
                    calories = uiState.avgMealCalories,
                    carbs = uiState.avgMealCarbs,
                    protein = uiState.avgMealProtein,
                    fat = uiState.avgMealFat
                )
                
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun IntakeChartCard(uiState: NutritionDetailUiState) {
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("热量及营养素柱状图", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            val carbsColor = NutrientColors.Carbs
            val proteinColor = NutrientColors.Protein
            val fatColor = NutrientColors.Fat
            val targetColor = Color.Red
            val axisColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            val textColor = MaterialTheme.colorScheme.onSurface

            val textMeasurer = rememberTextMeasurer()

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .pointerInput(uiState.dailyData) {
                        detectTapGestures { offset ->
                            val width = size.width
                            val yAxisWidth = 40.dp.toPx()
                            val chartWidth = width - yAxisWidth
                            val barWidth = chartWidth / 14f
                            val spacing = chartWidth / 7f

                            val x = offset.x
                            if (x > yAxisWidth) {
                                val index = ((x - yAxisWidth) / spacing).toInt()
                                if (index in 0..6) {
                                    val barCenterX = yAxisWidth + index * spacing + spacing / 2f
                                    if (kotlin.math.abs(x - barCenterX) <= barWidth) {
                                        selectedItem = uiState.dailyData[index]
                                    }
                                }
                            }
                        }
                    }
            ) {
                val maxCals = maxOf(
                    uiState.targetCalories,
                    uiState.dailyData.maxOfOrNull { it.calories } ?: 0f
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
                if (uiState.targetCalories > 0) {
                    val targetY = chartHeight - (chartHeight * uiState.targetCalories / maxAxisValue)
                    drawLine(
                        color = targetColor,
                        start = Offset(yAxisWidth, targetY),
                        end = Offset(size.width, targetY),
                        strokeWidth = 2.dp.toPx()
                    )
                }

                // Draw bars and X labels
                val spacing = chartWidth / 7f
                val barWidth = chartWidth / 14f

                uiState.dailyData.forEachIndexed { index, day ->
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
                NutritionLegendItem("碳水", carbsColor)
                Spacer(Modifier.width(16.dp))
                NutritionLegendItem("蛋白质", proteinColor)
                Spacer(Modifier.width(16.dp))
                NutritionLegendItem("脂肪", fatColor)
                Spacer(Modifier.width(16.dp))
                NutritionLegendItem("目标热量", targetColor, isLine = true)
            }
        }
    }
}

@Composable
private fun NutritionLegendItem(label: String, color: Color, isLine: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isLine) {
            Box(Modifier.width(16.dp).height(2.dp).background(color))
        } else {
            Box(Modifier.size(12.dp).background(color, RoundedCornerShape(2.dp)))
        }
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun NutritionStatCard(
    title: String,
    calories: Float,
    carbs: Float,
    protein: Float,
    fat: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                NutritionStatItem("热量", calories, "kcal", MaterialTheme.colorScheme.error)
                NutritionStatItem("碳水", carbs, "g", NutrientColors.Carbs)
                NutritionStatItem("蛋白质", protein, "g", NutrientColors.Protein)
                NutritionStatItem("脂肪", fat, "g", NutrientColors.Fat)
            }
        }
    }
}

@Composable
private fun NutritionStatItem(label: String, value: Float, unit: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            String.format(Locale.getDefault(), "%.1f", value), 
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(unit, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
