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
import androidx.compose.material.icons.filled.Delete
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
 * 编辑周期食物页面
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditCycleFoodScreen(
    cycleFoodId: Long,
    viewModel: EditCycleFoodViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val cycleFood by viewModel.cycleFood.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            onNavigateBack()
        }
    }

    var name by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("🍽️") }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var caloriesPer100g by remember { mutableStateOf("") }
    var carbsPer100g by remember { mutableStateOf("") }
    var proteinPer100g by remember { mutableStateOf("") }
    var fatPer100g by remember { mutableStateOf("") }
    var totalWeight by remember { mutableStateOf("") }
    var estimatedDays by remember { mutableStateOf("") }

    LaunchedEffect(cycleFoodId) {
        viewModel.loadCycleFood(cycleFoodId)
    }

    LaunchedEffect(cycleFood) {
        cycleFood?.let { food ->
            name = food.name
            selectedEmoji = food.icon
            // 逆向推算每百克热量（用于编辑显示）
            val c100 = if (food.totalWeight > 0) (food.totalCalories * 100.0 / food.totalWeight) else 0.0
            val carbs100 = if (food.totalWeight > 0) (food.totalCarbs * 100.0 / food.totalWeight) else 0.0
            val p100 = if (food.totalWeight > 0) (food.totalProtein * 100.0 / food.totalWeight) else 0.0
            val f100 = if (food.totalWeight > 0) (food.totalFat * 100.0 / food.totalWeight) else 0.0
            
            caloriesPer100g = String.format("%.1f", c100)
            carbsPer100g = String.format("%.1f", carbs100)
            proteinPer100g = String.format("%.1f", p100)
            fatPer100g = String.format("%.1f", f100)
            totalWeight = food.totalWeight.toString()
            estimatedDays = food.expectedDays.toString()
        }
    }

    val caloriesValue = caloriesPer100g.toDoubleOrNull() ?: 0.0
    val carbsValue = carbsPer100g.toDoubleOrNull() ?: 0.0
    val proteinValue = proteinPer100g.toDoubleOrNull() ?: 0.0
    val fatValue = fatPer100g.toDoubleOrNull() ?: 0.0
    val weightValue = totalWeight.toDoubleOrNull() ?: 0.0
    val daysValue = estimatedDays.toIntOrNull() ?: 1

    // 计算总营养值
    val totalCalories = (weightValue / 100.0) * caloriesValue
    val totalCarbs = (weightValue / 100.0) * carbsValue
    val totalProtein = (weightValue / 100.0) * proteinValue
    val totalFat = (weightValue / 100.0) * fatValue

    val isValid = name.isNotBlank() && caloriesValue > 0 && weightValue > 0 && daysValue > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑周期食物", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (cycleFood == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
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
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("食物名称 *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "图标", style = MaterialTheme.typography.titleSmall)
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

                Text(text = "每百克营养数据", style = MaterialTheme.typography.titleSmall)

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

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(text = "周期设置", style = MaterialTheme.typography.titleSmall)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = totalWeight,
                        onValueChange = { totalWeight = it },
                        label = { Text("总重量 (g) *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = estimatedDays,
                        onValueChange = { estimatedDays = it },
                        label = { Text("预计天数 *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                if (weightValue > 0 && caloriesValue > 0) {
                    val dailyWeight = weightValue / daysValue
                    val dailyCalories = totalCalories / daysValue

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "周期摘要", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("总热量: ${String.format("%.0f", totalCalories)} kcal")
                            Text("平均每天: ${String.format("%.0f", dailyWeight)}g / ${String.format("%.0f", dailyCalories)} kcal")
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        viewModel.updateCycleFood(
                            id = cycleFoodId,
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
                    } else {
                        Text("更新周期食物")
                    }
                }
            }
        }
    }

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
                    .height(450.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                FoodEmojiUtils.foodEmojisByCategory.forEach { (category, emojis) ->
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        emojis.forEach { (emoji, _) ->
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (emoji == selectedEmoji)
                                            MaterialTheme.colorScheme.primaryContainer
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
