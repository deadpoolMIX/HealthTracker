package com.example.healthtracker.ui.screens

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onNavigateBack: () -> Unit
) {
    var gender by remember { mutableIntStateOf(0) }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var activityLevel by remember { mutableIntStateOf(1) }
    val activityLevels = listOf("久坐", "轻度活动", "中度活动", "重度活动", "极重度活动")

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
                        text = "基础代谢率 (BMR): -- kcal",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "每日总能量消耗 (TDEE): -- kcal",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存")
            }
        }
    }
}