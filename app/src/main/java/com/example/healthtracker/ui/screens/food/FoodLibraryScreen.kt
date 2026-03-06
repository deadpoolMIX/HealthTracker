package com.example.healthtracker.ui.screens.food

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.healthtracker.data.local.entity.FoodEntity
import com.example.healthtracker.data.local.entity.IntakeRecordEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodLibraryScreen(
    viewModel: FoodLibraryViewModel = hiltViewModel(),
    onNavigateToAddCustomFood: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val recentRecords by viewModel.recentRecords.collectAsState()
    val customFoods by viewModel.customFoods.collectAsState()
    val favoriteFoods by viewModel.favoriteFoods.collectAsState()

    val tabs = listOf("最近摄入", "自定义食物", "收藏食物")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("食物库", fontWeight = FontWeight.Medium) }
            )
        },
        floatingActionButton = {
            // 只在自定义食物tab显示FAB
            if (uiState.selectedTabIndex == 1) {
                FloatingActionButton(
                    onClick = onNavigateToAddCustomFood,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "添加自定义食物")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = uiState.selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedTabIndex == index,
                        onClick = { viewModel.setSelectedTab(index) },
                        text = { Text(title) }
                    )
                }
            }

            // Content
            when (uiState.selectedTabIndex) {
                0 -> RecentFoodsTab(
                    records = recentRecords
                )
                1 -> CustomFoodsTab(
                    foods = customFoods,
                    onFavoriteClick = { viewModel.toggleFavorite(it) },
                    onDeleteClick = { viewModel.deleteCustomFood(it) }
                )
                2 -> FavoriteFoodsTab(
                    foods = favoriteFoods,
                    onFavoriteClick = { viewModel.toggleFavorite(it) }
                )
            }
        }
    }
}

@Composable
private fun RecentFoodsTab(
    records: List<IntakeRecordEntity>
) {
    if (records.isEmpty()) {
        EmptyState(message = "暂无最近摄入记录\n添加摄入后会显示在这里")
    } else {
        // 去重：同一个食物名称只显示一次
        val uniqueRecords = records
            .distinctBy { it.foodName }
            .take(20)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uniqueRecords) { record ->
                RecentFoodItem(record = record)
            }
        }
    }
}

@Composable
private fun CustomFoodsTab(
    foods: List<FoodEntity>,
    onFavoriteClick: (FoodEntity) -> Unit,
    onDeleteClick: (FoodEntity) -> Unit
) {
    if (foods.isEmpty()) {
        EmptyState(message = "暂无自定义食物\n点击右下角 + 添加")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(foods) { food ->
                FoodItem(
                    food = food,
                    onFavoriteClick = { onFavoriteClick(food) },
                    onDeleteClick = { onDeleteClick(food) }
                )
            }
            // 底部间距给FAB留空间
            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
private fun FavoriteFoodsTab(
    foods: List<FoodEntity>,
    onFavoriteClick: (FoodEntity) -> Unit
) {
    if (foods.isEmpty()) {
        EmptyState(message = "暂无收藏的食物\n点击食物右侧心形图标收藏")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(foods) { food ->
                FoodItem(
                    food = food,
                    onFavoriteClick = { onFavoriteClick(food) }
                )
            }
        }
    }
}

@Composable
private fun RecentFoodItem(record: IntakeRecordEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 食物图标
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getCategoryEmoji(record.foodName),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 食物信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.foodName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${record.caloriesPer100g.toInt()} kcal/100g",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 营养素信息
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "P ${record.proteinPer100g.toInt()}g",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "C ${record.carbsPer100g.toInt()}g",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "F ${record.fatPer100g.toInt()}g",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun FoodItem(
    food: FoodEntity,
    onFavoriteClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 食物图标
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getCategoryEmoji(food.name),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 食物信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${food.category} · ${food.calories.toInt()} kcal/100g",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 收藏按钮
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (food.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (food.isFavorite) "取消收藏" else "收藏",
                    tint = if (food.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 删除按钮（仅自定义食物显示）
            if (onDeleteClick != null) {
                IconButton(onClick = onDeleteClick) {
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

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun getCategoryEmoji(name: String): String {
    return when {
        name.contains("饭") || name.contains("面") || name.contains("粥") -> "🍚"
        name.contains("猪") || name.contains("牛") || name.contains("羊") -> "🥩"
        name.contains("鸡") || name.contains("鸭") -> "🍗"
        name.contains("鱼") || name.contains("虾") || name.contains("蟹") -> "🐟"
        name.contains("蛋") -> "🥚"
        name.contains("奶") || name.contains("牛奶") -> "🥛"
        name.contains("豆") -> "🫘"
        name.contains("蔬菜") || name.contains("菜") -> "🥬"
        name.contains("水果") || name.contains("苹果") || name.contains("香蕉") || name.contains("橙") -> "🍎"
        name.contains("油") -> "🫒"
        name.contains("坚果") || name.contains("花生") -> "🥜"
        else -> "🍽️"
    }
}