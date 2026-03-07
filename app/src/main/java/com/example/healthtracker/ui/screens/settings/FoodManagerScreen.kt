package com.example.healthtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.healthtracker.data.local.entity.FoodEntity
import com.example.healthtracker.ui.screens.settings.FoodManagerViewModel
import com.example.healthtracker.util.FoodEmojiUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodManagerScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddCustomFood: () -> Unit = {},
    viewModel: FoodManagerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val foods by viewModel.filteredFoods.collectAsStateWithLifecycle()
    val allFoods by viewModel.allFoods.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // 按首字母分组
    val groupedFoods = foods.groupBy { food ->
        food.name.firstOrNull()?.let { viewModel.getFirstLetter(it) } ?: "#"
    }.toSortedMap()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("食物管理", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddCustomFood,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "添加食物")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 搜索框
            OutlinedTextField(
                value = uiState.searchText,
                onValueChange = { viewModel.setSearchText(it) },
                label = { Text("共有 ${allFoods.size} 个食物") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "搜索")
                },
                trailingIcon = {
                    if (uiState.searchText.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchText("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "清除")
                        }
                    }
                }
            )

            // 食物列表 + 字母导航栏
            if (foods.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (uiState.searchText.isNotEmpty()) "未找到匹配的食物" else "暂无食物数据",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Row(modifier = Modifier.fillMaxSize()) {
                    // 食物列表
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        groupedFoods.forEach { (letter, foodsInGroup) ->
                            // 首字母标题
                            item {
                                Text(
                                    text = letter,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            // 该字母下的食物
                            items(foodsInGroup) { food ->
                                FoodManagerItem(
                                    food = food,
                                    onFavoriteClick = { viewModel.toggleFavorite(food) },
                                    onDeleteClick = { viewModel.deleteFood(food) }
                                )
                            }
                        }
                        // 底部留白给FAB
                        item {
                            Spacer(modifier = Modifier.height(60.dp))
                        }
                    }

                    // 字母导航栏
                    AlphabetIndexBar(
                        letters = groupedFoods.keys.toList(),
                        onLetterClick = { letter ->
                            // 滚动到对应字母位置
                            val index = groupedFoods.keys.indexOf(letter)
                            if (index >= 0) {
                                scope.launch {
                                    listState.animateScrollToItem(index * 2) // 每组有一个标题 + 食物列表
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FoodManagerItem(
    food: FoodEntity,
    onFavoriteClick: () -> Unit,
    onDeleteClick: () -> Unit
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
                if (food.isCustom) {
                    Text(
                        text = "自定义",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
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
            if (food.isCustom) {
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
private fun AlphabetIndexBar(
    letters: List<String>,
    onLetterClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(end = 4.dp)
            .width(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        letters.forEach { letter ->
            Text(
                text = letter,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { onLetterClick(letter) }
                    .padding(1.dp)
            )
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