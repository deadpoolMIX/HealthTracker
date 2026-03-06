package com.example.healthtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.healthtracker.data.local.entity.FoodEntity
import com.example.healthtracker.ui.screens.intake.AddIntakeViewModel
import kotlinx.coroutines.FlowPreview

/**
 * 搜索食物页面
 * 从首页加号进入，显示食物库搜索 + 添加自定义食物按钮
 */
@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun AddIntakeScreen(
    viewModel: AddIntakeViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToCustomFood: (Long?) -> Unit = {},
    onNavigateToFoodDetail: (String, Double, Double, Double, Double, Int) -> Unit = { _, _, _, _, _, _ -> }
) {
    val searchResults by viewModel.searchResults.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedMealType by remember { mutableIntStateOf(0) }
    var showSearchResults by remember { mutableStateOf(true) }

    val mealTypes = listOf("早餐", "午餐", "晚餐", "加餐")

    // 搜索防抖
    LaunchedEffect(searchQuery) {
        viewModel.searchFoods(searchQuery)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加摄入", fontWeight = FontWeight.Medium) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 餐次选择
            Text(
                text = "选择餐次",
                style = MaterialTheme.typography.titleSmall
            )
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

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // 搜索食物
            Text(
                text = "搜索食物库",
                style = MaterialTheme.typography.titleSmall
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    showSearchResults = true
                },
                label = { Text("搜索食物名称") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "搜索")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            showSearchResults = true
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "清除")
                        }
                    }
                }
            )

            // 搜索结果列表
            if (showSearchResults && searchResults.isNotEmpty()) {
                Text(
                    text = "搜索结果 (${searchResults.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(searchResults.take(20)) { food ->
                        FoodSearchResultItem(
                            food = food,
                            onClick = {
                                // 点击食物，跳转到详细录入页面
                                onNavigateToFoodDetail(
                                    food.name,
                                    food.calories,
                                    food.carbohydrates,
                                    food.protein,
                                    food.fat,
                                    selectedMealType
                                )
                            }
                        )
                    }
                }
            } else if (searchQuery.isNotEmpty() && searchResults.isEmpty()) {
                // 无搜索结果
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "未找到 \"$searchQuery\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "点击下方按钮添加自定义食物",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // 初始状态
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "搜索食物库中的食物",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 添加自定义食物按钮
            Button(
                onClick = { onNavigateToCustomFood(null) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("添加自定义食物")
            }
        }
    }
}

@Composable
private fun FoodSearchResultItem(
    food: FoodEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getCategoryEmoji(food.category),
                    style = MaterialTheme.typography.titleSmall
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 食物信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${food.calories.toInt()} kcal/100g · ${food.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 箭头
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "选择",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getCategoryEmoji(category: String): String {
    return when (category) {
        "主食" -> "🍚"
        "肉类" -> "🥩"
        "蔬菜" -> "🥬"
        "水果" -> "🍎"
        "蛋奶" -> "🥚"
        "豆类" -> "🫘"
        "坚果" -> "🥜"
        "海鲜" -> "🐟"
        "油脂" -> "🫒"
        else -> "🍽️"
    }
}