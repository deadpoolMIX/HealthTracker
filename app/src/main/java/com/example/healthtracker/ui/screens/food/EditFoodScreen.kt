package com.example.healthtracker.ui.screens.food

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.healthtracker.util.FoodEmojiUtils
import kotlinx.coroutines.launch

/**
 * 编辑食物页面
 * 布局与添加自定义食物页面一致
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFoodScreen(
    foodId: Long,
    viewModel: EditFoodViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var foodName by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("🍽️") }
    var showEmojiPicker by remember { mutableStateOf(false) }

    // 单位相关
    var hasUnit by remember { mutableStateOf(false) }
    var unit by remember { mutableStateOf("") }
    var gramsPerUnit by remember { mutableStateOf("") }

    // 每百克营养数据
    var caloriesPer100g by remember { mutableStateOf("") }
    var carbsPer100g by remember { mutableStateOf("") }
    var proteinPer100g by remember { mutableStateOf("") }
    var fatPer100g by remember { mutableStateOf("") }

    val commonUnits = listOf("个", "杯", "瓶", "份", "块", "片", "勺", "包", "碗", "袋")
    var expandedUnit by remember { mutableStateOf(false) }

    // 加载食物数据
    LaunchedEffect(foodId) {
        viewModel.loadFood(foodId)
    }

    // 当食物数据加载完成后，更新表单
    LaunchedEffect(uiState.food) {
        uiState.food?.let { food ->
            foodName = food.name
            selectedEmoji = if (food.icon.isNotEmpty()) food.icon else FoodEmojiUtils.getDefaultEmojiForFood(food.name)
            caloriesPer100g = food.calories.toString()
            carbsPer100g = food.carbohydrates.toString()
            proteinPer100g = food.protein.toString()
            fatPer100g = food.fat.toString()
            // 单位数据
            hasUnit = !food.unit.isNullOrEmpty() && food.gramsPerUnit != null && food.gramsPerUnit > 0
            unit = food.unit ?: ""
            gramsPerUnit = food.gramsPerUnit?.toString() ?: ""
        }
    }

    // 计算每单位的营养值（用于预览）
    val caloriesValue = caloriesPer100g.toDoubleOrNull() ?: 0.0
    val carbsValue = carbsPer100g.toDoubleOrNull() ?: 0.0
    val proteinValue = proteinPer100g.toDoubleOrNull() ?: 0.0
    val fatValue = fatPer100g.toDoubleOrNull() ?: 0.0
    val gramsPerUnitValue = gramsPerUnit.toDoubleOrNull() ?: 0.0

    val unitCalories = if (hasUnit && gramsPerUnitValue > 0) caloriesValue * gramsPerUnitValue / 100 else 0.0
    val unitCarbs = if (hasUnit && gramsPerUnitValue > 0) carbsValue * gramsPerUnitValue / 100 else 0.0
    val unitProtein = if (hasUnit && gramsPerUnitValue > 0) proteinValue * gramsPerUnitValue / 100 else 0.0
    val unitFat = if (hasUnit && gramsPerUnitValue > 0) fatValue * gramsPerUnitValue / 100 else 0.0

    // 验证输入
    val isValid = foodName.isNotBlank() &&
            caloriesValue > 0 &&
            (!hasUnit || (unit.isNotBlank() && gramsPerUnitValue > 0))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑食物", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.food == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("食物不存在")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 食物名称
                OutlinedTextField(
                    value = foodName,
                    onValueChange = { foodName = it },
                    label = { Text("食物名称 *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // 图标选择
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "图标",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { showEmojiPicker = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectedEmoji,
                            fontSize = 24.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { showEmojiPicker = true }) {
                        Text("更换")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // 每百克营养数据
                Text(
                    text = "每百克营养数据 *",
                    style = MaterialTheme.typography.titleSmall
                )

                OutlinedTextField(
                    value = caloriesPer100g,
                    onValueChange = { caloriesPer100g = it },
                    label = { Text("热量 (kcal)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = carbsPer100g,
                        onValueChange = { carbsPer100g = it },
                        label = { Text("碳水 (g)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = proteinPer100g,
                        onValueChange = { proteinPer100g = it },
                        label = { Text("蛋白质 (g)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                OutlinedTextField(
                    value = fatPer100g,
                    onValueChange = { fatPer100g = it },
                    label = { Text("脂肪 (g)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // 单位设置（可选）
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = hasUnit,
                        onCheckedChange = { hasUnit = it }
                    )
                    Text(
                        text = "设置单位（可选）",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // 单位相关输入
                if (hasUnit) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = gramsPerUnit,
                            onValueChange = { gramsPerUnit = it },
                            label = { Text("数值 *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )

                        ExposedDropdownMenuBox(
                            expanded = expandedUnit,
                            onExpandedChange = { expandedUnit = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = unit,
                                onValueChange = { unit = it },
                                label = { Text("单位 *") },
                                modifier = Modifier.menuAnchor(),
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = expandedUnit,
                                onDismissRequest = { expandedUnit = false }
                            ) {
                                commonUnits.forEach { u ->
                                    DropdownMenuItem(
                                        text = { Text(u) },
                                        onClick = {
                                            unit = u
                                            expandedUnit = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 每单位营养预览
                    if (gramsPerUnitValue > 0 && caloriesValue > 0) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "每${unit.ifBlank { "单位" }}营养值预览",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("热量: ${String.format("%.1f", unitCalories)} kcal")
                                    Text("碳水: ${String.format("%.1f", unitCarbs)} g")
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("蛋白质: ${String.format("%.1f", unitProtein)} g")
                                    Text("脂肪: ${String.format("%.1f", unitFat)} g")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // 保存按钮
                Button(
                    onClick = {
                        scope.launch {
                            if (viewModel.updateFood(
                                    name = foodName,
                                    calories = caloriesValue,
                                    carbs = carbsValue,
                                    protein = proteinValue,
                                    fat = fatValue,
                                    category = uiState.food?.category ?: "其他",
                                    icon = selectedEmoji,
                                    unit = if (hasUnit) unit else null,
                                    gramsPerUnit = if (hasUnit) gramsPerUnitValue else null
                                )) {
                                onNavigateBack()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isValid
                ) {
                    Text("保存修改")
                }

                // 删除按钮（仅自定义食物可删除）
                if (uiState.food?.isCustom == true) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                viewModel.deleteFood()
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("删除此食物")
                    }
                }
            }
        }
    }

    // Emoji 选择对话框
    if (showEmojiPicker) {
        EmojiPickerDialog(
            selectedEmoji = selectedEmoji,
            onEmojiSelected = { emoji ->
                selectedEmoji = emoji
                showEmojiPicker = false
            },
            onDismiss = { showEmojiPicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmojiPickerDialog(
    selectedEmoji: String,
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择图标") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                FoodEmojiUtils.foodEmojisByCategory.forEach { (category, emojis) ->
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    // 使用普通 Row 代替 LazyVerticalGrid，使整个页面可以滚动
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        emojis.forEach { (emoji, _) ->
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (emoji == selectedEmoji)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { onEmojiSelected(emoji) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = emoji,
                                    fontSize = 24.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("完成")
            }
        }
    )
}