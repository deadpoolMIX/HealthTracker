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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.healthtracker.data.local.entity.FoodEntity
import com.example.healthtracker.util.FoodEmojiUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodLibraryScreen(
    viewModel: FoodLibraryViewModel = hiltViewModel(),
    onNavigateToAddCustomFood: () -> Unit = {},
    onNavigateToEditFood: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredRecentFoods by viewModel.filteredRecentFoods.collectAsState()
    val filteredCustomFoods by viewModel.filteredCustomFoods.collectAsState()

    val tabs = listOf("最近摄入", "自定义食物")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (uiState.isSearching) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("搜索食物...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                    } else {
                        Text("食物库", fontWeight = FontWeight.Medium)
                    }
                },
                actions = {
                    if (uiState.isSearching) {
                        IconButton(onClick = { viewModel.closeSearch() }) {
                            Icon(Icons.Default.Close, contentDescription = "关闭搜索")
                        }
                    } else {
                        IconButton(onClick = { viewModel.toggleSearch() }) {
                            Icon(Icons.Default.Search, contentDescription = "搜索")
                        }
                    }
                }
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
                    foods = filteredRecentFoods,
                    onFoodClick = { food -> onNavigateToEditFood(food.id) },
                    searchQuery = uiState.searchQuery
                )
                1 -> CustomFoodsTab(
                    foods = filteredCustomFoods,
                    onFoodClick = { food -> onNavigateToEditFood(food.id) },
                    onDeleteClick = { viewModel.deleteCustomFood(it) },
                    searchQuery = uiState.searchQuery
                )
            }
        }
    }
}

@Composable
private fun RecentFoodsTab(
    foods: List<FoodEntity>,
    onFoodClick: (FoodEntity) -> Unit,
    searchQuery: String = ""
) {
    if (foods.isEmpty()) {
        EmptyState(
            message = if (searchQuery.isNotBlank()) "未找到匹配的食物" else "暂无食物数据\n食物库中的食物将显示在这里"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(foods) { food ->
                FoodItem(
                    food = food,
                    onClick = { onFoodClick(food) }
                )
            }
        }
    }
}

@Composable
private fun CustomFoodsTab(
    foods: List<FoodEntity>,
    onFoodClick: (FoodEntity) -> Unit,
    onDeleteClick: (FoodEntity) -> Unit,
    searchQuery: String = ""
) {
    if (foods.isEmpty()) {
        EmptyState(
            message = if (searchQuery.isNotBlank()) "未找到匹配的自定义食物" else "暂无自定义食物\n点击右下角 + 添加"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(foods) { food ->
                FoodItem(
                    food = food,
                    onClick = { onFoodClick(food) },
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

@Composable
private fun FoodItem(
    food: FoodEntity,
    onClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            ),
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
                    text = getFoodEmoji(food),
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

            // 点击编辑指示（仅自定义食物显示）
            if (onClick != null && onDeleteClick != null) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "编辑",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun getFoodEmoji(food: FoodEntity): String {
    // 如果食物有自定义图标且是emoji格式，使用自定义图标
    if (food.icon.isNotEmpty() && food.icon != "custom" && isEmoji(food.icon)) {
        return food.icon
    }
    // 否则根据名称推断
    return FoodEmojiUtils.getDefaultEmojiForFood(food.name)
}

/**
 * 检查字符串是否为emoji
 */
private fun isEmoji(text: String): Boolean {
    if (text.isEmpty()) return false
    val firstChar = text[0]
    // Emoji 的 Unicode 范围检查
    return firstChar.code in 0x1F300..0x1F9FF ||
            firstChar.code in 0x2600..0x26FF ||
            firstChar.code in 0x2700..0x27BF
}