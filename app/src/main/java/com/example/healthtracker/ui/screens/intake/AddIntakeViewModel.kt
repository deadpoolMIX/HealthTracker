package com.example.healthtracker.ui.screens.intake

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.data.local.entity.FoodEntity
import com.example.healthtracker.data.local.entity.IntakeRecordEntity
import com.example.healthtracker.data.repository.FoodRepository
import com.example.healthtracker.data.repository.IntakeRecordRepository
import com.example.healthtracker.util.DateTimeUtils
import com.example.healthtracker.util.FoodEmojiUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 待添加的食物项
 */
data class PendingFoodItem(
    val food: FoodEntity,
    val amount: Double,  // 克数
    val unit: String,
    val calories: Double,
    val carbohydrates: Double,
    val protein: Double,
    val fat: Double
)

@HiltViewModel
class AddIntakeViewModel @Inject constructor(
    private val intakeRecordRepository: IntakeRecordRepository,
    private val foodRepository: FoodRepository
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<FoodEntity>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _pendingItems = MutableStateFlow<List<PendingFoodItem>>(emptyList())
    val pendingItems = _pendingItems.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    private val _saveCompleted = MutableStateFlow(false)
    val saveCompleted: StateFlow<Boolean> = _saveCompleted.asStateFlow()

    init {
        loadAllFoods()
    }

    private fun loadAllFoods() {
        viewModelScope.launch {
            foodRepository.getAllFoods().collectLatest {
                _searchResults.value = it
            }
        }
    }

    fun searchFoods(keyword: String) {
        viewModelScope.launch {
            if (keyword.isBlank()) {
                foodRepository.getAllFoods().collectLatest {
                    _searchResults.value = it
                }
            } else {
                foodRepository.searchFoods(keyword).collectLatest {
                    _searchResults.value = it
                }
            }
        }
    }

    /**
     * 添加待保存的食物项
     */
    fun addPendingItem(food: FoodEntity, amount: Double, unit: String) {
        // 计算营养值
        val calories = (amount / 100.0) * food.calories
        val carbs = (amount / 100.0) * food.carbohydrates
        val protein = (amount / 100.0) * food.protein
        val fat = (amount / 100.0) * food.fat

        val item = PendingFoodItem(
            food = food,
            amount = amount,
            unit = unit,
            calories = calories,
            carbohydrates = carbs,
            protein = protein,
            fat = fat
        )

        _pendingItems.value = _pendingItems.value + item
    }

    /**
     * 移除待保存的食物项
     */
    fun removePendingItem(index: Int) {
        val currentList = _pendingItems.value.toMutableList()
        if (index in currentList.indices) {
            currentList.removeAt(index)
            _pendingItems.value = currentList
        }
    }

    /**
     * 获取食物的正确图标（确保是emoji格式）
     */
    private fun getFoodIcon(food: FoodEntity): String {
        // 如果食物有图标且是emoji格式，直接使用
        if (food.icon.isNotEmpty() && isEmoji(food.icon)) {
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

    /**
     * 批量保存所有记录
     */
    fun saveAllRecords(dateMillis: Long, mealType: Int) {
        viewModelScope.launch {
            _isSaving.value = true

            val date = DateTimeUtils.getStartOfDay(dateMillis)

            val records = _pendingItems.value.map { item ->
                // 获取正确的食物图标
                val foodIcon = getFoodIcon(item.food)

                IntakeRecordEntity(
                    foodName = item.food.name,
                    foodIcon = foodIcon,
                    date = date,
                    amount = item.amount,
                    calories = item.calories,
                    carbohydrates = item.carbohydrates,
                    protein = item.protein,
                    fat = item.fat,
                    mealType = mealType,
                    caloriesPer100g = item.food.calories,
                    carbsPer100g = item.food.carbohydrates,
                    proteinPer100g = item.food.protein,
                    fatPer100g = item.food.fat,
                    unit = item.unit,
                    amountInUnit = null,
                    gramsPerUnit = null,
                    note = null,
                    foodId = item.food.id
                )
            }

            intakeRecordRepository.insertRecords(records)

            _pendingItems.value = emptyList()
            _isSaving.value = false
            _saveCompleted.value = true
        }
    }

    // 兼容旧的方法（自定义食物输入页面使用）
    fun saveRecord(
        foodName: String,
        amount: Double,
        caloriesPer100g: Double,
        carbsPer100g: Double,
        proteinPer100g: Double,
        fatPer100g: Double,
        mealType: Int,
        unit: String?,
        amountInUnit: Double?,
        gramsPerUnit: Double?,
        note: String?,
        saveAsCustomFood: Boolean = false,
        icon: String = "🍽️"
    ) {
        viewModelScope.launch {
            _isSaving.value = true

            val actualCalories = (amount / 100.0) * caloriesPer100g
            val actualCarbs = (amount / 100.0) * carbsPer100g
            val actualProtein = (amount / 100.0) * proteinPer100g
            val actualFat = (amount / 100.0) * fatPer100g

            // 确保图标是emoji格式
            val foodIcon = if (icon.isNotEmpty() && isEmoji(icon)) {
                icon
            } else {
                FoodEmojiUtils.getDefaultEmojiForFood(foodName)
            }

            val record = IntakeRecordEntity(
                foodName = foodName,
                foodIcon = foodIcon,
                date = DateTimeUtils.getStartOfDay(),
                amount = amount,
                calories = actualCalories,
                carbohydrates = actualCarbs,
                protein = actualProtein,
                fat = actualFat,
                mealType = mealType,
                caloriesPer100g = caloriesPer100g,
                carbsPer100g = carbsPer100g,
                proteinPer100g = proteinPer100g,
                fatPer100g = fatPer100g,
                unit = unit,
                amountInUnit = amountInUnit,
                gramsPerUnit = gramsPerUnit,
                note = note
            )

            intakeRecordRepository.insertRecord(record)

            if (saveAsCustomFood) {
                val customFood = FoodEntity(
                    name = foodName,
                    category = "自定义",
                    calories = caloriesPer100g,
                    carbohydrates = carbsPer100g,
                    protein = proteinPer100g,
                    fat = fatPer100g,
                    icon = icon,
                    isCustom = true
                )
                foodRepository.insertFood(customFood)
            }

            _isSaving.value = false
            _saveCompleted.value = true
        }
    }
}