package com.example.healthtracker.ui.screens.intake

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.healthtracker.data.local.entity.FoodEntity
import com.example.healthtracker.util.FoodEmojiUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * 添加摄入页面 - 功能 #24
 * 1. 选择餐次、日期
 * 2. 搜索食物库中的食物
 * 3. 点击食物弹出对话框输入份量
 * 4. 添加多个食物到临时列表
 * 5. 底部保存按钮批量保存
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIntakeScreen(
    viewModel: AddIntakeViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToCustomFood: (Long?) -> Unit = {}
) {
    val searchResults by viewModel.searchResults.collectAsState()
    val pendingItems by viewModel.pendingItems.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val saveCompleted by viewModel.saveCompleted.collectAsState(initial = false)

    var searchQuery by remember { mutableStateOf("") }
    var selectedMealType by remember { mutableIntStateOf(0) }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf<FoodEntity?>(null) }

    val mealTypes = listOf("早餐", "午餐", "晚餐", "加餐")
    val dateFormat = remember { SimpleDateFormat("MM月dd日 E", Locale.CHINA) }

    // 搜索防抖
    LaunchedEffect(searchQuery) {
        viewModel.searchFoods(searchQuery)
    }

    // 保存成功后返回
    LaunchedEffect(saveCompleted) {
        if (saveCompleted) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加摄入", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        bottomBar = {
            if (pendingItems.isNotEmpty()) {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Button(
                        onClick = { viewModel.saveAllRecords(selectedDate, selectedMealType) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("保存记录 (${pendingItems.size})")
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
            // 日期和餐次选择
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 日期选择
                OutlinedCard(
                    modifier = Modifier.weight(1f),
                    onClick = { showDatePicker = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = dateFormat.format(Date(selectedDate)),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // 餐次选择
                OutlinedCard(
                    modifier = Modifier.weight(1f),
                    onClick = { /* 显示餐次选择 */ }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = mealTypes[selectedMealType],
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // 餐次快速切换
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                mealTypes.forEachIndexed { index, type ->
                    FilterChip(
                        selected = selectedMealType == index,
                        onClick = { selectedMealType = index },
                        label = { Text(type) }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // 已添加的食物列表
            if (pendingItems.isNotEmpty()) {
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
                        pendingItems.forEachIndexed { index, item ->
                            PendingFoodItem(
                                item = item,
                                onRemove = { viewModel.removePendingItem(index) }
                            )
                            if (index < pendingItems.lastIndex) {
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

            // 搜索食物
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
                    .height(280.dp), // 约4行食物的高度
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

                    // 底部添加自定义食物按钮
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = { onNavigateToCustomFood(null) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("添加自定义食物")
                        }
                    }
                }
            }
        }
    }

    // 日期选择对话框
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDate = it
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // 添加食物对话框
    showAddDialog?.let { food ->
        AddFoodDialog(
            food = food,
            onDismiss = { showAddDialog = null },
            onConfirm = { amount, unit ->
                viewModel.addPendingItem(food, amount, unit)
                showAddDialog = null
            }
        )
    }
}

/**
 * 已添加的食物项
 */
@Composable
private fun PendingFoodItem(
    item: PendingFoodItem,
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
                text = getFoodEmoji(item.food),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // 食物信息
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.food.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${item.amount.toInt()}${item.unit} · ${item.calories.toInt()} kcal",
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
 * 食物搜索结果项
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
 * 添加食物对话框
 * 左边为输入框（输入数值），右边为下拉选择框（选择单位）
 * 使用食物库数据计算营养值
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFoodDialog(
    food: FoodEntity,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, unit: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var selectedUnitIndex by remember { mutableIntStateOf(0) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var expandedUnit by remember { mutableStateOf(false) }

    // 单位选项 - 根据食物库数据动态生成
    val baseUnits = listOf("克", "毫升")
    val customUnits = listOf("个", "杯", "勺", "份", "块", "片", "包", "碗", "袋")

    // 如果食物有自定义单位，优先显示
    val units = if (food.unit != null && food.gramsPerUnit != null && food.gramsPerUnit > 0) {
        listOf(food.unit!!) + baseUnits + customUnits.filter { it != food.unit }
    } else {
        baseUnits + customUnits
    }

    // 每单位对应的克数
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
            // 如果食物有自定义单位，使用食物库中的值
            if (food.unit != null && food.gramsPerUnit != null) {
                put(food.unit!!, food.gramsPerUnit)
            }
        }
    }

    // 计算预览数据 - 使用食物库的营养数据
    val amount = amountText.toDoubleOrNull() ?: 0.0
    val currentUnit = units.getOrElse(selectedUnitIndex) { "克" }
    val gramsPerUnit = unitGramsMap[currentUnit] ?: 1.0
    val grams = amount * gramsPerUnit

    // 使用食物库中每百克的营养数据计算
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

                // 数值和单位输入行 - 左边输入框，右边选择框
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 左边：数值输入框
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

                    // 右边：单位下拉选择框
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
                                // 返回实际克数
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
    // 如果食物有自定义图标且是emoji格式，使用自定义图标
    if (food.icon.isNotEmpty() && food.icon != "custom" && isEmoji(food.icon)) {
        return food.icon
    }
    // 否则根据名称推断
    return FoodEmojiUtils.getDefaultEmojiForFood(food.name)
}

/**
 * 检查字符串是否为emoji
 */
private fun isEmoji(text: String): Boolean {
    if (text.isEmpty()) return false
    val firstChar = text[0]
    // Emoji 的 Unicode 范围检查
    return firstChar.code in 0x1F300..0x1F9FF ||
            firstChar.code in 0x2600..0x26FF ||
            firstChar.code in 0x2700..0x27BF
}