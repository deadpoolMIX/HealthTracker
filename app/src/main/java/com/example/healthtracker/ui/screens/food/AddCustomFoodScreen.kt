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

/**
 * 添加自定义食物页面
 * 专门用于添加自定义食物到食物库
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomFoodScreen(
    viewModel: AddCustomFoodViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val isSaving by viewModel.isSaving.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    // 保存成功后返回
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            onNavigateBack()
        }
    }

    var foodName by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("🍽️") }
    var showEmojiPicker by remember { mutableStateOf(false) }

    // 单位相关
    var hasUnit by remember { mutableStateOf(false) }
    var unit by remember { mutableStateOf("") }
    var gramsPerUnit by remember { mutableStateOf("") }

    // 每n克营养数据
    var perAmount by remember { mutableStateOf("100") }
    var perUnit by remember { mutableStateOf("克") }
    var expandedPerUnit by remember { mutableStateOf(false) }

    // 每n克营养数据
    var caloriesPerN by remember { mutableStateOf("") }
    var carbsPerN by remember { mutableStateOf("") }
    var proteinPerN by remember { mutableStateOf("") }
    var fatPerN by remember { mutableStateOf("") }

    val commonUnits = listOf("个", "杯", "瓶", "份", "块", "片", "勺", "包", "碗", "袋")
    var expandedUnit by remember { mutableStateOf(false) }

    // 计算每百克的营养值（用于内部存储）
    val perAmountValue = perAmount.toDoubleOrNull() ?: 100.0
    val caloriesPerNValue = caloriesPerN.toDoubleOrNull() ?: 0.0
    val carbsPerNValue = carbsPerN.toDoubleOrNull() ?: 0.0
    val proteinPerNValue = proteinPerN.toDoubleOrNull() ?: 0.0
    val fatPerNValue = fatPerN.toDoubleOrNull() ?: 0.0

    // 计算每百克营养值
    val caloriesValue = if (perAmountValue > 0) caloriesPerNValue * 100.0 / perAmountValue else 0.0
    val carbsValue = if (perAmountValue > 0) carbsPerNValue * 100.0 / perAmountValue else 0.0
    val proteinValue = if (perAmountValue > 0) proteinPerNValue * 100.0 / perAmountValue else 0.0
    val fatValue = if (perAmountValue > 0) fatPerNValue * 100.0 / perAmountValue else 0.0
    val gramsPerUnitValue = gramsPerUnit.toDoubleOrNull() ?: 0.0

    // 验证输入
    val isValid = foodName.isNotBlank() &&
            caloriesPerNValue > 0 &&
            perAmountValue > 0 &&
            (!hasUnit || (unit.isNotBlank() && gramsPerUnitValue > 0))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加自定义食物", fontWeight = FontWeight.Medium) },
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
            // 食物名称
            OutlinedTextField(
                value = foodName,
                onValueChange = {
                    foodName = it
                    // 自动更新 emoji
                    if (selectedEmoji == "🍽️" || selectedEmoji.isEmpty()) {
                        selectedEmoji = FoodEmojiUtils.getDefaultEmojiForFood(it)
                    }
                },
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

            // 每n克营养数据
            Text(
                text = "营养数据 *",
                style = MaterialTheme.typography.titleSmall
            )

            // 每 n 克/毫升 输入行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("每", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = perAmount,
                    onValueChange = { perAmount = it.filter { c -> c.isDigit() } },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                ExposedDropdownMenuBox(
                    expanded = expandedPerUnit,
                    onExpandedChange = { expandedPerUnit = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = perUnit,
                        onValueChange = {},
                        modifier = Modifier.menuAnchor(),
                        singleLine = true,
                        readOnly = true
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPerUnit,
                        onDismissRequest = { expandedPerUnit = false }
                    ) {
                        listOf("克", "毫升").forEach { u ->
                            DropdownMenuItem(
                                text = { Text(u) },
                                onClick = {
                                    perUnit = u
                                    expandedPerUnit = false
                                }
                            )
                        }
                    }
                }
                Text("含", style = MaterialTheme.typography.bodyMedium)
            }

            OutlinedTextField(
                value = caloriesPerN,
                onValueChange = { caloriesPerN = it },
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
                    value = carbsPerN,
                    onValueChange = { carbsPerN = it },
                    label = { Text("碳水 (g)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = proteinPerN,
                    onValueChange = { proteinPerN = it },
                    label = { Text("蛋白质 (g)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            OutlinedTextField(
                value = fatPerN,
                onValueChange = { fatPerN = it },
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
                        label = { Text("克/毫升 *") },
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
                    // 计算每单位的营养值
                    val unitCalories = caloriesValue * gramsPerUnitValue / 100
                    val unitCarbs = carbsValue * gramsPerUnitValue / 100
                    val unitProtein = proteinValue * gramsPerUnitValue / 100
                    val unitFat = fatValue * gramsPerUnitValue / 100

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
                    viewModel.saveCustomFood(
                        name = foodName,
                        icon = selectedEmoji,
                        calories = caloriesValue,
                        carbs = carbsValue,
                        protein = proteinValue,
                        fat = fatValue,
                        unit = if (hasUnit) unit else null,
                        gramsPerUnit = if (hasUnit) gramsPerUnitValue else null
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isValid && !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("保存中...")
                } else {
                    Text("保存到食物库")
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
                    // 使用 FlowRow 代替 LazyVerticalGrid，使整个页面可以滚动
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