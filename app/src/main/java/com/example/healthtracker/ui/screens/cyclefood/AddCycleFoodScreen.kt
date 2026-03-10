package com.example.healthtracker.ui.screens.cyclefood

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.healthtracker.util.FoodEmojiUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCycleFoodScreen(
    viewModel: AddCycleFoodViewModel = hiltViewModel(),
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

    var name by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("🍽️") }
    var showEmojiPicker by remember { mutableStateOf(false) }

    // 每百克营养数据
    var caloriesPer100g by remember { mutableStateOf("") }
    var carbsPer100g by remember { mutableStateOf("") }
    var proteinPer100g by remember { mutableStateOf("") }
    var fatPer100g by remember { mutableStateOf("") }

    // 总重量
    var totalWeight by remember { mutableStateOf("") }

    // 预计天数
    var expectedDays by remember { mutableStateOf("3") }

    // 解析输入值
    val calories100 = caloriesPer100g.toDoubleOrNull() ?: 0.0
    val carbs100 = carbsPer100g.toDoubleOrNull() ?: 0.0
    val protein100 = proteinPer100g.toDoubleOrNull() ?: 0.0
    val fat100 = fatPer100g.toDoubleOrNull() ?: 0.0
    val weight = totalWeight.toDoubleOrNull() ?: 0.0
    val daysValue = expectedDays.toIntOrNull() ?: 3

    // 计算总营养数据
    val totalCalories = calories100 * weight / 100
    val totalCarbs = carbs100 * weight / 100
    val totalProtein = protein100 * weight / 100
    val totalFat = fat100 * weight / 100

    // 计算每份数据
    val portionCalories = if (daysValue > 0) totalCalories / daysValue else totalCalories
    val portionCarbs = if (daysValue > 0) totalCarbs / daysValue else totalCarbs
    val portionProtein = if (daysValue > 0) totalProtein / daysValue else totalProtein
    val portionFat = if (daysValue > 0) totalFat / daysValue else totalFat

    // 验证输入
    val isValid = name.isNotBlank() &&
            calories100 > 0 &&
            weight > 0 &&
            daysValue > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加周期食物", fontWeight = FontWeight.Medium) },
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
            // 说明卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "周期食物",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "适用于分多天吃完的食物，如一整个蛋糕。输入每百克营养数据和总重量，系统自动计算。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 食物名称
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
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
                Text("图标", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { showEmojiPicker = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = selectedEmoji, fontSize = 24.sp)
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
                label = { Text("热量 (kcal/100g)") },
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
                    label = { Text("碳水 (g/100g)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = proteinPer100g,
                    onValueChange = { proteinPer100g = it },
                    label = { Text("蛋白质 (g/100g)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            OutlinedTextField(
                value = fatPer100g,
                onValueChange = { fatPer100g = it },
                label = { Text("脂肪 (g/100g)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // 总重量
            OutlinedTextField(
                value = totalWeight,
                onValueChange = { totalWeight = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("总重量 (g) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                supportingText = { Text("整个食物的总重量") }
            )

            // 预计天数
            OutlinedTextField(
                value = expectedDays,
                onValueChange = { expectedDays = it.filter { c -> c.isDigit() } },
                label = { Text("预计几天吃完 *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // 计算结果预览
            if (weight > 0 && calories100 > 0 && daysValue > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "计算结果",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // 总营养
                        Text(
                            text = "总营养数据",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("热量: ${String.format("%.1f", totalCalories)} kcal")
                            Text("碳水: ${String.format("%.1f", totalCarbs)} g")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("蛋白质: ${String.format("%.1f", totalProtein)} g")
                            Text("脂肪: ${String.format("%.1f", totalFat)} g")
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        // 每份
                        Text(
                            text = "每份营养（每天吃一份）",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("热量: ${String.format("%.1f", portionCalories)} kcal")
                            Text("碳水: ${String.format("%.1f", portionCarbs)} g")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("蛋白质: ${String.format("%.1f", portionProtein)} g")
                            Text("脂肪: ${String.format("%.1f", portionFat)} g")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 保存按钮
            Button(
                onClick = {
                    viewModel.saveCycleFood(
                        name = name,
                        icon = selectedEmoji,
                        totalCalories = totalCalories,
                        totalCarbs = totalCarbs,
                        totalProtein = totalProtein,
                        totalFat = totalFat,
                        expectedDays = daysValue
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
                    Text("保存并开始")
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
                                    fontSize = 24.sp
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