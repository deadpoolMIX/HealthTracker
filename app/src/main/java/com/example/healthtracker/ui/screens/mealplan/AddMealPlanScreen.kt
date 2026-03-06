package com.example.healthtracker.ui.screens.mealplan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.healthtracker.data.local.entity.MealPlanItemEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealPlanScreen(
    viewModel: AddMealPlanViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    // 当前添加食物的状态
    var showAddItemDialog by remember { mutableStateOf(false) }
    var editingItemIndex by remember { mutableStateOf<Int?>(null) }

    val planTypes = listOf("单餐", "单日（三餐）", "周计划（每天三餐）")
    val mealTypes = listOf("早餐", "午餐", "晚餐", "加餐")
    val weekDays = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加饮食计划", fontWeight = FontWeight.Medium) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 计划名称
            OutlinedTextField(
                value = uiState.planName,
                onValueChange = { viewModel.setPlanName(it) },
                label = { Text("计划名称 *") },
                placeholder = { Text("例如：减脂期一周食谱") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 计划类型
            Text(
                text = "计划类型",
                style = MaterialTheme.typography.titleSmall
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                planTypes.forEachIndexed { index, type ->
                    FilterChip(
                        selected = uiState.planType == index,
                        onClick = { viewModel.setPlanType(index) },
                        label = { Text(type) }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // 餐次选择（针对单日和周计划）
            if (uiState.planType >= 1) {
                Text(
                    text = "当前编辑餐次",
                    style = MaterialTheme.typography.titleSmall
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    mealTypes.forEachIndexed { index, type ->
                        FilterChip(
                            selected = uiState.currentMealType == index,
                            onClick = { viewModel.setCurrentMealType(index) },
                            label = { Text(type) }
                        )
                    }
                }
            }

            // 星期选择（针对周计划）
            if (uiState.planType == 2) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "当前编辑星期",
                    style = MaterialTheme.typography.titleSmall
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    weekDays.forEachIndexed { index, day ->
                        FilterChip(
                            selected = uiState.currentDayOfWeek == index,
                            onClick = { viewModel.setCurrentDayOfWeek(index) },
                            label = { Text(day) },
                            modifier = Modifier.sizeIn(minWidth = 40.dp)
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // 已添加的食物列表
            Text(
                text = "已添加食物 (${uiState.items.size})",
                style = MaterialTheme.typography.titleSmall
            )

            if (uiState.items.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text(
                            text = "点击下方按钮添加食物",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // 显示已添加的食物
                uiState.items.forEachIndexed { index, item ->
                    MealPlanItemCard(
                        item = item,
                        mealTypeName = viewModel.getMealTypeName(item.mealType),
                        dayName = item.dayOfWeek?.let { viewModel.getDayName(it) },
                        onEdit = {
                            editingItemIndex = index
                            showAddItemDialog = true
                        },
                        onDelete = { viewModel.removeItem(index) }
                    )
                }
            }

            // 添加食物按钮
            OutlinedButton(
                onClick = { showAddItemDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("添加食物")
            }

            Spacer(modifier = Modifier.weight(1f))

            // 保存按钮
            Button(
                onClick = {
                    scope.launch {
                        if (viewModel.savePlan()) {
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.planName.isNotBlank() && uiState.items.isNotEmpty()
            ) {
                Text("保存计划")
            }
        }
    }

    // 添加食物对话框
    if (showAddItemDialog) {
        AddMealPlanItemDialog(
            editingItem = editingItemIndex?.let { uiState.items[it] },
            currentMealType = uiState.currentMealType,
            currentDayOfWeek = uiState.currentDayOfWeek,
            onDismiss = {
                showAddItemDialog = false
                editingItemIndex = null
            },
            onConfirm = { item ->
                if (editingItemIndex != null) {
                    viewModel.updateItem(editingItemIndex!!, item)
                } else {
                    viewModel.addItem(item)
                }
                showAddItemDialog = false
                editingItemIndex = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealPlanItemCard(
    item: MealPlanItemEntity,
    mealTypeName: String,
    dayName: String?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.foodName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = buildString {
                        append(mealTypeName)
                        if (dayName != null) {
                            append(" · $dayName")
                        }
                        append(" · ${item.amount.toInt()}g")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "编辑")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "删除")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMealPlanItemDialog(
    editingItem: MealPlanItemEntity?,
    currentMealType: Int,
    currentDayOfWeek: Int?,
    onDismiss: () -> Unit,
    onConfirm: (MealPlanItemEntity) -> Unit
) {
    var foodName by remember { mutableStateOf(editingItem?.foodName ?: "") }
    var amount by remember { mutableStateOf(editingItem?.amount?.toString() ?: "") }
    var calories by remember { mutableStateOf(editingItem?.caloriesPer100g?.toString() ?: "") }
    var carbs by remember { mutableStateOf(editingItem?.carbsPer100g?.toString() ?: "") }
    var protein by remember { mutableStateOf(editingItem?.proteinPer100g?.toString() ?: "") }
    var fat by remember { mutableStateOf(editingItem?.fatPer100g?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editingItem != null) "编辑食物" else "添加食物") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = foodName,
                    onValueChange = { foodName = it },
                    label = { Text("食物名称 *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("份量 (克) *") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("热量 (kcal/100g)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it },
                        label = { Text("碳水 (g)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it },
                        label = { Text("蛋白质 (g)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = fat,
                    onValueChange = { fat = it },
                    label = { Text("脂肪 (g/100g)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    if (foodName.isNotBlank() && amountValue > 0) {
                        val item = MealPlanItemEntity(
                            id = editingItem?.id ?: 0,
                            planId = 0, // 将在保存时设置
                            foodName = foodName,
                            amount = amountValue,
                            mealType = editingItem?.mealType ?: currentMealType,
                            dayOfWeek = editingItem?.dayOfWeek ?: currentDayOfWeek,
                            caloriesPer100g = calories.toDoubleOrNull() ?: 0.0,
                            carbsPer100g = carbs.toDoubleOrNull() ?: 0.0,
                            proteinPer100g = protein.toDoubleOrNull() ?: 0.0,
                            fatPer100g = fat.toDoubleOrNull() ?: 0.0
                        )
                        onConfirm(item)
                    }
                },
                enabled = foodName.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}