package com.example.healthtracker.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.healthtracker.data.local.entity.CycleFoodEntity
import com.example.healthtracker.data.local.entity.IntakeRecordEntity
import com.example.healthtracker.ui.components.IntakeRecordItem
import com.example.healthtracker.ui.components.getMealTypeName
import com.example.healthtracker.util.DateTimeUtils
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToAddIntake: () -> Unit,
    onNavigateToAddBodyData: () -> Unit,
    onNavigateToAddSleep: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToUserProfile: () -> Unit,
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToEditIntake: (Long) -> Unit = {},
    onNavigateToAddCycleFood: () -> Unit = {},
    onNavigateToEditCycleFood: (Long) -> Unit = {},
    onNavigateToSearch: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var fabExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<IntakeRecordEntity?>(null) }
    var showContextMenu by remember { mutableStateOf<IntakeRecordEntity?>(null) }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showTargetCaloriesDialog by remember { mutableStateOf(false) }
    var showEditIntakeDialog by remember { mutableStateOf<IntakeRecordEntity?>(null) }
    var showCycleFoodMenu by remember { mutableStateOf<CycleFoodEntity?>(null) }
    var showDeleteCycleFoodDialog by remember { mutableStateOf<CycleFoodEntity?>(null) }

    DisposableEffect(Unit) {
        fabExpanded = false
        onDispose { }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (selectionMode) {
                        Text(text = "已选择 ${selectedIds.size} 项", fontWeight = FontWeight.Medium)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Row(
                                modifier = Modifier.clickable(onClick = onNavigateToCalendar),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = DateTimeUtils.formatDateShort(uiState.selectedDate),
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(Icons.Default.ExpandMore, contentDescription = "日历", modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { viewModel.goToPreviousDay() }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "前一天", modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { viewModel.goToToday() }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Circle, contentDescription = "今天", modifier = Modifier.size(12.dp))
                            }
                            IconButton(onClick = { viewModel.goToNextDay() }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "后一天", modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                },
                navigationIcon = {
                    if (selectionMode) {
                        IconButton(onClick = { selectionMode = false; selectedIds = emptySet() }) {
                            Icon(Icons.Default.Close, contentDescription = "取消")
                        }
                    } else {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Outlined.Settings, contentDescription = "设置")
                        }
                    }
                },
                actions = {
                    if (selectionMode) {
                        IconButton(onClick = {
                            selectedIds = if (selectedIds.size == uiState.todayIntake.size) emptySet() else uiState.todayIntake.map { it.id }.toSet()
                        }) {
                            Icon(if (selectedIds.size == uiState.todayIntake.size) Icons.Default.Deselect else Icons.Default.SelectAll, contentDescription = "全选")
                        }
                        IconButton(
                            onClick = {
                                if (selectedIds.isNotEmpty()) {
                                    viewModel.deleteRecordsByIds(selectedIds.toList())
                                    selectionMode = false; selectedIds = emptySet()
                                }
                            },
                            enabled = selectedIds.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "删除")
                        }
                    } else {
                        IconButton(onClick = onNavigateToSearch) {
                            Icon(Icons.Default.Search, contentDescription = "搜索")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!selectionMode) {
                MultiActionFab(
                    expanded = fabExpanded,
                    onExpandChange = { fabExpanded = it },
                    onIntakeClick = { fabExpanded = false; onNavigateToAddIntake() },
                    onBodyClick = { fabExpanded = false; onNavigateToAddBodyData() },
                    onSleepClick = { fabExpanded = false; onNavigateToAddSleep() },
                    onCycleFoodClick = { fabExpanded = false; onNavigateToAddCycleFood() }
                )
            }
        }
    ) { paddingValues ->
        val mealCardColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { CalorieArcCard(uiState.totalCalories, uiState.targetCalories, uiState.bmr, uiState.caloriePercentage, onLongClick = { showTargetCaloriesDialog = true }) }
            item { NutrientSummaryCard(uiState.totalCarbs, uiState.totalProtein, uiState.totalFat, uiState.targetCarbs, uiState.targetProtein, uiState.targetFat) }
            
            if (uiState.activeCycleFoods.isNotEmpty()) {
                item { Text("周期食物", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                items(uiState.activeCycleFoods) { cycleFood ->
                    Card(
                        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = { }, onLongClick = { showCycleFoodMenu = cycleFood }),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(cycleFood.icon, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(cycleFood.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("剩余: ${cycleFood.remainingCalories.roundToInt()} kcal", style = MaterialTheme.typography.bodySmall)
                                Text("约 ${cycleFood.getRemainingPortions()} 份", style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { viewModel.eatCycleFoodPortion(cycleFood) }, modifier = Modifier.weight(1f)) { Text("吃一份") }
                                Button(onClick = { viewModel.finishCycleFood(cycleFood) }, modifier = Modifier.weight(1f)) { Text("吃完剩余") }
                            }
                        }
                    }
                }
            }

            item { Text("今日摄入", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            if (uiState.todayIntake.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("暂无记录", textAlign = TextAlign.Center)
                        }
                    }
                }
            } else {
                listOf(0, 1, 2, 3).forEach { mealType ->
                    val records = uiState.todayIntake.filter { it.mealType == mealType }
                    if (records.isNotEmpty()) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = mealCardColor)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(getMealTypeName(mealType), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text("C:${records.sumOf { it.carbohydrates.roundToInt() }} P:${records.sumOf { it.protein.roundToInt() }} F:${records.sumOf { it.fat.roundToInt() }}", style = MaterialTheme.typography.bodySmall)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("${records.sumOf { it.calories.roundToInt() }} kcal", fontWeight = FontWeight.Bold)
                                    }
                                    records.forEach { record ->
                                        IntakeRecordItem(record, isSelected = selectedIds.contains(record.id), selectionMode = selectionMode, showMealType = false,
                                            onClick = { if (selectionMode) selectedIds = if (selectedIds.contains(record.id)) selectedIds - record.id else selectedIds + record.id else showEditIntakeDialog = record },
                                            onLongClick = { if (!selectionMode) showContextMenu = record })
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Text("今日身体数据", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            item { BodyDataCard(uiState.todayBodyRecord, onClick = onNavigateToAddBodyData) }
            item { Text("今日睡眠", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            item { SleepDataCard(uiState.todaySleepRecord, onClick = onNavigateToAddSleep) }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("删除记录") },
            text = { Text("确定要删除吗？") },
            confirmButton = { Button(onClick = { viewModel.deleteRecord(showDeleteDialog!!); showDeleteDialog = null }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("删除") } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = null }) { Text("取消") } }
        )
    }

    if (showContextMenu != null) {
        val record = showContextMenu!!
        AlertDialog(
            onDismissRequest = { showContextMenu = null },
            title = { Text(record.foodName) },
            text = {
                Column {
                    TextButton(onClick = { showContextMenu = null; onNavigateToEditIntake(record.id) }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Edit, null); Spacer(Modifier.width(8.dp)); Text("编辑") }
                    TextButton(onClick = { showContextMenu = null; showDeleteDialog = record }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Icon(Icons.Default.Delete, null); Spacer(Modifier.width(8.dp)); Text("删除") }
                    TextButton(onClick = { showContextMenu = null; selectionMode = true; selectedIds = setOf(record.id) }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Checklist, null); Spacer(Modifier.width(8.dp)); Text("批量选择") }
                }
            },
            confirmButton = { TextButton(onClick = { showContextMenu = null }) { Text("取消") } }
        )
    }

    if (showTargetCaloriesDialog) {
        TargetCaloriesDialog(
            currentTarget = uiState.targetCalories,
            nutrientMode = uiState.nutrientMode,
            carbsRatio = uiState.carbsRatio,
            proteinRatio = uiState.proteinRatio,
            fatRatio = uiState.fatRatio,
            targetCarbs = uiState.targetCarbs,
            targetProtein = uiState.targetProtein,
            targetFat = uiState.targetFat,
            onDismiss = { showTargetCaloriesDialog = false },
            onConfirm = { newTarget, nutrientMode, carbsRatio, proteinRatio, fatRatio, targetCarbs, targetProtein, targetFat ->
                viewModel.updateTargetCalories(newTarget)
                viewModel.updateNutrientSettings(nutrientMode, carbsRatio, proteinRatio, fatRatio, targetCarbs, targetProtein, targetFat)
                showTargetCaloriesDialog = false
            }
        )
    }

    if (showEditIntakeDialog != null) {
        EditIntakeDialog(
            record = showEditIntakeDialog!!,
            onDismiss = { showEditIntakeDialog = null },
            onConfirm = { updatedRecord -> viewModel.updateRecord(updatedRecord); showEditIntakeDialog = null }
        )
    }

    if (showCycleFoodMenu != null) {
        val cycleFood = showCycleFoodMenu!!
        AlertDialog(
            onDismissRequest = { showCycleFoodMenu = null },
            title = { Text(cycleFood.name) },
            text = {
                Column {
                    TextButton(onClick = { showCycleFoodMenu = null; onNavigateToEditCycleFood(cycleFood.id) }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Edit, null); Spacer(Modifier.width(8.dp)); Text("编辑") }
                    TextButton(onClick = { showCycleFoodMenu = null; showDeleteCycleFoodDialog = cycleFood }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Icon(Icons.Default.Delete, null); Spacer(Modifier.width(8.dp)); Text("删除") }
                }
            },
            confirmButton = { TextButton(onClick = { showCycleFoodMenu = null }) { Text("取消") } }
        )
    }

    if (showDeleteCycleFoodDialog != null) {
        val cycleFood = showDeleteCycleFoodDialog!!
        AlertDialog(
            onDismissRequest = { showDeleteCycleFoodDialog = null },
            title = { Text("删除周期食物") },
            text = { Text("确定要删除 \"${cycleFood.name}\" 吗？") },
            confirmButton = { Button(onClick = { viewModel.deleteCycleFood(cycleFood); showDeleteCycleFoodDialog = null }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("删除") } },
            dismissButton = { TextButton(onClick = { showDeleteCycleFoodDialog = null }) { Text("取消") } }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalorieArcCard(consumed: Double, target: Double, bmr: Double, percentage: Int, onLongClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().combinedClickable(onClick = {}, onLongClick = onLongClick), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("今日可摄入热量", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.size(180.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(progress = { 1f }, modifier = Modifier.size(180.dp), color = MaterialTheme.colorScheme.surfaceVariant, strokeWidth = 16.dp, strokeCap = StrokeCap.Round)
                CircularProgressIndicator(progress = { (percentage / 100f).coerceIn(0f, 1f) }, modifier = Modifier.size(180.dp), color = if (percentage > 100) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, strokeWidth = 16.dp, strokeCap = StrokeCap.Round)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${consumed.toInt()}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                    Text("/ ${target.toInt()} kcal", style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (bmr > 0 && bmr < target) { Text("基础代谢: ${bmr.toInt()} kcal", style = MaterialTheme.typography.bodySmall) }
            val remaining = target - consumed
            Text(if (remaining >= 0) "还可摄入 ${remaining.toInt()} kcal" else "已超出 ${(-remaining).toInt()} kcal", color = if (remaining >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun NutrientSummaryCard(carbs: Double, protein: Double, fat: Double, targetCarbs: Double, targetProtein: Double, targetFat: Double) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            NutrientProgressItem("碳水", carbs, targetCarbs, MaterialTheme.colorScheme.primary)
            NutrientProgressItem("蛋白质", protein, targetProtein, MaterialTheme.colorScheme.secondary)
            NutrientProgressItem("脂肪", fat, targetFat, MaterialTheme.colorScheme.tertiary)
        }
    }
}

@Composable
fun NutrientProgressItem(name: String, value: Double, target: Double, color: Color) {
    val progress = if (target > 0) (value / target).coerceIn(0.0, 1.0).toFloat() else 0f
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name, style = MaterialTheme.typography.bodyMedium)
            Text("${value.roundToInt()}/${target.roundToInt()}g", color = if (value > target) MaterialTheme.colorScheme.error else color)
        }
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = if (value > target) MaterialTheme.colorScheme.error else color, strokeCap = StrokeCap.Round)
    }
}

@Composable
fun BodyDataCard(record: com.example.healthtracker.data.local.entity.BodyRecordEntity?, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))) {
        if (record == null) { Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { Text("点击添加身体数据") } }
        else {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                DataItem("体重", "${record.weight}kg"); DataItem("体脂", "${record.bodyFatRate}%"); DataItem("肌肉", "${record.muscleMass}kg")
            }
        }
    }
}

@Composable
fun SleepDataCard(record: com.example.healthtracker.data.local.entity.SleepRecordEntity?, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))) {
        if (record == null) { Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { Text("点击添加睡眠记录") } }
        else {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                DataItem("入睡", DateTimeUtils.formatTime(record.sleepTime)); DataItem("起床", DateTimeUtils.formatTime(record.wakeTime)); DataItem("时长", DateTimeUtils.formatDuration(record.duration))
            }
        }
    }
}

@Composable
fun DataItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold); Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun MultiActionFab(expanded: Boolean, onExpandChange: (Boolean) -> Unit, onIntakeClick: () -> Unit, onBodyClick: () -> Unit, onSleepClick: () -> Unit, onCycleFoodClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AnimatedVisibility(visible = expanded, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FabOption(Icons.Outlined.Restaurant, "摄入", onIntakeClick)
                FabOption(Icons.Outlined.MonitorWeight, "身体", onBodyClick)
                FabOption(Icons.Outlined.Bedtime, "睡眠", onSleepClick)
                FabOption(Icons.Outlined.Schedule, "周期", onCycleFoodClick)
            }
        }
        FloatingActionButton(onClick = { onExpandChange(!expanded) }) { Icon(if (expanded) Icons.Default.Close else Icons.Default.Add, null) }
    }
}

@Composable
fun FabOption(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(4.dp)) { Text(label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium) }
        FloatingActionButton(onClick = onClick, modifier = Modifier.size(48.dp)) { Icon(icon, label, modifier = Modifier.size(24.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditIntakeDialog(record: IntakeRecordEntity, onDismiss: () -> Unit, onConfirm: (IntakeRecordEntity) -> Unit) {
    var selectedMealType by remember { mutableIntStateOf(record.mealType) }
    var amountText by remember { mutableStateOf(record.amount.toInt().toString()) }
    var selectedUnit by remember { mutableStateOf(record.unit?.replace(Regex("^[0-9.]*"), "") ?: "克") }
    var expandedUnit by remember { mutableStateOf(false) }
    val mealTypes = listOf("早餐", "午餐", "晚餐", "加餐")
    val units = listOf("克", "毫升", "个", "杯", "勺", "份", "块", "片", "包", "碗")
    val amount = amountText.toDoubleOrNull() ?: 0.0
    val ratio = if (record.amount > 0) amount / record.amount else 1.0
    val calories = record.calories * ratio
    val carbs = record.carbohydrates * ratio
    val protein = record.protein * ratio
    val fat = record.fat * ratio

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(record.foodName) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    mealTypes.forEachIndexed { index, type ->
                        FilterChip(selected = selectedMealType == index, onClick = { selectedMealType = index }, label = { Text(type) }, modifier = Modifier.weight(1f))
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = amountText, onValueChange = { amountText = it.filter { c -> c.isDigit() } }, label = { Text("数值") }, modifier = Modifier.weight(1f))
                    ExposedDropdownMenuBox(expanded = expandedUnit, onExpandedChange = { expandedUnit = it }, modifier = Modifier.weight(1f)) {
                        OutlinedTextField(value = selectedUnit, onValueChange = {}, label = { Text("单位") }, modifier = Modifier.menuAnchor(), readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedUnit) })
                        ExposedDropdownMenu(expanded = expandedUnit, onDismissRequest = { expandedUnit = false }) {
                            units.forEach { unit -> DropdownMenuItem(text = { Text(unit) }, onClick = { selectedUnit = unit; expandedUnit = false }) }
                        }
                    }
                }
                if (amount > 0) {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))) {
                        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            DataItem("热量", "${calories.roundToInt()}"); DataItem("碳水", "${carbs.roundToInt()}g"); DataItem("蛋白", "${protein.roundToInt()}g"); DataItem("脂肪", "${fat.roundToInt()}g")
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onConfirm(record.copy(mealType = selectedMealType, amount = amount, calories = calories, carbohydrates = carbs, protein = protein, fat = fat, unit = selectedUnit)) }, enabled = amount > 0) { Text("保存") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TargetCaloriesDialog(currentTarget: Double, nutrientMode: Int, carbsRatio: Double, proteinRatio: Double, fatRatio: Double, targetCarbs: Double, targetProtein: Double, targetFat: Double, onDismiss: () -> Unit, onConfirm: (Double, Int, Double, Double, Double, Double?, Double?, Double?) -> Unit) {
    var targetValue by remember { mutableStateOf(currentTarget.toInt().toString()) }
    var currentNutrientMode by remember { mutableIntStateOf(nutrientMode) }
    var currentCarbsRatio by remember { mutableFloatStateOf(carbsRatio.toFloat()) }
    var currentProteinRatio by remember { mutableFloatStateOf(proteinRatio.toFloat()) }
    var currentFatRatio by remember { mutableFloatStateOf(fatRatio.toFloat()) }
    var cCarbs by remember { mutableStateOf(if (targetCarbs > 0) targetCarbs.toInt().toString() else "") }
    var cProtein by remember { mutableStateOf(if (targetProtein > 0) targetProtein.toInt().toString() else "") }
    var cFat by remember { mutableStateOf(if (targetFat > 0) targetFat.toInt().toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置每日目标") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = targetValue, onValueChange = { targetValue = it.filter { c -> c.isDigit() } }, label = { Text("目标热量") }, suffix = { Text("kcal") }, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    FilterChip(selected = currentNutrientMode == 0, onClick = { currentNutrientMode = 0 }, label = { Text("自动") })
                    FilterChip(selected = currentNutrientMode == 1, onClick = { currentNutrientMode = 1 }, label = { Text("手动") })
                }
                if (currentNutrientMode == 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("碳水 ${currentCarbsRatio.toInt()}%"); Slider(value = currentCarbsRatio, onValueChange = { currentCarbsRatio = it }, valueRange = 0f..100f)
                        Text("蛋白 ${currentProteinRatio.toInt()}%"); Slider(value = currentProteinRatio, onValueChange = { currentProteinRatio = it }, valueRange = 0f..100f)
                        Text("脂肪 ${currentFatRatio.toInt()}%"); Slider(value = currentFatRatio, onValueChange = { currentFatRatio = it }, valueRange = 0f..100f)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = cCarbs, onValueChange = { cCarbs = it.filter { c -> c.isDigit() } }, label = { Text("目标碳水(g)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = cProtein, onValueChange = { cProtein = it.filter { c -> c.isDigit() } }, label = { Text("目标蛋白(g)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = cFat, onValueChange = { cFat = it.filter { c -> c.isDigit() } }, label = { Text("目标脂肪(g)") }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onConfirm(targetValue.toDoubleOrNull() ?: currentTarget, currentNutrientMode, currentCarbsRatio.toDouble(), currentProteinRatio.toDouble(), currentFatRatio.toDouble(), cCarbs.toDoubleOrNull(), cProtein.toDoubleOrNull(), cFat.toDoubleOrNull()) }) { Text("确定") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

private fun getFoodEmoji(record: IntakeRecordEntity): String {
    return record.foodIcon ?: "🍴"
}
