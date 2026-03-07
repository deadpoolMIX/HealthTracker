package com.example.healthtracker.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.healthtracker.data.local.entity.IntakeRecordEntity
import com.example.healthtracker.util.DateTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToAddIntake: () -> Unit,
    onNavigateToAddBodyData: () -> Unit,
    onNavigateToAddSleep: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToUserProfile: () -> Unit,
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToEditIntake: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var fabExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<IntakeRecordEntity?>(null) }
    var showContextMenu by remember { mutableStateOf<IntakeRecordEntity?>(null) }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showTargetCaloriesDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (selectionMode) {
                        Text(
                            text = "已选择 ${selectedIds.size} 项",
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Row(
                            modifier = Modifier.clickable(onClick = onNavigateToCalendar),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = DateTimeUtils.formatDate(uiState.selectedDate),
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = "查看日历",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (selectionMode) {
                        IconButton(onClick = {
                            selectionMode = false
                            selectedIds = emptySet()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "取消选择")
                        }
                    } else {
                        IconButton(onClick = onNavigateToUserProfile) {
                            Icon(
                                imageVector = Icons.Outlined.AccountCircle,
                                contentDescription = "用户资料"
                            )
                        }
                    }
                },
                actions = {
                    if (selectionMode) {
                        IconButton(onClick = {
                            if (selectedIds.size == uiState.todayIntake.size) {
                                selectedIds = emptySet()
                            } else {
                                selectedIds = uiState.todayIntake.map { it.id }.toSet()
                            }
                        }) {
                            Icon(
                                if (selectedIds.size == uiState.todayIntake.size)
                                    Icons.Default.Deselect
                                else
                                    Icons.Default.SelectAll,
                                contentDescription = "全选"
                            )
                        }
                        IconButton(
                            onClick = {
                                if (selectedIds.isNotEmpty()) {
                                    viewModel.deleteRecordsByIds(selectedIds.toList())
                                    selectionMode = false
                                    selectedIds = emptySet()
                                }
                            },
                            enabled = selectedIds.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "删除选中")
                        }
                    } else {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "设置"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (!selectionMode) {
                MultiActionFab(
                    expanded = fabExpanded,
                    onExpandChange = { fabExpanded = it },
                    onIntakeClick = {
                        fabExpanded = false
                        onNavigateToAddIntake()
                    },
                    onBodyClick = {
                        fabExpanded = false
                        onNavigateToAddBodyData()
                    },
                    onSleepClick = {
                        fabExpanded = false
                        onNavigateToAddSleep()
                    }
                )
            }
        }
    ) { paddingValues ->
        // 餐次分组颜色 - 统一使用主题色，比背景浅一点
        val mealCardColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 热量半圆显示
            item {
                CalorieArcCard(
                    consumed = uiState.totalCalories,
                    target = uiState.targetCalories,
                    bmr = uiState.bmr,
                    percentage = uiState.caloriePercentage,
                    onLongClick = { showTargetCaloriesDialog = true }
                )
            }

            // 今日营养素摄入
            item {
                NutrientSummaryCard(
                    carbs = uiState.totalCarbs,
                    protein = uiState.totalProtein,
                    fat = uiState.totalFat
                )
            }

            // 今日摄入记录
            item {
                Text(
                    text = "今日摄入",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (uiState.todayIntake.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "暂无摄入记录\n点击右下角 + 添加",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // 按餐次分组显示
                val mealTypes = listOf(0, 1, 2, 3) // 早餐、午餐、晚餐、加餐

                mealTypes.forEach { mealType ->
                    val recordsForMeal = uiState.todayIntake.filter { it.mealType == mealType }
                    if (recordsForMeal.isNotEmpty()) {
                        // 餐次分组卡片
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = mealCardColor
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    // 餐次标题
                                    Text(
                                        text = getMealTypeName(mealType),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // 该餐次的食物列表
                                    recordsForMeal.forEach { record ->
                                        MealIntakeItem(
                                            record = record,
                                            isSelected = selectedIds.contains(record.id),
                                            selectionMode = selectionMode,
                                            onClick = {
                                                if (selectionMode) {
                                                    selectedIds = if (selectedIds.contains(record.id)) {
                                                        selectedIds - record.id
                                                    } else {
                                                        selectedIds + record.id
                                                    }
                                                } else {
                                                    onNavigateToEditIntake(record.id)
                                                }
                                            },
                                            onLongClick = {
                                                if (!selectionMode) {
                                                    showContextMenu = record
                                                }
                                            }
                                        )
                                        if (record != recordsForMeal.last()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            // 今日身体数据
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "今日身体数据",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                BodyDataCard(
                    bodyRecord = uiState.todayBodyRecord,
                    onClick = onNavigateToAddBodyData
                )
            }

            // 今日睡眠
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "今日睡眠",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                SleepDataCard(
                    sleepRecord = uiState.todaySleepRecord,
                    onClick = onNavigateToAddSleep
                )
            }

            // 底部间距
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // 删除确认对话框
    showDeleteDialog?.let { record ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("删除记录") },
            text = { Text("确定要删除 \"${record.foodName}\" 吗？") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRecord(record)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("取消")
                }
            }
        )
    }

    // 长按菜单
    showContextMenu?.let { record ->
        AlertDialog(
            onDismissRequest = { showContextMenu = null },
            title = { Text(record.foodName) },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showContextMenu = null
                            onNavigateToEditIntake(record.id)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("编辑")
                    }
                    TextButton(
                        onClick = {
                            showContextMenu = null
                            showDeleteDialog = record
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("删除")
                    }
                    HorizontalDivider()
                    TextButton(
                        onClick = {
                            showContextMenu = null
                            selectionMode = true
                            selectedIds = setOf(record.id)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Checklist, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("批量选择")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showContextMenu = null }) {
                    Text("取消")
                }
            }
        )
    }

    // 设置目标卡路里对话框
    if (showTargetCaloriesDialog) {
        TargetCaloriesDialog(
            currentTarget = uiState.targetCalories,
            onDismiss = { showTargetCaloriesDialog = false },
            onConfirm = { newTarget ->
                viewModel.updateTargetCalories(newTarget)
                showTargetCaloriesDialog = false
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CalorieArcCard(
    consumed: Double,
    target: Double,
    bmr: Double,
    percentage: Int,
    onLongClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "今日可摄入热量",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 半圆进度条
            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                // 背景弧
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(180.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 16.dp,
                    strokeCap = StrokeCap.Round,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                // 进度弧
                CircularProgressIndicator(
                    progress = { (percentage / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier.size(180.dp),
                    color = if (percentage > 100) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    strokeWidth = 16.dp,
                    strokeCap = StrokeCap.Round,
                    trackColor = Color.Transparent
                )

                // 中心文字
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${consumed.toInt()}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "/ ${target.toInt()} kcal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // BMR 标记
            if (bmr > 0 && bmr < target) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "基础代谢: ${bmr.toInt()} kcal",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 剩余热量
            val remaining = target - consumed
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (remaining >= 0) "还可摄入 ${remaining.toInt()} kcal" else "已超出 ${(-remaining).toInt()} kcal",
                style = MaterialTheme.typography.bodyMedium,
                color = if (remaining >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun NutrientSummaryCard(
    carbs: Double,
    protein: Double,
    fat: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NutrientItem(
                name = "碳水",
                value = carbs,
                color = MaterialTheme.colorScheme.primary,
                unit = "g"
            )
            NutrientItem(
                name = "蛋白质",
                value = protein,
                color = MaterialTheme.colorScheme.secondary,
                unit = "g"
            )
            NutrientItem(
                name = "脂肪",
                value = fat,
                color = MaterialTheme.colorScheme.tertiary,
                unit = "g"
            )
        }
    }
}

@Composable
private fun NutrientItem(
    name: String,
    value: Double,
    color: Color,
    unit: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = String.format("%.1f", value),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = "$name ($unit)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 餐次分组内的食物项（带食物对应的emoji）
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MealIntakeItem(
    record: IntakeRecordEntity,
    isSelected: Boolean = false,
    selectionMode: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else
            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 选中复选框或食物图标
            if (selectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() }
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                // 食物对应的emoji图标
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getFoodEmoji(record.foodName),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            // 食物信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.foodName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${record.amount.toInt()}g",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 热量
            Text(
                text = "${record.calories.toInt()} kcal",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun IntakeRecordItem(
    record: IntakeRecordEntity,
    isSelected: Boolean = false,
    selectionMode: Boolean = false,
    showMealType: Boolean = true,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onSelect: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else
            null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 选中复选框或食物图标
            if (selectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() }
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getFoodEmoji(record.foodName),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            // 食物信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.foodName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // 只在需要时显示餐次
                Text(
                    text = if (showMealType) "${record.amount.toInt()}g · ${getMealTypeName(record.mealType)}" else "${record.amount.toInt()}g",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 热量
            Text(
                text = "${record.calories.toInt()} kcal",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun BodyDataCard(
    bodyRecord: com.example.healthtracker.data.local.entity.BodyRecordEntity?,
    onClick: () -> Unit
) {
    // 使用主题色，比背景浅一点
    val cardColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (bodyRecord == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "点击添加今日身体数据",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                bodyRecord.weight?.let {
                    DataItem(label = "体重", value = "${it} kg")
                }
                bodyRecord.bodyFatRate?.let {
                    DataItem(label = "体脂率", value = "${it}%")
                }
                bodyRecord.muscleMass?.let {
                    DataItem(label = "肌肉量", value = "${it} kg")
                }
            }
        }
    }
}

@Composable
private fun SleepDataCard(
    sleepRecord: com.example.healthtracker.data.local.entity.SleepRecordEntity?,
    onClick: () -> Unit
) {
    // 使用主题色，比背景浅一点
    val cardColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (sleepRecord == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "点击添加今日睡眠记录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DataItem(
                    label = "入睡",
                    value = DateTimeUtils.formatTime(sleepRecord.sleepTime)
                )
                DataItem(
                    label = "起床",
                    value = DateTimeUtils.formatTime(sleepRecord.wakeTime)
                )
                DataItem(
                    label = "时长",
                    value = DateTimeUtils.formatDuration(sleepRecord.duration)
                )
            }
        }
    }
}

@Composable
private fun DataItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MultiActionFab(
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onIntakeClick: () -> Unit,
    onBodyClick: () -> Unit,
    onSleepClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 展开的选项
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FabOption(
                    icon = Icons.Outlined.Restaurant,
                    label = "摄入",
                    onClick = onIntakeClick
                )
                FabOption(
                    icon = Icons.Outlined.MonitorWeight,
                    label = "身体",
                    onClick = onBodyClick
                )
                FabOption(
                    icon = Icons.Outlined.Bedtime,
                    label = "睡眠",
                    onClick = onSleepClick
                )
            }
        }

        // 主 FAB
        FloatingActionButton(
            onClick = { onExpandChange(!expanded) },
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = if (expanded) "关闭" else "添加"
            )
        }
    }
}

@Composable
private fun FabOption(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium
            )
        }
        FloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private fun getMealTypeName(mealType: Int): String {
    return when (mealType) {
        0 -> "早餐"
        1 -> "午餐"
        2 -> "晚餐"
        3 -> "加餐"
        else -> "其他"
    }
}

private fun getMealTypeEmoji(mealType: Int): String {
    return when (mealType) {
        0 -> "🌅"
        1 -> "☀️"
        2 -> "🌙"
        3 -> "🍎"
        else -> "🍽️"
    }
}

/**
 * 根据食物名称返回对应的emoji
 */
private fun getFoodEmoji(name: String): String {
    return when {
        name.contains("饭") || name.contains("粥") -> "🍚"
        name.contains("面") || name.contains("粉") -> "🍜"
        name.contains("馒头") || name.contains("包子") -> "🥟"
        name.contains("面包") -> "🍞"
        name.contains("猪") -> "🥩"
        name.contains("牛") -> "🥩"
        name.contains("羊") -> "🍖"
        name.contains("鸡") || name.contains("鸭") -> "🍗"
        name.contains("鱼") -> "🐟"
        name.contains("虾") || name.contains("蟹") -> "🦐"
        name.contains("蛋") -> "🥚"
        name.contains("奶") || name.contains("牛奶") -> "🥛"
        name.contains("豆") || name.contains("豆腐") -> "🫘"
        name.contains("蔬菜") || name.contains("白菜") || name.contains("青菜") -> "🥬"
        name.contains("西红柿") || name.contains("番茄") -> "🍅"
        name.contains("黄瓜") -> "🥒"
        name.contains("土豆") -> "🥔"
        name.contains("胡萝卜") -> "🥕"
        name.contains("苹果") -> "🍎"
        name.contains("香蕉") -> "🍌"
        name.contains("橙") || name.contains("橘子") -> "🍊"
        name.contains("葡萄") -> "🍇"
        name.contains("草莓") -> "🍓"
        name.contains("西瓜") -> "🍉"
        name.contains("水果") -> "🍇"
        name.contains("油") -> "🫒"
        name.contains("坚果") || name.contains("花生") || name.contains("核桃") -> "🥜"
        name.contains("零食") -> "🍪"
        name.contains("饮料") || name.contains("可乐") || name.contains("汽水") -> "🥤"
        name.contains("咖啡") -> "☕"
        name.contains("茶") -> "🍵"
        name.contains("酒") -> "🍺"
        name.contains("汤") -> "🥣"
        name.contains("水") -> "💧"
        else -> "🍽️"
    }
}

/**
 * 设置目标卡路里对话框
 */
@Composable
private fun TargetCaloriesDialog(
    currentTarget: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var targetValue by remember { mutableStateOf(currentTarget.toInt().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置每日目标热量") },
        text = {
            Column {
                Text(
                    text = "请输入每日目标摄入热量（kcal）",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = targetValue,
                    onValueChange = { targetValue = it.filter { c -> c.isDigit() } },
                    label = { Text("目标热量") },
                    suffix = { Text("kcal") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetValue.toDoubleOrNull()
                    if (target != null && target > 0) {
                        onConfirm(target)
                    }
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}