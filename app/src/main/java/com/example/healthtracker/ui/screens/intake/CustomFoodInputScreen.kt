package com.example.healthtracker.ui.screens.intake

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * 详细录入页面
 * 从搜索页点击食物后进入，或从食物库添加自定义食物进入
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomFoodInputScreen(
    viewModel: AddIntakeViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    // 预填充的食物数据（从食物库选择时传入）
    initialFoodName: String = "",
    initialCalories: Double = 0.0,
    initialCarbs: Double = 0.0,
    initialProtein: Double = 0.0,
    initialFat: Double = 0.0,
    initialMealType: Int = 0,
    // 是否是从食物库添加自定义食物入口进入
    isFromFoodLibrary: Boolean = false
) {
    val isSaving by viewModel.isSaving.collectAsState()

    var foodName by remember { mutableStateOf(initialFoodName) }
    var amount by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var amountInUnit by remember { mutableStateOf("") }
    var gramsPerUnit by remember { mutableStateOf("") }
    var caloriesPer100g by remember { mutableStateOf(if (initialCalories > 0) initialCalories.toString() else "") }
    var carbsPer100g by remember { mutableStateOf(if (initialCarbs > 0) initialCarbs.toString() else "") }
    var proteinPer100g by remember { mutableStateOf(if (initialProtein > 0) initialProtein.toString() else "") }
    var fatPer100g by remember { mutableStateOf(if (initialFat > 0) initialFat.toString() else "") }
    var note by remember { mutableStateOf("") }
    var selectedMealType by remember { mutableIntStateOf(initialMealType) }
    var saveAsCustomFood by remember { mutableStateOf(isFromFoodLibrary) }

    val mealTypes = listOf("早餐", "午餐", "晚餐", "加餐")
    val commonUnits = listOf("克", "毫升", "个", "杯", "瓶", "份", "块", "片", "勺", "包")
    var expandedUnit by remember { mutableStateOf(false) }

    // 是否有预填充数据（从食物库选择的）
    val hasPrefilledData = initialFoodName.isNotEmpty()

    // 计算实际营养值
    val amountValue = amount.toDoubleOrNull() ?: 0.0
    val caloriesPer100gValue = caloriesPer100g.toDoubleOrNull() ?: 0.0
    val carbsPer100gValue = carbsPer100g.toDoubleOrNull() ?: 0.0
    val proteinPer100gValue = proteinPer100g.toDoubleOrNull() ?: 0.0
    val fatPer100gValue = fatPer100g.toDoubleOrNull() ?: 0.0

    val actualCalories = (amountValue / 100.0) * caloriesPer100gValue
    val actualCarbs = (amountValue / 100.0) * carbsPer100gValue
    val actualProtein = (amountValue / 100.0) * proteinPer100gValue
    val actualFat = (amountValue / 100.0) * fatPer100gValue

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isFromFoodLibrary) "添加自定义食物"
                        else if (hasPrefilledData) "记录摄入"
                        else "添加摄入记录",
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 餐次选择
            Text(
                text = "餐次",
                style = MaterialTheme.typography.titleSmall
            )
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

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // 食物名称
            OutlinedTextField(
                value = foodName,
                onValueChange = { foodName = it },
                label = { Text("食物名称 *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !hasPrefilledData
            )

            // 重量和单位
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("重量 (克/毫升) *") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                ExposedDropdownMenuBox(
                    expanded = expandedUnit,
                    onExpandedChange = { expandedUnit = it },
                    modifier = Modifier.weight(0.5f)
                ) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("单位") },
                        modifier = Modifier.menuAnchor(),
                        singleLine = true,
                        readOnly = false
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

            // 单位换算（可选）
            if (unit.isNotEmpty() && unit !in listOf("克", "毫升")) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = amountInUnit,
                        onValueChange = { amountInUnit = it },
                        label = { Text("数量 ($unit)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = gramsPerUnit,
                        onValueChange = { gramsPerUnit = it },
                        label = { Text("每${unit}克数") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // 每百克营养数据
            Text(
                text = "每百克营养数据",
                style = MaterialTheme.typography.titleSmall
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = caloriesPer100g,
                    onValueChange = { caloriesPer100g = it },
                    label = { Text("热量 (kcal) *") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = carbsPer100g,
                    onValueChange = { carbsPer100g = it },
                    label = { Text("碳水 (g)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = proteinPer100g,
                    onValueChange = { proteinPer100g = it },
                    label = { Text("蛋白质 (g)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = fatPer100g,
                    onValueChange = { fatPer100g = it },
                    label = { Text("脂肪 (g)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            // 计算结果预览
            if (amountValue > 0 && caloriesPer100gValue > 0) {
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
                            text = "计算结果",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("热量: ${String.format("%.1f", actualCalories)} kcal")
                            Text("碳水: ${String.format("%.1f", actualCarbs)} g")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("蛋白质: ${String.format("%.1f", actualProtein)} g")
                            Text("脂肪: ${String.format("%.1f", actualFat)} g")
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // 备注
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("备注（可选）") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            // 保存为自定义食物选项
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = saveAsCustomFood,
                    onCheckedChange = { saveAsCustomFood = it }
                )
                Text(
                    text = "同时保存到食物库",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 保存按钮
            Button(
                onClick = {
                    if (foodName.isNotBlank() && amountValue > 0 && caloriesPer100gValue > 0) {
                        viewModel.saveRecord(
                            foodName = foodName,
                            amount = amountValue,
                            caloriesPer100g = caloriesPer100gValue,
                            carbsPer100g = carbsPer100gValue,
                            proteinPer100g = proteinPer100gValue,
                            fatPer100g = fatPer100gValue,
                            mealType = selectedMealType,
                            unit = unit.takeIf { it.isNotEmpty() },
                            amountInUnit = amountInUnit.toDoubleOrNull(),
                            gramsPerUnit = gramsPerUnit.toDoubleOrNull(),
                            note = note.takeIf { it.isNotBlank() },
                            saveAsCustomFood = saveAsCustomFood
                        )
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = foodName.isNotBlank() && amountValue > 0 && caloriesPer100gValue > 0 && !isSaving
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
                    Text("保存记录")
                }
            }
        }
    }
}