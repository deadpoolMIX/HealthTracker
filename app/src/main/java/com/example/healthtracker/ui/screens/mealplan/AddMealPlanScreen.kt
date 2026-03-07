package com.example.healthtracker.ui.screens.mealplan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.healthtracker.data.local.entity.FoodEntity
import com.example.healthtracker.data.local.entity.MealPlanItemEntity
import com.example.healthtracker.util.FoodEmojiUtils
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun AddMealPlanScreen(
    viewModel: AddMealPlanViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToCustomFood: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    // 搜索相关状态
    var searchQuery by remember { mutableStateOf("") }
    var showSearchMode by remember { mutableStateOf(false) }
    val searchResults by viewModel.searchResults.collectAsState()

    val planTypes = listOf("单餐", "单日（三餐）", "周计划（每天三餐）")
    val mealTypes = listOf("早餐", "午餐", "晚餐", "加餐")
    val weekDays = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

    // 搜索防抖
    LaunchedEffect(searchQuery) {
        viewModel.searchFoods(searchQuery)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加饮食计划", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (showSearchMode) {
                            showSearchMode = false
                            searchQuery = ""
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (showSearchMode) {
            // 搜索食物模式
            SearchFoodContent(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                searchResults = searchResults,
                onFoodSelect = { food ->
                    // 将选中的食物添加到计划中
                    val item = MealPlanItemEntity(
                        id = 0,
                        planId = 0,
                        foodName = food.name,
                        amount = 100.0,
                        mealType = uiState.currentMealType,
                        dayOfWeek = if (uiState.planType == 2) uiState.currentDayOfWeek else null,
                        caloriesPer100g = food.calories,
                        carbsPer100g = food.carbohydrates,
                        proteinPer100g = food.protein,
                        fatPer100g = food.fat
                    )
                    viewModel.addItem(item)
                    showSearchMode = false
                    searchQuery = ""
                },
                onAddCustomFood = {
                    // 添加自定义食物
                    onNavigateToCustomFood()
                },
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            // 正常编辑模式
            EditPlanContent(
                uiState = uiState,
                viewModel = viewModel,
                planTypes = planTypes,
                mealTypes = mealTypes,
                weekDays = weekDays,
                onAddFoodClick = { showSearchMode = true },
                onSaveClick = {
                    scope.launch {
                        if (viewModel.savePlan()) {
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchFoodContent(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchResults: List<FoodEntity>,
    onFoodSelect: (FoodEntity) -> Unit,
    onAddCustomFood: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 搜索框
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("搜索食物名称") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "搜索")
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "清除")
                    }
                }
            }
        )

        // 搜索结果
        if (searchResults.isNotEmpty()) {
            Text(
                text = "搜索结果 (${searchResults.size})",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(searchResults.take(20)) { food ->
                    FoodSearchItem(
                        food = food,
                        onClick = { onFoodSelect(food) }
                    )
                }
            }
        } else if (searchQuery.isNotEmpty()) {
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
                }
            }
        } else {
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
        OutlinedButton(
            onClick = onAddCustomFood,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("添加自定义食物")
        }
    }
}

@Composable
private fun FoodSearchItem(
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
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getFoodEmoji(food),
                    style = MaterialTheme.typography.titleSmall
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

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

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "选择",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditPlanContent(
    uiState: AddMealPlanUiState,
    viewModel: AddMealPlanViewModel,
    planTypes: List<String>,
    mealTypes: List<String>,
    weekDays: List<String>,
    onAddFoodClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 计划名称
        OutlinedTextField(
            value = uiState.planName,
            onValueChange = { viewModel.setPlanName(it) },
            label = { Text("计划名称 *") },
            placeholder = { Text("例如：减脂期一周食谱") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // 计划类型
        Text(
            text = "计划类型",
            style = MaterialTheme.typography.titleSmall
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            planTypes.forEachIndexed { index, type ->
                FilterChip(
                    selected = uiState.planType == index,
                    onClick = { viewModel.setPlanType(index) },
                    label = { Text(type) }
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // 餐次选择（针对单日和周计划）
        if (uiState.planType >= 1) {
            Text(
                text = "当前编辑餐次",
                style = MaterialTheme.typography.titleSmall
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                mealTypes.forEachIndexed { index, type ->
                    FilterChip(
                        selected = uiState.currentMealType == index,
                        onClick = { viewModel.setCurrentMealType(index) },
                        label = { Text(type) }
                    )
                }
            }
        }

        // 星期选择（针对周计划）
        if (uiState.planType == 2) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "当前编辑星期",
                style = MaterialTheme.typography.titleSmall
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                weekDays.forEachIndexed { index, day ->
                    FilterChip(
                        selected = uiState.currentDayOfWeek == index,
                        onClick = { viewModel.setCurrentDayOfWeek(index) },
                        label = { Text(day) },
                        modifier = Modifier.sizeIn(minWidth = 40.dp)
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // 已添加的食物列表
        Text(
            text = "已添加食物 (${uiState.items.size})",
            style = MaterialTheme.typography.titleSmall
        )

        if (uiState.items.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "点击下方按钮添加食物",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            uiState.items.forEachIndexed { index, item ->
                MealPlanItemCard(
                    item = item,
                    mealTypeName = viewModel.getMealTypeName(item.mealType),
                    dayName = item.dayOfWeek?.let { viewModel.getDayName(it) },
                    onEdit = { viewModel.removeItem(index) },
                    onDelete = { viewModel.removeItem(index) }
                )
            }
        }

        // 添加食物按钮
        Button(
            onClick = onAddFoodClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("从食物库添加")
        }

        Spacer(modifier = Modifier.weight(1f))

        // 保存按钮
        Button(
            onClick = onSaveClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.planName.isNotBlank() && uiState.items.isNotEmpty()
        ) {
            Text("保存计划")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealPlanItemCard(
    item: MealPlanItemEntity,
    mealTypeName: String,
    dayName: String?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.foodName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = buildString {
                        append(mealTypeName)
                        if (dayName != null) {
                            append(" · $dayName")
                        }
                        append(" · ${item.amount.toInt()}g")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "编辑")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "删除")
            }
        }
    }
}

private fun getFoodEmoji(food: FoodEntity): String {
    // 如果食物有自定义图标，使用自定义图标
    if (food.icon.isNotEmpty() && food.icon != "custom") {
        return food.icon
    }
    // 否则根据名称推断
    return FoodEmojiUtils.getDefaultEmojiForFood(food.name)
}