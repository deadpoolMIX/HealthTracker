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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.healthtracker.ui.components.IntakeRecordItem
import com.example.healthtracker.ui.components.getMealTypeName
import com.example.healthtracker.util.DateTimeUtils
import kotlin.math.roundToInt

import androidx.compose.ui.tooling.preview.Preview
import com.example.healthtracker.ui.theme.HealthTrackerTheme
import com.example.healthtracker.data.local.entity.IntakeRecordEntity

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    HealthTrackerTheme {
        SearchScreenContent(
            searchQuery = "牛肉",
            searchResults = listOf(
                IntakeRecordEntity(
                    id = 1,
                    foodName = "牛肉",
                    foodIcon = "🥩",
                    amount = 100.0,
                    calories = 250.0,
                    carbohydrates = 0.0,
                    protein = 26.0,
                    fat = 15.0,
                    mealType = 1,
                    date = 1713369600000L // 2024-04-18
                ),
                IntakeRecordEntity(
                    id = 2,
                    foodName = "牛肉面",
                    foodIcon = "🍜",
                    amount = 400.0,
                    calories = 450.0,
                    carbohydrates = 60.0,
                    protein = 20.0,
                    fat = 12.0,
                    mealType = 1,
                    date = 1713283200000L // 2024-04-17
                )
            ),
            selectedMealTypes = emptySet(),
            startDate = null,
            endDate = null,
            onSearchQueryChange = {},
            onNavigateBack = {},
            onNavigateToEditIntake = {},
            onFilterApply = { _, _, _ -> },
            onClearSearch = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreenContent(
    searchQuery: String,
    searchResults: List<IntakeRecordEntity>,
    selectedMealTypes: Set<Int>,
    startDate: Long?,
    endDate: Long?,
    onSearchQueryChange: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToEditIntake: (Long) -> Unit,
    onFilterApply: (Set<Int>, Long?, Long?) -> Unit,
    onClearSearch: () -> Unit
) {
    var showFilterDialog by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        if (searchQuery.isEmpty()) {
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("搜索食物名称") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
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
                                IconButton(onClick = onClearSearch) {
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
        val mealCardColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val groupedByDate = searchResults.groupBy { it.date }
                    groupedByDate.forEach { (date, dateRecords) ->
                        item {
                            Text(
                                text = DateTimeUtils.formatDate(date),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        val groupedByMeal = dateRecords.groupBy { it.mealType }
                        groupedByMeal.keys.sorted().forEach { mealType ->
                            val records = groupedByMeal[mealType] ?: emptyList()
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = mealCardColor)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = getMealTypeName(mealType),
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            Text(
                                                text = "C:${records.sumOf { it.carbohydrates.roundToInt() }} P:${records.sumOf { it.protein.roundToInt() }} F:${records.sumOf { it.fat.roundToInt() }}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "${records.sumOf { it.calories.roundToInt() }} kcal",
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        
                                        records.forEach { record ->
                                            IntakeRecordItem(
                                                record = record,
                                                showMacros = false,
                                                showMealType = false,
                                                onClick = { onNavigateToEditIntake(record.id) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

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

    SearchScreenContent(
        searchQuery = searchQuery,
        searchResults = searchResults,
        selectedMealTypes = selectedMealTypes,
        startDate = startDate,
        endDate = endDate,
        onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
        onNavigateBack = onNavigateBack,
        onNavigateToEditIntake = onNavigateToEditIntake,
        onFilterApply = { _, start, end -> viewModel.setDateRange(start, end) },
        onClearSearch = { viewModel.onSearchQueryChange("") }
    )
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
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    mealTypes.forEachIndexed { index, name ->
                        FilterChip(
                            selected = selectedMealTypes.contains(index),
                            onClick = { viewModel.toggleMealType(index) },
                            label = { Text(name) }
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
