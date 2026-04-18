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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyDataDetailScreen(
    viewModel: BodyDataDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val unitString = remember(uiState.dataType, uiState.unitMode) {
        when (uiState.dataType) {
            0, 2 -> if (uiState.unitMode == 1) "斤" else "kg"
            1 -> if (uiState.unitMode == 1) "斤" else "%" // OPML 规定前三个可转
            3, 4, 5 -> "cm"
            else -> ""
        }
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
                // 筛选区
                BodyFiltersBlock(uiState, viewModel)

                // 图表区
                BodyChartBlock(uiState, unitString)

                // 数据总结区块 1
                uiState.summary1?.let { sum1 ->
                    Summary1Block(sum1, unitString, uiState.startWeek, uiState.endWeek)
                }

                // 数据总结区块 2 (周与周的变化)
                if (uiState.summary2.isNotEmpty()) {
                    Summary2Block(uiState.summary2, unitString)
                }
                
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BodyFiltersBlock(
    uiState: BodyDataDetailUiState,
    viewModel: BodyDataDetailViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // 1. 数据类型
            Text("数据类型", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val types = listOf("体重", "体脂", "肌肉", "胸围", "腰围", "臀围")
                types.forEachIndexed { index, label ->
                    FilterChip(
                        selected = uiState.dataType == index,
                        onClick = { viewModel.updateFilters(dataType = index) },
                        label = { Text(label, fontSize = 12.sp) }
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // 2. 统计方式
            Text("统计方式", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.statMode == 0,
                    onClick = { viewModel.updateFilters(statMode = 0) },
                    label = { Text("平均数") }
                )
                FilterChip(
                    selected = uiState.statMode == 1,
                    onClick = { viewModel.updateFilters(statMode = 1) },
                    label = { Text("中位数") }
                )
            }

            // 3. 单位选择 (仅前三种数据可用)
            if (uiState.dataType <= 2) {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                Text("单位", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = uiState.unitMode == 0,
                        onClick = { viewModel.updateFilters(unitMode = 0) },
                        label = { Text("kg (或%)") }
                    )
                    FilterChip(
                        selected = uiState.unitMode == 1,
                        onClick = { viewModel.updateFilters(unitMode = 1) },
                        label = { Text("斤") }
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            // 4. 周区间选择 (下拉框或弹窗，这里用加减按钮简单实现)
            Text("区间选择 (第 ${uiState.startWeek} 周 - 第 ${uiState.endWeek} 周)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("起:", modifier = Modifier.padding(end = 8.dp))
                    FilledIconButton(
                        onClick = { viewModel.updateFilters(startWeek = uiState.startWeek - 1) },
                        enabled = uiState.startWeek > 1,
                        modifier = Modifier.size(32.dp)
                    ) { Text("-") }
                    Text("${uiState.startWeek}", modifier = Modifier.padding(horizontal = 8.dp))
                    FilledIconButton(
                        onClick = { viewModel.updateFilters(startWeek = uiState.startWeek + 1) },
                        enabled = uiState.startWeek < uiState.maxWeekNumber,
                        modifier = Modifier.size(32.dp)
                    ) { Text("+") }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("止:", modifier = Modifier.padding(end = 8.dp))
                    FilledIconButton(
                        onClick = { viewModel.updateFilters(endWeek = uiState.endWeek - 1) },
                        enabled = uiState.endWeek > 1,
                        modifier = Modifier.size(32.dp)
                    ) { Text("-") }
                    Text("${uiState.endWeek}", modifier = Modifier.padding(horizontal = 8.dp))
                    FilledIconButton(
                        onClick = { viewModel.updateFilters(endWeek = uiState.endWeek + 1) },
                        enabled = uiState.endWeek < uiState.maxWeekNumber,
                        modifier = Modifier.size(32.dp)
                    ) { Text("+") }
                }
            }
        }
    }
}

@Composable
private fun BodyChartBlock(uiState: BodyDataDetailUiState, unitString: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val typeStr = listOf("体重", "体脂", "肌肉", "胸围", "腰围", "臀围")[uiState.dataType]
            Text("${typeStr}趋势 ($unitString)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            var selectedPoint by remember { mutableStateOf<BodyChartPoint?>(null) }

            // 弹窗
            selectedPoint?.let { pt ->
                AlertDialog(
                    onDismissRequest = { selectedPoint = null },
                    title = { Text("第 ${pt.weekNumber} 周", fontWeight = FontWeight.Bold) },
                    text = {
                        Text("$typeStr: ${String.format(Locale.getDefault(), "%.1f", pt.value)} $unitString", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                    },
                    confirmButton = {
                        TextButton(onClick = { selectedPoint = null }) { Text("关闭") }
                    }
                )
            }

            val lineColor = MaterialTheme.colorScheme.primary
            val axisColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            val textColor = MaterialTheme.colorScheme.onSurface
            val textMeasurer = rememberTextMeasurer()

            val validPoints = uiState.chartData.filter { !it.isDummy }
            if (validPoints.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("该区间内无数据", color = axisColor)
                }
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .pointerInput(uiState.chartData) {
                            detectTapGestures { offset ->
                                val yAxisWidth = 40.dp.toPx()
                                val chartWidth = size.width - yAxisWidth
                                val spacing = chartWidth / (uiState.chartData.size.toFloat().coerceAtLeast(1f))
                                val clickX = offset.x
                                
                                if (clickX > yAxisWidth) {
                                    val index = ((clickX - yAxisWidth) / spacing).toInt().coerceIn(0, uiState.chartData.lastIndex)
                                    val point = uiState.chartData[index]
                                    if (!point.isDummy) {
                                        val pointX = yAxisWidth + index * spacing + spacing / 2f
                                        if (abs(clickX - pointX) < spacing) {
                                            selectedPoint = point
                                        }
                                    }
                                }
                            }
                        }
                ) {
                    val yAxisWidth = 40.dp.toPx()
                    val xAxisHeight = 24.dp.toPx()
                    val chartHeight = size.height - xAxisHeight
                    val chartWidth = size.width - yAxisWidth

                    val minVal = validPoints.minOf { it.value } * 0.9f
                    val maxVal = validPoints.maxOf { it.value } * 1.1f
                    val range = if (maxVal - minVal == 0f) 10f else maxVal - minVal

                    // Draw Y axis steps
                    val ySteps = 4
                    for (i in 0..ySteps) {
                        val yVal = maxVal - (range * i / ySteps)
                        val yPos = chartHeight * i / ySteps

                        drawLine(
                            color = axisColor.copy(alpha = 0.2f),
                            start = Offset(yAxisWidth, yPos),
                            end = Offset(size.width, yPos),
                            strokeWidth = 1.dp.toPx()
                        )

                        drawText(
                            textMeasurer = textMeasurer,
                            text = String.format(Locale.getDefault(), "%.1f", yVal),
                            topLeft = Offset(0f, yPos - 8.dp.toPx()),
                            style = TextStyle(color = axisColor, fontSize = 10.sp, textAlign = TextAlign.End)
                        )
                    }

                    // Draw X axis labels and lines
                    val barCount = uiState.chartData.size
                    val spacing = chartWidth / barCount.toFloat()
                    val renderPoints = mutableListOf<Offset>()

                    uiState.chartData.forEachIndexed { index, pt ->
                        val xCenter = yAxisWidth + index * spacing + spacing / 2f
                        
                        // X Label
                        val labelResult = textMeasurer.measure("${pt.weekNumber}周", style = TextStyle(color = textColor, fontSize = 10.sp))
                        drawText(
                            textLayoutResult = labelResult,
                            topLeft = Offset(xCenter - labelResult.size.width / 2f, chartHeight + 8.dp.toPx())
                        )

                        if (!pt.isDummy) {
                            val yCenter = chartHeight - ((pt.value - minVal) / range * chartHeight)
                            renderPoints.add(Offset(xCenter, yCenter))
                            
                            // Draw dot (小点)
                            drawCircle(color = lineColor, radius = 6.dp.toPx(), center = Offset(xCenter, yCenter))
                            drawCircle(color = Color.White, radius = 3.dp.toPx(), center = Offset(xCenter, yCenter))
                        }
                    }

                    // Draw Path
                    if (renderPoints.size >= 2) {
                        val path = Path()
                        path.moveTo(renderPoints.first().x, renderPoints.first().y)
                        for (i in 1 until renderPoints.size) {
                            path.lineTo(renderPoints[i].x, renderPoints[i].y)
                        }
                        drawPath(path = path, color = lineColor, style = Stroke(width = 2.dp.toPx()))
                    }

                    // Draw Custom Tooltip for selectedPoint
                    selectedPoint?.let { pt ->
                        val index = uiState.chartData.indexOf(pt)
                        if (index != -1 && !pt.isDummy) {
                            val xCenter = yAxisWidth + index * spacing + spacing / 2f
                            val yCenter = chartHeight - ((pt.value - minVal) / range * chartHeight)

                            // 1. Draw vertical line from point to X axis
                            drawLine(
                                color = axisColor,
                                start = Offset(xCenter, yCenter),
                                end = Offset(xCenter, chartHeight),
                                strokeWidth = 1.5f.dp.toPx()
                            )

                            // Highlight selected point
                            drawCircle(color = lineColor, radius = 8.dp.toPx(), center = Offset(xCenter, yCenter))
                            drawCircle(color = Color.White, radius = 5.dp.toPx(), center = Offset(xCenter, yCenter))

                            // 2. Draw Tooltip Box
                            val topText = "第 ${pt.weekNumber} 周"
                            val bottomText = "${String.format(Locale.getDefault(), "%.1f", pt.value)} $unitString"

                            val topResult = textMeasurer.measure(
                                topText,
                                style = TextStyle(color = Color.LightGray, fontSize = 12.sp)
                            )
                            val bottomResult = textMeasurer.measure(
                                bottomText,
                                style = TextStyle(color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            )

                            val paddingX = 12.dp.toPx()
                            val paddingY = 8.dp.toPx()
                            val gapY = 4.dp.toPx()

                            val boxWidth = maxOf(topResult.size.width, bottomResult.size.width) + paddingX * 2
                            val boxHeight = topResult.size.height + bottomResult.size.height + gapY + paddingY * 2

                            // Calculate tooltip position (above the point, centered horizontally)
                            var boxLeft = xCenter - boxWidth / 2f
                            var boxTop = yCenter - boxHeight - 16.dp.toPx()

                            // Keep box within canvas bounds
                            if (boxLeft < yAxisWidth) boxLeft = yAxisWidth
                            if (boxLeft + boxWidth > size.width) boxLeft = size.width - boxWidth
                            if (boxTop < 0) boxTop = yCenter + 16.dp.toPx() // flip below if no space

                            // Draw rounded rect background
                            drawRoundRect(
                                color = Color(0xFF4A4453), // Dark slightly purplish gray
                                topLeft = Offset(boxLeft, boxTop),
                                size = androidx.compose.ui.geometry.Size(boxWidth, boxHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx(), 8.dp.toPx())
                            )

                            // Draw text
                            drawText(
                                textLayoutResult = topResult,
                                topLeft = Offset(boxLeft + paddingX, boxTop + paddingY)
                            )
                            drawText(
                                textLayoutResult = bottomResult,
                                topLeft = Offset(boxLeft + paddingX, boxTop + paddingY + topResult.size.height + gapY)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Summary1Block(sum1: BodySummary1, unit: String, startWeek: Int, endWeek: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("区块1：第${startWeek}周 到 第${endWeek}周 统计", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val changeStr = if (sum1.changeValue > 0) "上升" else "下降"
                val changeSign = if (sum1.changeValue > 0) "+" else ""
                
                StatCardItem("$changeStr(值)", "$changeSign${String.format(Locale.getDefault(), "%.1f", sum1.changeValue)}", unit)
                StatCardItem("$changeStr(%)", "$changeSign${String.format(Locale.getDefault(), "%.1f", sum1.changePercent)}", "%")
                StatCardItem("区间最大", String.format(Locale.getDefault(), "%.1f", sum1.maxValue), unit)
                StatCardItem("区间最小", String.format(Locale.getDefault(), "%.1f", sum1.minValue), unit)
            }
        }
    }
}

@Composable
private fun StatCardItem(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(unit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun Summary2Block(changes: List<WeekToWeekChange>, unit: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("区块2：周环比变化", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            
            changes.forEach { change ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${change.fromWeek} → ${change.toWeek}", style = MaterialTheme.typography.bodyMedium)
                    
                    val arrow = if (change.changeValue > 0) "↑" else if (change.changeValue < 0) "↓" else "-"
                    val color = if (change.changeValue > 0) Color(0xFFE53935) else if (change.changeValue < 0) Color(0xFF43A047) else MaterialTheme.colorScheme.onSurface
                    
                    Text(
                        text = "$arrow ${String.format(Locale.getDefault(), "%.1f", abs(change.changeValue))} $unit",
                        style = MaterialTheme.typography.bodyMedium,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            }
        }
    }
}