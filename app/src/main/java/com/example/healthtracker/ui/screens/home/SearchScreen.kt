package com.example.healthtracker.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.healthtracker.ui.components.IntakeRecordItem
import com.example.healthtracker.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEditIntake: (Long) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val selectedMealTypes by viewModel.selectedMealTypes.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()

    var showFilterDialog by remember { mutableStateOf(false) }

    if (showFilterDialog) {
        SearchFilterDialog(
            selectedMealTypes = selectedMealTypes,
            startDate = startDate,
            endDate = endDate,
            onDismiss = { showFilterDialog = false },
            onApply = { mealTypes, start, end ->
                viewModel.setDateRange(start, end)
                // ViewModel toggleMealType is already handled if we use it directly, 
                // but for a dialog it's better to pass the whole set
                showFilterDialog = false
            },
            viewModel = viewModel
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        placeholder = { Text("搜索食物名称") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        singleLine = true,
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "清除")
                                }
                            }
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "筛选",
                            tint = if (selectedMealTypes.isNotEmpty() || startDate != null || endDate != null)
                                MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (searchQuery.isEmpty() && searchResults.isEmpty() && selectedMealTypes.isEmpty() && startDate == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("输入名称开始搜索", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else if (searchResults.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("未找到相关记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 按日期分组显示
                    val groupedResults = searchResults.groupBy { it.date }
                    groupedResults.forEach { (date, records) ->
                        item {
                            Text(
                                text = DateTimeUtils.formatDate(date),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(records) { record ->
                            IntakeRecordItem(
                                record = record,
                                showMacros = true,
                                showMealType = true,
                                onClick = { onNavigateToEditIntake(record.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFilterDialog(
    selectedMealTypes: Set<Int>,
    startDate: Long?,
    endDate: Long?,
    onDismiss: () -> Unit,
    onApply: (Set<Int>, Long?, Long?) -> Unit,
    viewModel: SearchViewModel
) {
    val mealTypes = listOf("早餐", "午餐", "晚餐", "加餐")
    
    // 日期选择状态
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    if (showStartPicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setDateRange(datePickerState.selectedDateMillis, endDate)
                    showStartPicker = false
                }) { Text("确定") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndPicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = endDate ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setDateRange(startDate, datePickerState.selectedDateMillis)
                    showEndPicker = false
                }) { Text("确定") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("筛选记录") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("餐次", style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    mealTypes.forEachIndexed { index, name ->
                        FilterChip(
                            selected = selectedMealTypes.contains(index),
                            onClick = { viewModel.toggleMealType(index) },
                            label = { Text(name) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Text("日期范围", style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedCard(
                        onClick = { showStartPicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("开始日期", style = MaterialTheme.typography.labelSmall)
                            Text(if (startDate != null) DateTimeUtils.formatDate(startDate) else "不限")
                        }
                    }
                    OutlinedCard(
                        onClick = { showEndPicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("结束日期", style = MaterialTheme.typography.labelSmall)
                            Text(if (endDate != null) DateTimeUtils.formatDate(endDate) else "不限")
                        }
                    }
                }
                
                TextButton(
                    onClick = { viewModel.clearFilters() },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("重置筛选")
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("完成")
            }
        }
    )
}
