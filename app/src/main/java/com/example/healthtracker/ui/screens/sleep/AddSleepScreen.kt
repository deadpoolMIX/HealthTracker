package com.example.healthtracker.ui.screens.sleep

import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
                .padding(16.dp)
        ) {
            // 入睡时间区域
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "入睡时间",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 入睡日期选择
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = DateTimeUtils.formatDate(uiState.sleepDate),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        IconButton(onClick = { showSleepDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "选择日期")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 滑动转轮时间选择器
                    WheelTimePicker(
                        initialHour = uiState.sleepHour,
                        initialMinute = uiState.sleepMinute,
                        onTimeSelected = { hour, minute ->
                            viewModel.setSleepHour(hour)
                            viewModel.setSleepMinute(minute)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 起床时间区域
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "起床时间",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 起床日期选择
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = DateTimeUtils.formatDate(uiState.wakeDate),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        IconButton(onClick = { showWakeDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "选择日期")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 滑动转轮时间选择器
                    WheelTimePicker(
                        initialHour = uiState.wakeHour,
                        initialMinute = uiState.wakeMinute,
                        onTimeSelected = { hour, minute ->
                            viewModel.setWakeHour(hour)
                            viewModel.setWakeMinute(minute)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 睡眠时长显示
            val sleepCalendar = Calendar.getInstance()
            sleepCalendar.timeInMillis = uiState.sleepDate
            sleepCalendar.set(Calendar.HOUR_OF_DAY, uiState.sleepHour)
            sleepCalendar.set(Calendar.MINUTE, uiState.sleepMinute)

            val wakeCalendar = Calendar.getInstance()
            wakeCalendar.timeInMillis = uiState.wakeDate
            wakeCalendar.set(Calendar.HOUR_OF_DAY, uiState.wakeHour)
            wakeCalendar.set(Calendar.MINUTE, uiState.wakeMinute)

            val durationMinutes = (wakeCalendar.timeInMillis - sleepCalendar.timeInMillis) / (1000 * 60)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "预计睡眠时长",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (durationMinutes >= 0) {
                            "${durationMinutes / 60}小时${durationMinutes % 60}分钟"
                        } else {
                            "时间设置无效"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (durationMinutes >= 0) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.save() },
                modifier = Modifier.fillMaxWidth(),
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
 * 滑动转轮时间选择器
 */
@Composable
private fun WheelTimePicker(
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (Int, Int) -> Unit
) {
    var selectedHour by remember { mutableIntStateOf(initialHour) }
    var selectedMinute by remember { mutableIntStateOf(initialMinute) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 小时选择器
        WheelPicker(
            items = (0..23).map { String.format("%02d", it) },
            initialIndex = initialHour,
            onSelectedChange = { index ->
                selectedHour = index
                onTimeSelected(index, selectedMinute)
            },
            modifier = Modifier.width(80.dp)
        )

        Text(
            text = ":",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // 分钟选择器
        WheelPicker(
            items = (0..59).map { String.format("%02d", it) },
            initialIndex = initialMinute,
            onSelectedChange = { index ->
                selectedMinute = index
                onTimeSelected(selectedHour, index)
            },
            modifier = Modifier.width(80.dp)
        )
    }
}

/**
 * 滑动转轮选择器
 */
@Composable
private fun WheelPicker(
    items: List<String>,
    initialIndex: Int,
    onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = maxOf(0, initialIndex - 2))
    val snappingLayout = remember(listState) { SnapLayoutInfoProvider(listState) }
    val flingBehavior = rememberSnapFlingBehavior(snappingLayout)

    // 监听滚动位置变化
    LaunchedEffect(listState.firstVisibleItemIndex) {
        onSelectedChange(listState.firstVisibleItemIndex + 2)
    }

    Box(
        modifier = modifier.height(150.dp)
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
                                Color.White.copy(alpha = 0.9f),
                                Color.Transparent,
                                Color.Transparent,
                                Color.White.copy(alpha = 0.9f)
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
            contentPadding = PaddingValues(top = 50.dp, bottom = 50.dp)
        ) {
            items(items.size + 4) { index ->
                val actualIndex = index - 2
                val isSelected = actualIndex == listState.firstVisibleItemIndex + 2

                Box(
                    modifier = Modifier
                        .height(50.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (actualIndex in items.indices) {
                        Text(
                            text = items[actualIndex],
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // 选中行指示器
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .height(50.dp)
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )
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