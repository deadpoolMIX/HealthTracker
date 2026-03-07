package com.example.healthtracker.ui.screens.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.healthtracker.data.local.entity.IntakeRecordEntity
import com.example.healthtracker.util.DateTimeUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val maxCalories = viewModel.getMaxCalories()
    var showMonthYearPicker by remember { mutableStateOf(false) }

    // 用于控制滑动方向和动画
    var slideDirection by remember { mutableStateOf(0) } // -1 向左, 1 向右
    var totalDrag by remember { mutableFloatStateOf(0f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("日历", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        // 整个页面使用 verticalScroll，一起滑动
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .pointerInput(Unit) {
                    // 左右滑动切换月份
                    // 从左向右滑动(totalDrag > 0) -> 上个月
                    // 从右向左滑动(totalDrag < 0) -> 下个月
                    detectHorizontalDragGestures(
                        onDragStart = {
                            totalDrag = 0f
                        },
                        onDragEnd = {
                            when {
                                totalDrag > 100 -> {
                                    // 从左向右滑动 - 上个月
                                    slideDirection = -1
                                    viewModel.previousMonth()
                                }
                                totalDrag < -100 -> {
                                    // 从右向左滑动 - 下个月
                                    slideDirection = 1
                                    viewModel.nextMonth()
                                }
                            }
                            totalDrag = 0f
                        }
                    ) { change, dragAmount ->
                        totalDrag += dragAmount
                    }
                }
        ) {
            // 月份选择器 - 点击可弹出选择窗口
            MonthSelector(
                monthName = viewModel.getMonthName(),
                onPrevious = {
                    slideDirection = -1
                    viewModel.previousMonth()
                },
                onNext = {
                    slideDirection = 1
                    viewModel.nextMonth()
                },
                onTitleClick = { showMonthYearPicker = true }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 星期标题
            WeekDayHeader()

            // 日历热力图 - 带动画
            AnimatedContent(
                targetState = uiState.selectedMonth,
                transitionSpec = {
                    if (slideDirection > 0) {
                        // 向右滑动，新内容从右边进入
                        slideInHorizontally { width -> width } togetherWith
                                slideOutHorizontally { width -> -width }
                    } else {
                        // 向左滑动，新内容从左边进入
                        slideInHorizontally { width -> -width } togetherWith
                                slideOutHorizontally { width -> width }
                    }.using(SizeTransform(clip = false))
                },
                label = "calendar_animation"
            ) { targetMonth ->
                CalendarHeatmap(
                    selectedMonth = targetMonth,
                    selectedDate = uiState.selectedDate,
                    dailyCalories = uiState.dailyCalories,
                    maxCalories = maxCalories,
                    onDateSelected = { viewModel.selectDate(it) }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))

            // 选中日期的摄入记录
            SelectedDayRecords(
                selectedDate = uiState.selectedDate,
                records = uiState.selectedDayRecords
            )

            // 底部间距
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // 年月选择对话框
    if (showMonthYearPicker) {
        MonthYearPickerDialog(
            currentMonth = uiState.selectedMonth,
            onDismiss = { showMonthYearPicker = false },
            onConfirm = { newMonth ->
                viewModel.setMonth(newMonth)
                showMonthYearPicker = false
            }
        )
    }
}

@Composable
private fun MonthSelector(
    monthName: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onTitleClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "上个月")
        }
        // 点击标题弹出年月选择
        Text(
            text = monthName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable(onClick = onTitleClick)
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Default.ChevronRight, contentDescription = "下个月")
        }
    }
}

@Composable
private fun WeekDayHeader() {
    val weekDays = listOf("一", "二", "三", "四", "五", "六", "日")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        weekDays.forEach { day ->
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun CalendarHeatmap(
    selectedMonth: Long,
    selectedDate: Long,
    dailyCalories: Map<Long, Double>,
    maxCalories: Double,
    onDateSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = selectedMonth
    calendar.set(Calendar.DAY_OF_MONTH, 1)

    // 获取当月第一天是星期几（0=周一, 6=周日）
    var firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 2
    if (firstDayOfWeek < 0) firstDayOfWeek += 7

    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        var currentDay = 1

        // 计算需要多少行
        val totalCells = firstDayOfWeek + daysInMonth
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col

                    if (cellIndex < firstDayOfWeek || currentDay > daysInMonth) {
                        // 空白格子
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                        )
                    } else {
                        // 日期格子
                        calendar.set(Calendar.DAY_OF_MONTH, currentDay)
                        val date = calendar.timeInMillis
                        val calories = dailyCalories[DateTimeUtils.getStartOfDay(date)] ?: 0.0
                        val isSelected = DateTimeUtils.getStartOfDay(date) == DateTimeUtils.getStartOfDay(selectedDate)

                        // 计算热力图颜色（使用主题色深浅）
                        val heatColor = when {
                            calories <= 0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            else -> {
                                val ratio = (calories / maxCalories).coerceIn(0.0, 1.0)
                                MaterialTheme.colorScheme.primary.copy(alpha = (0.3f + ratio * 0.7f).toFloat())
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else heatColor
                                )
                                .clickable { onDateSelected(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = currentDay.toString(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else if (calories > 0) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                // 显示当日热量
                                if (calories > 0) {
                                    Text(
                                        text = "${calories.toInt()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                        else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                        currentDay++
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedDayRecords(
    selectedDate: Long,
    records: List<IntakeRecordEntity>
) {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = selectedDate
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val weekDay = when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.SUNDAY -> "周日"
        Calendar.MONDAY -> "周一"
        Calendar.TUESDAY -> "周二"
        Calendar.WEDNESDAY -> "周三"
        Calendar.THURSDAY -> "周四"
        Calendar.FRIDAY -> "周五"
        Calendar.SATURDAY -> "周六"
        else -> ""
    }

    val dateStr = "${year}年${month}月${day}日 $weekDay"
    val totalCalories = records.sumOf { it.calories }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // 日期和总热量行
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dateStr,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "总摄入 ${String.format("%.0f", totalCalories)} kcal",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        if (records.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "当日无摄入记录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // 按餐次分组显示（与首页一致）
            val mealTypes = listOf(0, 1, 2, 3) // 早餐、午餐、晚餐、加餐
            val mealCardColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)

            mealTypes.forEach { mealType ->
                val recordsForMeal = records.filter { it.mealType == mealType }
                if (recordsForMeal.isNotEmpty()) {
                    // 餐次分组卡片
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
                                IntakeRecordItem(record = record)
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
}

@Composable
private fun IntakeRecordItem(record: IntakeRecordEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 食物图标（与首页一致）
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = getFoodEmoji(record),
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

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

/**
 * 年月选择对话框 - 简洁版
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthYearPickerDialog(
    currentMonth: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = currentMonth

    var selectedYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH) + 1) }

    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = (currentYear - 5..currentYear + 5).toList()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择年月") },
        text = {
            Column {
                // 年份下拉选择
                var yearExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = yearExpanded,
                    onExpandedChange = { yearExpanded = it }
                ) {
                    OutlinedTextField(
                        value = "${selectedYear}年",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("年份") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = yearExpanded,
                        onDismissRequest = { yearExpanded = false }
                    ) {
                        years.forEach { year ->
                            DropdownMenuItem(
                                text = { Text("${year}年") },
                                onClick = {
                                    selectedYear = year
                                    yearExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 月份下拉选择
                var monthExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = monthExpanded,
                    onExpandedChange = { monthExpanded = it }
                ) {
                    OutlinedTextField(
                        value = "${selectedMonth}月",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("月份") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = monthExpanded,
                        onDismissRequest = { monthExpanded = false }
                    ) {
                        (1..12).forEach { month ->
                            DropdownMenuItem(
                                text = { Text("${month}月") },
                                onClick = {
                                    selectedMonth = month
                                    monthExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    calendar.set(selectedYear, selectedMonth - 1, 1)
                    onConfirm(calendar.timeInMillis)
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

private fun getMealTypeName(mealType: Int): String {
    return when (mealType) {
        0 -> "早餐"
        1 -> "午餐"
        2 -> "晚餐"
        3 -> "加餐"
        else -> "其他"
    }
}

/**
 * 获取摄入记录的食物图标（与首页一致）
 */
private fun getFoodEmoji(record: IntakeRecordEntity): String {
    // 如果记录中有存储的图标且是emoji格式，直接使用
    if (!record.foodIcon.isNullOrEmpty() && isEmoji(record.foodIcon)) {
        return record.foodIcon
    }
    // 否则根据名称推断
    return getFoodEmojiByName(record.foodName)
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

/**
 * 根据食物名称返回对应的emoji
 */
private fun getFoodEmojiByName(name: String): String {
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
        name.contains("饮料") || name.contains("可乐") || name.contains("汽水") -> "🥤"
        name.contains("茶") -> "🍵"
        name.contains("咖啡") -> "☕"
        name.contains("酒") -> "🍺"
        name.contains("蛋糕") -> "🎂"
        name.contains("饼干") -> "🍪"
        name.contains("巧克力") -> "🍫"
        name.contains("糖") -> "🍬"
        name.contains("坚果") || name.contains("花生") || name.contains("核桃") -> "🥜"
        name.contains("油") -> "🫒"
        name.contains("汤") -> "🥣"
        name.contains("沙拉") -> "🥗"
        name.contains("薯条") -> "🍟"
        name.contains("汉堡") -> "🍔"
        name.contains("披萨") -> "🍕"
        name.contains("三明治") -> "🥪"
        name.contains("饺子") -> "🥟"
        name.contains("寿司") -> "🍣"
        name.contains("rice", ignoreCase = true) -> "🍚"
        name.contains("bread", ignoreCase = true) -> "🍞"
        name.contains("egg", ignoreCase = true) -> "🥚"
        name.contains("milk", ignoreCase = true) -> "🥛"
        name.contains("chicken", ignoreCase = true) -> "🍗"
        name.contains("beef", ignoreCase = true) -> "🥩"
        name.contains("fish", ignoreCase = true) -> "🐟"
        name.contains("apple", ignoreCase = true) -> "🍎"
        name.contains("banana", ignoreCase = true) -> "🍌"
        name.contains("orange", ignoreCase = true) -> "🍊"
        else -> "🍽️"
    }
}