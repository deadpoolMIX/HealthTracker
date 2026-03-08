package com.example.healthtracker.ui.screens.sleep

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    // 时间选择对话框
    var showSleepTimePicker by remember { mutableStateOf(false) }
    var showWakeTimePicker by remember { mutableStateOf(false) }

    // 时间选择器状态
    val sleepTimePickerState = rememberTimePickerState(
        initialHour = uiState.sleepHour,
        initialMinute = uiState.sleepMinute,
        is24Hour = true
    )
    val wakeTimePickerState = rememberTimePickerState(
        initialHour = uiState.wakeHour,
        initialMinute = uiState.wakeMinute,
        is24Hour = true
    )

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

    if (showSleepTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showSleepTimePicker = false },
            onConfirm = {
                viewModel.setSleepHour(sleepTimePickerState.hour)
                viewModel.setSleepMinute(sleepTimePickerState.minute)
                showSleepTimePicker = false
            },
            timePickerState = sleepTimePickerState
        )
    }

    if (showWakeTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showWakeTimePicker = false },
            onConfirm = {
                viewModel.setWakeHour(wakeTimePickerState.hour)
                viewModel.setWakeMinute(wakeTimePickerState.minute)
                showWakeTimePicker = false
            },
            timePickerState = wakeTimePickerState
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
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 入睡时间卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "入睡时间",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 日期选择
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "日期：",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { showSleepDatePicker = true }) {
                            Text(DateTimeUtils.formatDateShort(uiState.sleepDate))
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 时间选择
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "时间：",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { showSleepTimePicker = true }) {
                            Text(
                                text = String.format("%02d:%02d", uiState.sleepHour, uiState.sleepMinute),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 起床时间卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "起床时间",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 日期选择
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "日期：",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { showWakeDatePicker = true }) {
                            Text(DateTimeUtils.formatDateShort(uiState.wakeDate))
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 时间选择
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "时间：",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { showWakeTimePicker = true }) {
                            Text(
                                text = String.format("%02d:%02d", uiState.wakeHour, uiState.wakeMinute),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
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
                        .padding(horizontal = 16.dp, vertical = 16.dp),
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
 * 时间选择对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    timePickerState: TimePickerState
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("取消")
            }
        },
        text = {
            TimePicker(
                state = timePickerState,
                modifier = Modifier.padding(8.dp)
            )
        }
    )
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