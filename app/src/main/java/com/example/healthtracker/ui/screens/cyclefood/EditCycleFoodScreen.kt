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
fun EditCycleFoodScreen(
    cycleFoodId: Long,
    viewModel: EditCycleFoodViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val cycleFood by viewModel.cycleFood.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    // 加载数据
    LaunchedEffect(cycleFoodId) {
        viewModel.loadCycleFood(cycleFoodId)
    }

    // 保存成功后返回
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            onNavigateBack()
        }
    }

    // 等待数据加载
    if (cycleFood == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    var name by remember { mutableStateOf(cycleFood!!.name) }
    var selectedEmoji by remember { mutableStateOf(cycleFood!!.icon) }
    var showEmojiPicker by remember { mutableStateOf(false) }

    // 从总数反推每百克数据（假设用户可能知道原始重量）
    // 这里用总数据显示，编辑时直接修改总数
    var totalCalories by remember { mutableStateOf(cycleFood!!.totalCalories.toString()) }
    var totalCarbs by remember { mutableStateOf(cycleFood!!.totalCarbs.toString()) }
    var totalProtein by remember { mutableStateOf(cycleFood!!.totalProtein.toString()) }
    var totalFat by remember { mutableStateOf(cycleFood!!.totalFat.toString()) }
    var expectedDays by remember { mutableStateOf(cycleFood!!.expectedDays.toString()) }

    // 解析输入值
    val caloriesValue = totalCalories.toDoubleOrNull() ?: 0.0
    val carbsValue = totalCarbs.toDoubleOrNull() ?: 0.0
    val proteinValue = totalProtein.toDoubleOrNull() ?: 0.0
    val fatValue = totalFat.toDoubleOrNull() ?: 0.0
    val daysValue = expectedDays.toIntOrNull() ?: 3

    // 计算每份数据
    val portionCalories = if (daysValue > 0) caloriesValue / daysValue else caloriesValue
    val portionCarbs = if (daysValue > 0) carbsValue / daysValue else carbsValue
    val portionProtein = if (daysValue > 0) proteinValue / daysValue else proteinValue
    val portionFat = if (daysValue > 0) fatValue / daysValue else fatValue

    // 验证输入
    val isValid = name.isNotBlank() &&
            caloriesValue > 0 &&
            daysValue > 0

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

            // 总营养数据（直接编辑）
            Text(
                text = "总营养数据 *",
                style = MaterialTheme.typography.titleSmall
            )

            OutlinedTextField(
                value = totalCalories,
                onValueChange = { totalCalories = it },
                label = { Text("总热量 (kcal)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = totalCarbs,
                    onValueChange = { totalCarbs = it },
                    label = { Text("总碳水 (g)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = totalProtein,
                    onValueChange = { totalProtein = it },
                    label = { Text("总蛋白质 (g)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            OutlinedTextField(
                value = totalFat,
                onValueChange = { totalFat = it },
                label = { Text("总脂肪 (g)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // 预计天数
            OutlinedTextField(
                value = expectedDays,
                onValueChange = { expectedDays = it.filter { c -> c.isDigit() } },
                label = { Text("预计几天吃完 *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // 每份预览
            if (caloriesValue > 0 && daysValue > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "每份营养（每天吃一份）",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
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
                    viewModel.updateCycleFood(
                        id = cycleFoodId,
                        name = name,
                        icon = selectedEmoji,
                        totalCalories = caloriesValue,
                        totalCarbs = carbsValue,
                        totalProtein = proteinValue,
                        totalFat = fatValue,
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
                    Text("保存修改")
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