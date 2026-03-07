package com.example.healthtracker.ui.screens.sleep

import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.healthtracker.util.DateTimeUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSleepScreen(
    viewModel: SleepDataViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // 保存成功后返回
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onNavigateBack()
        }
    }

    // 日期选择对话框
    var showSleepDatePicker by remember { mutableStateOf(false) }
    var showWakeDatePicker by remember { mutableStateOf(false) }

    if (showSleepDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showSleepDatePicker = false },
            onDateSelected = { date ->
                viewModel.setSleepDate(date)
                showSleepDatePicker = false
            },
            selectedDate = uiState.sleepDate
        )
    }

    if (showWakeDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showWakeDatePicker = false },
            onDateSelected = { date ->
                viewModel.setWakeDate(date)
                showWakeDatePicker = false
            },
            selectedDate = uiState.wakeDate
        )
    }

    // 计算睡眠时长
    val sleepCalendar = Calendar.getInstance()
    sleepCalendar.timeInMillis = uiState.sleepDate
    sleepCalendar.set(Calendar.HOUR_OF_DAY, uiState.sleepHour)
    sleepCalendar.set(Calendar.MINUTE, uiState.sleepMinute)

    val wakeCalendar = Calendar.getInstance()
    wakeCalendar.timeInMillis = uiState.wakeDate
    wakeCalendar.set(Calendar.HOUR_OF_DAY, uiState.wakeHour)
    wakeCalendar.set(Calendar.MINUTE, uiState.wakeMinute)

    val durationMinutes = (wakeCalendar.timeInMillis - sleepCalendar.timeInMillis) / (1000 * 60)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.existingRecord != null) "编辑睡眠记录" else "记录睡眠",
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 入睡时间行
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "入睡",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(50.dp)
                )

                // 日期选择
                TextButton(onClick = { showSleepDatePicker = true }) {
                    Text(
                        text = DateTimeUtils.formatDateShort(uiState.sleepDate),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // 时间滚轮
                CompactTimePicker(
                    initialHour = uiState.sleepHour,
                    initialMinute = uiState.sleepMinute,
                    onTimeSelected = { hour, minute ->
                        viewModel.setSleepHour(hour)
                        viewModel.setSleepMinute(minute)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 起床时间行
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "起床",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(50.dp)
                )

                // 日期选择
                TextButton(onClick = { showWakeDatePicker = true }) {
                    Text(
                        text = DateTimeUtils.formatDateShort(uiState.wakeDate),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // 时间滚轮
                CompactTimePicker(
                    initialHour = uiState.wakeHour,
                    initialMinute = uiState.wakeMinute,
                    onTimeSelected = { hour, minute ->
                        viewModel.setWakeHour(hour)
                        viewModel.setWakeMinute(minute)
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 睡眠时长显示
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "睡眠时长：",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (durationMinutes >= 0) {
                            "${durationMinutes / 60}小时${durationMinutes % 60}分钟"
                        } else {
                            "时间无效"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (durationMinutes >= 0) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 保存按钮
            Button(
                onClick = { viewModel.save() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                enabled = !uiState.isSaving && durationMinutes > 0
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("保存中...")
                } else {
                    Text(if (uiState.existingRecord != null) "更新记录" else "保存记录")
                }
            }
        }
    }
}

/**
 * 紧凑型时间选择器（滚动滚轮）
 */
@Composable
private fun CompactTimePicker(
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (Int, Int) -> Unit
) {
    var selectedHour by remember { mutableIntStateOf(initialHour) }
    var selectedMinute by remember { mutableIntStateOf(initialMinute) }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 小时滚轮
        CompactWheelPicker(
            items = (0..23).map { String.format("%02d", it) },
            initialIndex = initialHour,
            onSelectedChange = { index ->
                selectedHour = index
                onTimeSelected(index, selectedMinute)
            },
            modifier = Modifier.width(56.dp)
        )

        Text(
            text = ":",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 2.dp)
        )

        // 分钟滚轮
        CompactWheelPicker(
            items = (0..59).map { String.format("%02d", it) },
            initialIndex = initialMinute,
            onSelectedChange = { index ->
                selectedMinute = index
                onTimeSelected(selectedHour, index)
            },
            modifier = Modifier.width(56.dp)
        )
    }
}

/**
 * 紧凑型滚动选择器
 */
@Composable
private fun CompactWheelPicker(
    items: List<String>,
    initialIndex: Int,
    onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = maxOf(0, initialIndex - 1))
    val snappingLayout = remember(listState) { SnapLayoutInfoProvider(listState) }
    val flingBehavior = rememberSnapFlingBehavior(snappingLayout)

    // 监听滚动位置变化
    LaunchedEffect(listState.firstVisibleItemIndex) {
        onSelectedChange(listState.firstVisibleItemIndex + 1)
    }

    Box(
        modifier = modifier.height(80.dp)
    ) {
        // 渐变遮罩
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.85f),
                                Color.Transparent,
                                Color.White.copy(alpha = 0.85f)
                            ),
                            startY = 0f,
                            endY = size.height
                        )
                    )
                }
        )

        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp)
        ) {
            items(items.size + 2) { index ->
                val actualIndex = index - 1
                val isSelected = actualIndex == listState.firstVisibleItemIndex + 1

                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (actualIndex in items.indices) {
                        Text(
                            text = items[actualIndex],
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center,
                            fontSize = if (isSelected) 18.sp else 14.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * 日期选择对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (Long) -> Unit,
    selectedDate: Long
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate
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