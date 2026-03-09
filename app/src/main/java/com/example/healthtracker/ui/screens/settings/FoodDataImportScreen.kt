package com.example.healthtracker.ui.screens.settings

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.data.local.entity.FoodEntity
import com.example.healthtracker.data.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class FoodDataImportViewModel @Inject constructor(
    private val foodRepository: FoodRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FoodDataImportState())
    val state: StateFlow<FoodDataImportState> = _state.asStateFlow()

    fun importFromJson(context: Context, uri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, success = null, importResult = null)

            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().use { it.readText() }
                } ?: throw Exception("无法读取文件")

                val parsedFoods = parseJsonFoodData(jsonString)

                if (parsedFoods.isEmpty()) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "文件中没有有效的食物数据"
                    )
                    return@launch
                }

                // 获取数据库中已有的食物名称
                val existingFoods = foodRepository.getAllFoodsOnce()
                val existingNames = existingFoods.map { it.name }.toSet()

                // 分类：成功、重复、失败
                val validFoods = mutableListOf<FoodEntity>()
                val duplicateNames = mutableListOf<String>()

                parsedFoods.forEach { food ->
                    if (existingNames.contains(food.name)) {
                        duplicateNames.add(food.name)
                    } else {
                        validFoods.add(food)
                    }
                }

                // 保存新食物到数据库
                if (validFoods.isNotEmpty()) {
                    foodRepository.insertFoods(validFoods)
                }

                val result = ImportResult(
                    totalCount = parsedFoods.size,
                    successCount = validFoods.size,
                    duplicateCount = duplicateNames.size,
                    failedCount = 0,
                    duplicateNames = duplicateNames
                )

                _state.value = _state.value.copy(
                    isLoading = false,
                    importResult = result
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "导入失败: ${e.message}"
                )
            }
        }
    }

    fun importFromCsv(context: Context, uri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, success = null, importResult = null)

            try {
                val csvString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().use { it.readText() }
                } ?: throw Exception("无法读取文件")

                val parsedResult = parseCsvFoodDataWithErrors(csvString)

                if (parsedResult.validFoods.isEmpty() && parsedResult.invalidCount == 0) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "文件中没有有效的食物数据"
                    )
                    return@launch
                }

                // 获取数据库中已有的食物名称
                val existingFoods = foodRepository.getAllFoodsOnce()
                val existingNames = existingFoods.map { it.name }.toSet()

                // 分类：成功、重复
                val validFoods = mutableListOf<FoodEntity>()
                val duplicateNames = mutableListOf<String>()

                parsedResult.validFoods.forEach { food ->
                    if (existingNames.contains(food.name)) {
                        duplicateNames.add(food.name)
                    } else {
                        validFoods.add(food)
                    }
                }

                // 保存新食物到数据库
                if (validFoods.isNotEmpty()) {
                    foodRepository.insertFoods(validFoods)
                }

                val result = ImportResult(
                    totalCount = parsedResult.totalCount,
                    successCount = validFoods.size,
                    duplicateCount = duplicateNames.size,
                    failedCount = parsedResult.invalidCount,
                    duplicateNames = duplicateNames
                )

                _state.value = _state.value.copy(
                    isLoading = false,
                    importResult = result
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "导入失败: ${e.message}"
                )
            }
        }
    }

    private fun parseJsonFoodData(jsonString: String): List<FoodEntity> {
        val foods = mutableListOf<FoodEntity>()
        val json = JSONObject(jsonString)

        // 支持 "foods" 数组格式
        val foodsArray = if (json.has("foods")) {
            json.getJSONArray("foods")
        } else {
            // 也支持直接是数组格式
            return emptyList()
        }

        for (i in 0 until foodsArray.length()) {
            val item = foodsArray.getJSONObject(i)
            try {
                val food = FoodEntity(
                    name = item.getString("name"),
                    category = item.optString("category", "其他"),
                    calories = item.optDouble("calories", 0.0),
                    carbohydrates = item.optDouble("carbohydrates", 0.0),
                    protein = item.optDouble("protein", 0.0),
                    fat = item.optDouble("fat", 0.0),
                    icon = item.optString("emoji", item.optString("icon", "🍽️"))
                )
                foods.add(food)
            } catch (e: Exception) {
                // 跳过无效条目
            }
        }

        return foods
    }

    private fun parseCsvFoodData(csvString: String): List<FoodEntity> {
        val result = parseCsvFoodDataWithErrors(csvString)
        return result.validFoods
    }

    private data class CsvParseResult(
        val validFoods: List<FoodEntity>,
        val totalCount: Int,
        val invalidCount: Int
    )

    private fun parseCsvFoodDataWithErrors(csvString: String): CsvParseResult {
        val foods = mutableListOf<FoodEntity>()
        val lines = csvString.lines().filter { it.isNotBlank() }

        if (lines.size < 2) return CsvParseResult(emptyList(), 0, 0)

        // 解析表头
        val header = lines[0].split(",").map { it.trim().lowercase() }
        val nameIndex = header.indexOf("name")
        val categoryIndex = header.indexOf("category")
        val caloriesIndex = header.indexOf("calories")
        val carbsIndex = header.indexOf("carbohydrates")
        val proteinIndex = header.indexOf("protein")
        val fatIndex = header.indexOf("fat")
        val emojiIndex = header.indexOf("emoji")
        val iconIndex = header.indexOf("icon")

        if (nameIndex == -1) return CsvParseResult(emptyList(), 0, 0)

        var invalidCount = 0

        // 解析数据行
        for (i in 1 until lines.size) {
            val values = lines[i].split(",").map { it.trim() }
            if (values.size <= nameIndex || values[nameIndex].isBlank()) {
                invalidCount++
                continue
            }

            try {
                val food = FoodEntity(
                    name = values[nameIndex],
                    category = if (categoryIndex >= 0 && categoryIndex < values.size) values[categoryIndex] else "其他",
                    calories = if (caloriesIndex >= 0 && caloriesIndex < values.size) values[caloriesIndex].toDoubleOrNull() ?: 0.0 else 0.0,
                    carbohydrates = if (carbsIndex >= 0 && carbsIndex < values.size) values[carbsIndex].toDoubleOrNull() ?: 0.0 else 0.0,
                    protein = if (proteinIndex >= 0 && proteinIndex < values.size) values[proteinIndex].toDoubleOrNull() ?: 0.0 else 0.0,
                    fat = if (fatIndex >= 0 && fatIndex < values.size) values[fatIndex].toDoubleOrNull() ?: 0.0 else 0.0,
                    icon = if (emojiIndex >= 0 && emojiIndex < values.size && values[emojiIndex].isNotEmpty()) {
                        values[emojiIndex]
                    } else if (iconIndex >= 0 && iconIndex < values.size && values[iconIndex].isNotEmpty()) {
                        values[iconIndex]
                    } else {
                        "🍽️"
                    }
                )
                foods.add(food)
            } catch (e: Exception) {
                invalidCount++
            }
        }

        return CsvParseResult(foods, lines.size - 1, invalidCount)
    }

    fun clearMessage() {
        _state.value = _state.value.copy(error = null, success = null, importResult = null)
    }

    /**
     * 导入预设食物数据（从 assets 文件夹）
     */
    fun importPresetFoods(context: Context) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, success = null, importResult = null)

            try {
                val existingFoods = foodRepository.getAllFoodsOnce()
                val existingNames = existingFoods.map { it.name }.toSet()

                val totalParsed = mutableListOf<FoodEntity>()
                val totalDuplicateNames = mutableListOf<String>()

                // 导入 USDA 预设数据
                val usdaFoods = parseJsonFromAssets(context, "preset_foods_usda.json")
                usdaFoods.forEach { food ->
                    if (existingNames.contains(food.name) || totalParsed.any { it.name == food.name }) {
                        totalDuplicateNames.add(food.name)
                    } else {
                        totalParsed.add(food)
                    }
                }

                // 导入中国食物预设数据
                val chineseFoods = parseJsonFromAssets(context, "preset_foods_chinese.json")
                chineseFoods.forEach { food ->
                    if (existingNames.contains(food.name) || totalParsed.any { it.name == food.name }) {
                        if (!totalDuplicateNames.contains(food.name)) {
                            totalDuplicateNames.add(food.name)
                        }
                    } else {
                        totalParsed.add(food)
                    }
                }

                // 保存到数据库
                if (totalParsed.isNotEmpty()) {
                    foodRepository.insertFoods(totalParsed)
                }

                val result = ImportResult(
                    totalCount = usdaFoods.size + chineseFoods.size,
                    successCount = totalParsed.size,
                    duplicateCount = totalDuplicateNames.size,
                    failedCount = 0,
                    duplicateNames = totalDuplicateNames
                )

                _state.value = _state.value.copy(
                    isLoading = false,
                    importResult = result
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "导入预设数据失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 从 assets 文件夹解析 JSON 食物数据
     */
    private fun parseJsonFromAssets(context: Context, fileName: String): List<FoodEntity> {
        val foods = mutableListOf<FoodEntity>()
        val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        val json = JSONObject(jsonString)

        val foodsArray = if (json.has("foods")) {
            json.getJSONArray("foods")
        } else {
            return emptyList()
        }

        for (i in 0 until foodsArray.length()) {
            val item = foodsArray.getJSONObject(i)
            try {
                val food = FoodEntity(
                    name = item.getString("name"),
                    category = item.optString("category", "其他"),
                    calories = item.optDouble("calories", 0.0),
                    carbohydrates = item.optDouble("carbohydrates", 0.0),
                    protein = item.optDouble("protein", 0.0),
                    fat = item.optDouble("fat", 0.0),
                    icon = item.optString("emoji", item.optString("icon", "🍽️"))
                )
                foods.add(food)
            } catch (e: Exception) {
                // 跳过无效条目
            }
        }

        return foods
    }

    /**
     * 清空所有食物数据
     */
    fun clearAllFoods() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, success = null, importResult = null)

            try {
                foodRepository.deleteAllFoods()
                _state.value = _state.value.copy(
                    isLoading = false,
                    success = "食物数据库已清空"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "清空失败: ${e.message}"
                )
            }
        }
    }
}

data class FoodDataImportState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: String? = null,
    val importResult: ImportResult? = null
)

/**
 * 导入结果统计
 */
data class ImportResult(
    val totalCount: Int,        // 本次数据总数
    val successCount: Int,      // 成功导入数
    val duplicateCount: Int,    // 重复未导入数
    val failedCount: Int,       // 导入失败数（解析错误）
    val duplicateNames: List<String> = emptyList()  // 重复的食物名称列表
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDataImportScreen(
    onNavigateBack: () -> Unit,
    viewModel: FoodDataImportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    // JSON 文件选择器
    val jsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importFromJson(context, it) }
    }

    // CSV 文件选择器
    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importFromCsv(context, it) }
    }

    // 清空确认对话框状态
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("导入食物数据", fontWeight = FontWeight.Medium) },
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
        ) {
            // 说明卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "导入说明",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "支持导入 JSON 或 CSV 格式的食物营养数据文件。\n\n" +
                                "数据要求：\n" +
                                "• 名称(name)：必填\n" +
                                "• 热量(calories)：kcal/100g\n" +
                                "• 碳水(carbohydrates)：g/100g\n" +
                                "• 蛋白质(protein)：g/100g\n" +
                                "• 脂肪(fat)：g/100g\n" +
                                "• 分类(category)：可选\n" +
                                "• 图标(emoji)：可选",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 导入按钮
            Button(
                onClick = {
                    jsonLauncher.launch(arrayOf("application/json", "*/*"))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("导入 JSON 文件")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    csvLauncher.launch(arrayOf("text/csv", "text/plain", "*/*"))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text("导入 CSV 文件")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 预设数据导入卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "预设食物数据",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "包含 97 种 USDA 食物和 101 种中国常见食物",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.importPresetFoods(context) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading
                    ) {
                        Text("一键导入预设数据")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 清空数据库卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "清空食物数据库",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "删除所有食物数据，已记录的饮食记录不受影响",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showClearConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        enabled = !state.isLoading
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("清空食物数据库")
                    }
                }
            }

            // 加载中提示
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    // 导入结果对话框
    state.importResult?.let { result ->
        ImportResultDialog(
            result = result,
            onDismiss = { viewModel.clearMessage() }
        )
    }

    // 错误提示
    state.error?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.clearMessage() },
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("导入失败") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearMessage() }) {
                    Text("确定")
                }
            }
        )
    }

    // 成功提示
    state.success?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.clearMessage() },
            icon = { Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("操作成功") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearMessage() }) {
                    Text("确定")
                }
            }
        )
    }

    // 清空确认对话框
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("确认清空") },
            text = { Text("确定要清空食物数据库吗？\n\n此操作不可撤销，但已记录的饮食记录不受影响。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirmDialog = false
                        viewModel.clearAllFoods()
                    }
                ) {
                    Text("确认清空", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 导入结果对话框
 */
@Composable
private fun ImportResultDialog(
    result: ImportResult,
    onDismiss: () -> Unit
) {
    val hasDuplicates = result.duplicateCount > 0
    val hasFailures = result.failedCount > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = if (result.successCount > 0) Icons.Default.Check else Icons.Default.Warning,
                contentDescription = null,
                tint = if (result.successCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        },
        title = { Text("导入完成") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 统计信息
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        ResultRow("本次数据总数", result.totalCount.toString())
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        ResultRow(
                            label = "成功导入",
                            value = result.successCount.toString(),
                            valueColor = if (result.successCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        ResultRow(
                            label = "重复未导入",
                            value = result.duplicateCount.toString(),
                            valueColor = if (hasDuplicates) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (result.failedCount > 0) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            ResultRow(
                                label = "解析失败",
                                value = result.failedCount.toString(),
                                valueColor = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                // 显示重复的食物名称（最多显示10个）
                if (hasDuplicates && result.duplicateNames.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "重复的食物：",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = result.duplicateNames.take(10).joinToString("、") +
                                if (result.duplicateNames.size > 10) " 等${result.duplicateNames.size}种" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}

@Composable
private fun ResultRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}