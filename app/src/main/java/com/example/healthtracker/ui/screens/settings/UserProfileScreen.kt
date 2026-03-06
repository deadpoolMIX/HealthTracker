package com.example.healthtracker.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val saveSuccess by viewModel.saveSuccess.collectAsStateWithLifecycle()

    // 初始化表单值
    var gender by remember { mutableIntStateOf(settings?.gender ?: 0) }
    var height by remember { mutableStateOf(settings?.height?.toString() ?: "") }
    var weight by remember { mutableStateOf(settings?.weight?.toString() ?: "") }
    var age by remember { mutableStateOf(settings?.age?.toString() ?: "") }
    var activityLevel by remember { mutableIntStateOf(settings?.activityLevel ?: 1) }

    // 当 settings 加载完成后更新表单值
    LaunchedEffect(settings) {
        settings?.let { s ->
            gender = s.gender
            height = s.height?.toString() ?: ""
            weight = s.weight?.toString() ?: ""
            age = s.age?.toString() ?: ""
            activityLevel = s.activityLevel
        }
    }

    // 保存成功后返回
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            viewModel.resetSaveSuccess()
            onNavigateBack()
        }
    }

    val activityLevels = listOf("久坐", "轻度活动", "中度活动", "重度活动", "极重度活动")

    // 计算预览结果
    val heightValue = height.toDoubleOrNull() ?: 0.0
    val weightValue = weight.toDoubleOrNull() ?: 0.0
    val ageValue = age.toIntOrNull() ?: 0

    val bmr = if (heightValue > 0 && weightValue > 0 && ageValue > 0) {
        HealthCalculator.calculateBMR(gender, weightValue, heightValue, ageValue)
    } else null

    val tdee = if (bmr != null) {
        HealthCalculator.calculateTDEE(bmr, activityLevel)
    } else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("个人资料", fontWeight = FontWeight.Medium) },
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
                .padding(16.dp)
        ) {
            // 性别
            Text(
                text = "性别",
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FilterChip(
                    selected = gender == 0,
                    onClick = { gender = 0 },
                    label = { Text("男") }
                )
                FilterChip(
                    selected = gender == 1,
                    onClick = { gender = 1 },
                    label = { Text("女") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 身高
            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = { Text("身高 (cm)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 体重
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("体重 (kg)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 年龄
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("年龄") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 活动水平
            Text(
                text = "活动水平",
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column {
                activityLevels.forEachIndexed { index, level ->
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = activityLevel == index,
                            onClick = { activityLevel = index }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(level)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 计算结果显示
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "计算结果",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (bmr != null) {
                            "基础代谢率 (BMR): ${bmr.roundToInt()} kcal"
                        } else {
                            "基础代谢率 (BMR): -- kcal"
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = if (tdee != null) {
                            "每日总能量消耗 (TDEE): ${tdee.roundToInt()} kcal"
                        } else {
                            "每日总能量消耗 (TDEE): -- kcal"
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val h = height.toDoubleOrNull() ?: 0.0
                    val w = weight.toDoubleOrNull() ?: 0.0
                    val a = age.toIntOrNull() ?: 0

                    if (h > 0 && w > 0 && a > 0) {
                        viewModel.saveUserInfo(gender, a, h, w, activityLevel)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = heightValue > 0 && weightValue > 0 && ageValue > 0
            ) {
                Text("保存")
            }
        }
    }
}

// 引用 HealthCalculator
private object HealthCalculator {
    fun calculateBMR(gender: Int, weight: Double, height: Double, age: Int): Double {
        return if (gender == 0) {
            10 * weight + 6.25 * height - 5 * age + 5
        } else {
            10 * weight + 6.25 * height - 5 * age - 161
        }
    }

    fun calculateTDEE(bmr: Double, activityLevel: Int): Double {
        val multipliers = listOf(1.2, 1.375, 1.55, 1.725, 1.9)
        val multiplier = multipliers.getOrElse(activityLevel) { 1.2 }
        return bmr * multiplier
    }
}