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
 * 编辑摄入记录页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditIntakeScreen(
    recordId: Long,
    onNavigateBack: () -> Unit,
    viewModel: EditIntakeViewModel = hiltViewModel()
) {
    val record by viewModel.record.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    // 加载记录
    LaunchedEffect(recordId) {
        viewModel.loadRecord(recordId)
    }

    var foodName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var amountInUnit by remember { mutableStateOf("") }
    var gramsPerUnit by remember { mutableStateOf("") }
    var caloriesPer100g by remember { mutableStateOf("") }
    var carbsPer100g by remember { mutableStateOf("") }
    var proteinPer100g by remember { mutableStateOf("") }
    var fatPer100g by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedMealType by remember { mutableIntStateOf(0) }

    val mealTypes = listOf("早餐", "午餐", "晚餐", "加餐")
    val commonUnits = listOf("克", "毫升", "个", "杯", "瓶", "份", "块", "片", "勺", "包")
    var expandedUnit by remember { mutableStateOf(false) }

    // 当记录加载完成后，填充表单
    LaunchedEffect(record) {
        record?.let { r ->
            foodName = r.foodName
            amount = r.amount.toString()
            unit = r.unit ?: ""
            amountInUnit = r.amountInUnit?.toString() ?: ""
            gramsPerUnit = r.gramsPerUnit?.toString() ?: ""
            caloriesPer100g = r.caloriesPer100g.toString()
            carbsPer100g = r.carbsPer100g.toString()
            proteinPer100g = r.proteinPer100g.toString()
            fatPer100g = r.fatPer100g.toString()
            note = r.note ?: ""
            selectedMealType = r.mealType
        }
    }

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
                title = { Text("编辑摄入记录", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (record == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
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
                    singleLine = true
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

                Spacer(modifier = Modifier.weight(1f))

                // 保存按钮
                Button(
                    onClick = {
                        if (foodName.isNotBlank() && amountValue > 0 && caloriesPer100gValue > 0) {
                            viewModel.updateRecord(
                                recordId = recordId,
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
                                note = note.takeIf { it.isNotBlank() }
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
                        Text("保存修改")
                    }
                }
            }
        }
    }
}