package com.example.healthtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSleepScreen(
    onNavigateBack: () -> Unit
) {
    val calendar = Calendar.getInstance()
    var sleepHour by remember { mutableIntStateOf(23) }
    var sleepMinute by remember { mutableIntStateOf(0) }
    var wakeHour by remember { mutableIntStateOf(7) }
    var wakeMinute by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("记录睡眠", fontWeight = FontWeight.Medium) },
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
            // 入睡时间
            Text(
                text = "入睡时间",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 入睡小时
                var sleepHourExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = sleepHourExpanded,
                    onExpandedChange = { sleepHourExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = String.format("%02d", sleepHour),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("时") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sleepHourExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = sleepHourExpanded,
                        onDismissRequest = { sleepHourExpanded = false }
                    ) {
                        (0..23).forEach { hour ->
                            DropdownMenuItem(
                                text = { Text(String.format("%02d", hour)) },
                                onClick = {
                                    sleepHour = hour
                                    sleepHourExpanded = false
                                }
                            )
                        }
                    }
                }

                // 入睡分钟
                var sleepMinuteExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = sleepMinuteExpanded,
                    onExpandedChange = { sleepMinuteExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = String.format("%02d", sleepMinute),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("分") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sleepMinuteExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = sleepMinuteExpanded,
                        onDismissRequest = { sleepMinuteExpanded = false }
                    ) {
                        (0..59 step 5).forEach { minute ->
                            DropdownMenuItem(
                                text = { Text(String.format("%02d", minute)) },
                                onClick = {
                                    sleepMinute = minute
                                    sleepMinuteExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 起床时间
            Text(
                text = "起床时间",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 起床小时
                var wakeHourExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = wakeHourExpanded,
                    onExpandedChange = { wakeHourExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = String.format("%02d", wakeHour),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("时") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = wakeHourExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = wakeHourExpanded,
                        onDismissRequest = { wakeHourExpanded = false }
                    ) {
                        (0..23).forEach { hour ->
                            DropdownMenuItem(
                                text = { Text(String.format("%02d", hour)) },
                                onClick = {
                                    wakeHour = hour
                                    wakeHourExpanded = false
                                }
                            )
                        }
                    }
                }

                // 起床分钟
                var wakeMinuteExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = wakeMinuteExpanded,
                    onExpandedChange = { wakeMinuteExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = String.format("%02d", wakeMinute),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("分") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = wakeMinuteExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = wakeMinuteExpanded,
                        onDismissRequest = { wakeMinuteExpanded = false }
                    ) {
                        (0..59 step 5).forEach { minute ->
                            DropdownMenuItem(
                                text = { Text(String.format("%02d", minute)) },
                                onClick = {
                                    wakeMinute = minute
                                    wakeMinuteExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 睡眠时长显示
            val sleepMinutes = calculateSleepDuration(sleepHour, sleepMinute, wakeHour, wakeMinute)
            Text(
                text = "预计睡眠时长: ${sleepMinutes / 60}小时${sleepMinutes % 60}分钟",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Button(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存记录")
            }
        }
    }
}

private fun calculateSleepDuration(
    sleepHour: Int,
    sleepMinute: Int,
    wakeHour: Int,
    wakeMinute: Int
): Long {
    var totalMinutes = (wakeHour * 60 + wakeMinute) - (sleepHour * 60 + sleepMinute)
    if (totalMinutes < 0) {
        totalMinutes += 24 * 60 // 跨天
    }
    return totalMinutes.toLong()
}