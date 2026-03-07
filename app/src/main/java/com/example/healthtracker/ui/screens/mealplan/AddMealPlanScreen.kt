package com.example.healthtracker.ui.screens.mealplan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.healthtracker.data.local.entity.FoodEntity
import com.example.healthtracker.data.local.entity.MealPlanItemEntity
import com.example.healthtracker.util.FoodEmojiUtils
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch

/**
 * 添加饮食计划页面
 * 布局与"添加摄入"页面一致
 */
@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun AddMealPlanScreen(
    viewModel: AddMealPlanViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToCustomFood: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf<FoodEntity?>(null) }

    val planTypes = listOf("单餐", "单日（三餐）", "周计划（每天三餐）")
    val mealTypes = listOf("早餐", "午餐", "晚餐", "加餐")
    val weekDays = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

    // 搜索防抖
    LaunchedEffect(searchQuery) {
        viewModel.searchFoods(searchQuery)
    }

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
        },
        bottomBar = {
            if (uiState.items.isNotEmpty()) {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                if (viewModel.savePlan()) {
                                    onNavigateBack()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        enabled = uiState.planName.isNotBlank()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("保存计划 (${uiState.items.size})")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                planTypes.forEachIndexed { index, type ->
                    FilterChip(
                        selected = uiState.planType == index,
                        onClick = { viewModel.setPlanType(index) },
                        label = { Text(type, maxLines = 1) }
                    )
                }
            }

            // 餐次选择（针对单日和周计划）
            if (uiState.planType >= 1) {
                Text(
                    text = "选择餐次",
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
                Text(
                    text = "选择星期",
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

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // 已添加的食物列表（与 AddIntakeScreen 一致）
            if (uiState.items.isNotEmpty()) {
                Text(
                    text = "已添加的食物",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        uiState.items.forEachIndexed { index, item ->
                            MealPlanPendingItem(
                                item = item,
                                onRemove = { viewModel.removeItem(index) }
                            )
                            if (index < uiState.items.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }

            // 搜索食物（与 AddIntakeScreen 一致）
            Text(
                text = "搜索食物库",
                style = MaterialTheme.typography.titleSmall
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("搜索食物名称") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "搜索")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "清除")
                        }
                    }
                }
            )

            // 搜索结果列表 - 限制高度为4行食物
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(searchResults.take(30)) { food ->
                        FoodSearchResultItem(
                            food = food,
                            onClick = { showAddDialog = food }
                        )
                    }
                }
            }

            // 添加自定义食物按钮 - 在框外面
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onNavigateToCustomFood,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("添加自定义食物")
            }
        }
    }

    // 添加食物对话框
    showAddDialog?.let { food ->
        AddMealPlanFoodDialog(
            food = food,
            onDismiss = { showAddDialog = null },
            onConfirm = { amount, unit ->
                val item = MealPlanItemEntity(
                    id = 0,
                    planId = 0,
                    foodName = food.name,
                    amount = amount,
                    mealType = uiState.currentMealType,
                    dayOfWeek = if (uiState.planType == 2) uiState.currentDayOfWeek else null,
                    caloriesPer100g = food.calories,
                    carbsPer100g = food.carbohydrates,
                    proteinPer100g = food.protein,
                    fatPer100g = food.fat
                )
                viewModel.addItem(item)
                showAddDialog = null
            }
        )
    }
}

/**
 * 已添加的食物项（与 AddIntakeScreen 一致）
 */
@Composable
private fun MealPlanPendingItem(
    item: MealPlanItemEntity,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 食物图标
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = FoodEmojiUtils.getDefaultEmojiForFood(item.foodName),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // 食物信息
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.foodName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${item.amount.toInt()}g · ${item.caloriesPer100g.toInt()} kcal/100g",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 删除按钮
        IconButton(onClick = onRemove) {
            Icon(
                Icons.Default.Close,
                contentDescription = "移除",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * 食物搜索结果项（与 AddIntakeScreen 一致）
 */
@Composable
private fun FoodSearchResultItem(
    food: FoodEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 食物图标
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getFoodEmoji(food),
                    style = MaterialTheme.typography.titleSmall
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 食物信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${food.calories.toInt()} kcal/100g",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 添加图标
            Icon(
                imageVector = Icons.Default.AddCircleOutline,
                contentDescription = "添加",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * 添加食物对话框（与 AddIntakeScreen 一致）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMealPlanFoodDialog(
    food: FoodEntity,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, unit: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var selectedUnitIndex by remember { mutableIntStateOf(0) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var expandedUnit by remember { mutableStateOf(false) }

    // 单位选项
    val baseUnits = listOf("克", "毫升")
    val customUnits = listOf("个", "杯", "勺", "份", "块", "片", "包", "碗", "袋")

    val units = if (food.unit != null && food.gramsPerUnit != null && food.gramsPerUnit > 0) {
        listOf(food.unit!!) + baseUnits + customUnits.filter { it != food.unit }
    } else {
        baseUnits + customUnits
    }

    val unitGramsMap = remember(food) {
        mutableMapOf<String, Double>().apply {
            put("克", 1.0)
            put("毫升", 1.0)
            put("个", food.gramsPerUnit ?: 100.0)
            put("杯", 200.0)
            put("勺", 15.0)
            put("份", food.gramsPerUnit ?: 100.0)
            put("块", 50.0)
            put("片", 20.0)
            put("包", 100.0)
            put("碗", 200.0)
            put("袋", 100.0)
            if (food.unit != null && food.gramsPerUnit != null) {
                put(food.unit!!, food.gramsPerUnit)
            }
        }
    }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val currentUnit = units.getOrElse(selectedUnitIndex) { "克" }
    val gramsPerUnit = unitGramsMap[currentUnit] ?: 1.0
    val grams = amount * gramsPerUnit

    val calories = (grams / 100.0) * food.calories
    val carbs = (grams / 100.0) * food.carbohydrates
    val protein = (grams / 100.0) * food.protein
    val fat = (grams / 100.0) * food.fat

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 标题
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = getFoodEmoji(food), style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = food.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${food.calories.toInt()} kcal/100g",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 数值和单位输入行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = {
                            if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                amountText = it
                                amountError = null
                            }
                        },
                        label = { Text("数值") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        isError = amountError != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        )
                    )

                    ExposedDropdownMenuBox(
                        expanded = expandedUnit,
                        onExpandedChange = { expandedUnit = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = currentUnit,
                            onValueChange = {},
                            label = { Text("单位") },
                            modifier = Modifier.menuAnchor(),
                            singleLine = true,
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnit)
                            }
                        )
                        ExposedDropdownMenu(
                            expanded = expandedUnit,
                            onDismissRequest = { expandedUnit = false }
                        ) {
                            units.forEachIndexed { index, unit ->
                                DropdownMenuItem(
                                    text = { Text(unit) },
                                    onClick = {
                                        selectedUnitIndex = index
                                        expandedUnit = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 营养预览
                if (amount > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "营养预览 (${grams.toInt()}克)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                NutrientPreviewItem("热量", "${calories.toInt()} kcal")
                                NutrientPreviewItem("碳水", "${carbs.toInt()}g")
                                NutrientPreviewItem("蛋白质", "${protein.toInt()}g")
                                NutrientPreviewItem("脂肪", "${fat.toInt()}g")
                            }
                        }
                    }
                }

                // 按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("取消")
                    }
                    Button(
                        onClick = {
                            if (amount <= 0) {
                                amountError = "请输入有效数值"
                            } else {
                                onConfirm(grams, "$amount${currentUnit}")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("确认")
                    }
                }
            }
        }
    }
}

@Composable
private fun NutrientPreviewItem(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun getFoodEmoji(food: FoodEntity): String {
    if (food.icon.isNotEmpty() && food.icon != "custom" && isEmoji(food.icon)) {
        return food.icon
    }
    return FoodEmojiUtils.getDefaultEmojiForFood(food.name)
}

private fun isEmoji(text: String): Boolean {
    if (text.isEmpty()) return false
    val firstChar = text[0]
    return firstChar.code in 0x1F300..0x1F9FF ||
            firstChar.code in 0x2600..0x26FF ||
            firstChar.code in 0x2700..0x27BF
}