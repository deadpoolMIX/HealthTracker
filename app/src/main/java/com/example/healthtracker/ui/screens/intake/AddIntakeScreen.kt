package com.example.healthtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIntakeScreen(
    onNavigateBack: () -> Unit
) {
    var foodName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedMealType by remember { mutableIntStateOf(0) }
    val mealTypes = listOf("早餐", "午餐", "晚餐", "加餐")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加摄入记录", fontWeight = FontWeight.Medium) },
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
                .padding(16.dp)
        ) {
            // 餐次选择
            Text(
                text = "餐次",
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
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

            Spacer(modifier = Modifier.height(24.dp))

            // 食物搜索
            OutlinedTextField(
                value = foodName,
                onValueChange = { foodName = it },
                label = { Text("搜索食物") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 摄入量
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("摄入量 (g)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.weight(1f))

            // 保存按钮
            Button(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存记录")
            }
        }
    }
}