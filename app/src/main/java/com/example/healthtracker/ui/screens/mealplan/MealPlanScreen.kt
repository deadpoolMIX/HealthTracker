package com.example.healthtracker.ui.screens.mealplan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.healthtracker.data.local.entity.MealPlanEntity
import com.example.healthtracker.data.local.entity.MealPlanItemEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanScreen(
    viewModel: MealPlanViewModel = hiltViewModel(),
    onNavigateToAddPlan: () -> Unit = {},
    onNavigateToEditPlan: (Long) -> Unit = {}
) {
    val allPlans by viewModel.allPlans.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val selectedPlanId = uiState.selectedPlanId
    val selectedPlanItems by viewModel.selectedPlanItems.collectAsState()

    var showDeleteDialog by remember { mutableStateOf<MealPlanEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("饮食计划", fontWeight = FontWeight.Medium) },
                actions = {
                    // 排序按钮
                    IconButton(onClick = { viewModel.showSortDialog() }) {
                        Icon(Icons.Default.Sort, contentDescription = "排序")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddPlan,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加计划")
            }
        }
    ) { paddingValues ->
        if (allPlans.isEmpty()) {
            EmptyPlanState(onAddClick = onNavigateToAddPlan)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 当前排序方式提示
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "排序：${viewModel.getSortTypeName(uiState.sortType)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { viewModel.showSortDialog() }) {
                            Text("更改", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                // 计划列表
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(allPlans, key = { it.id }) { plan ->
                        MealPlanCard(
                            plan = plan,
                            isSelected = selectedPlanId == plan.id,
                            planTypeName = viewModel.getPlanTypeName(plan.planType),
                            onClick = {
                                // 点击计划跳转到编辑页面
                                onNavigateToEditPlan(plan.id)
                            },
                            onToggleActive = { viewModel.togglePlanActive(plan) },
                            onDelete = { showDeleteDialog = plan }
                        )
                    }
                }
            }
        }
    }

    // 排序选择对话框
    if (uiState.showSortDialog) {
        SortDialog(
            currentSortType = uiState.sortType,
            onDismiss = { viewModel.hideSortDialog() },
            onSelect = { viewModel.setSortType(it) }
        )
    }

    // 删除确认对话框
    showDeleteDialog?.let { plan ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("删除计划") },
            text = { Text("确定要删除「${plan.name}」吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePlan(plan)
                        showDeleteDialog = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun SortDialog(
    currentSortType: SortType,
    onDismiss: () -> Unit,
    onSelect: (SortType) -> Unit
) {
    val sortOptions = listOf(
        SortType.TIME_DESC to "时间 新-旧",
        SortType.TIME_ASC to "时间 旧-新",
        SortType.NAME_ASC to "名称 A-Z",
        SortType.NAME_DESC to "名称 Z-A",
        SortType.MANUAL to "手动排序"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("排序方式") },
        text = {
            Column {
                sortOptions.forEach { (sortType, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(sortType) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentSortType == sortType,
                            onClick = { onSelect(sortType) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

@Composable
private fun EmptyPlanState(onAddClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.RestaurantMenu,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = "暂无饮食计划",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "点击右下角 + 创建你的第一个饮食计划",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            OutlinedButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("创建计划")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealPlanCard(
    plan: MealPlanEntity,
    isSelected: Boolean,
    planTypeName: String,
    onClick: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 计划图标
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (plan.isActive)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (plan.planType) {
                            0 -> Icons.Filled.Restaurant
                            1 -> Icons.Filled.Today
                            2 -> Icons.Filled.DateRange
                            else -> Icons.Filled.CalendarMonth
                        },
                        contentDescription = null,
                        tint = if (plan.isActive)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 计划信息
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plan.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = planTypeName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 激活开关
                Switch(
                    checked = plan.isActive,
                    onCheckedChange = { onToggleActive() }
                )
            }

            // 展开指示器
            if (isSelected) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanItemsList(items: List<MealPlanItemEntity>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "计划内容",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 按餐次分组显示
            val groupedByMeal = items.groupBy { it.mealType }
            val mealOrder = listOf(0, 1, 2, 3)
            val mealNames = listOf("早餐", "午餐", "晚餐", "加餐")

            mealOrder.forEach { mealType ->
                val mealItems = groupedByMeal[mealType] ?: return@forEach
                if (mealItems.isNotEmpty()) {
                    Text(
                        text = mealNames[mealType],
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    mealItems.forEach { item ->
                        PlanItemRow(item = item)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun PlanItemRow(item: MealPlanItemEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "• ${item.foodName}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${item.amount.toInt()}g",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}