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
    val amount: Double,               // 总克数/毫升
    val unit: String,                 // 单位名称
    val amountInUnit: Double = 0.0,   // 按单位计的数量
    val gramsPerUnit: Double = 1.0,   // 每单位对应多少克
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
    fun addPendingItem(food: FoodEntity, amountInUnit: Double, unit: String, gramsPerUnit: Double) {
        // 计算实际重量
        val grams = amountInUnit * gramsPerUnit
        
        // 计算营养值
        val calories = (grams / 100.0) * food.calories
        val carbs = (grams / 100.0) * food.carbohydrates
        val protein = (grams / 100.0) * food.protein
        val fat = (grams / 100.0) * food.fat

        val item = PendingFoodItem(
            food = food,
            amount = grams,
            unit = unit,
            amountInUnit = amountInUnit,
            gramsPerUnit = gramsPerUnit,
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
     * 获取食物的正确图标
     * 直接使用食物库中的图标，如果为空则根据名称推断
     */
    private fun getFoodIcon(food: FoodEntity): String {
        // 如果食物有图标且不是空字符串或"custom"，直接使用
        if (food.icon.isNotEmpty() && food.icon != "custom") {
            return food.icon
        }
        // 否则根据名称推断
        return FoodEmojiUtils.getDefaultEmojiForFood(food.name)
    }

    /**
     * 批量保存所有记录
     */
    fun saveAllRecords(dateMillis: Long, mealType: Int) {
        viewModelScope.launch {
            _isSaving.value = true

            val date = DateTimeUtils.getStartOfDay(dateMillis)
            val currentTime = System.currentTimeMillis()

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
                    amountInUnit = item.amountInUnit,
                    gramsPerUnit = item.gramsPerUnit,
                    note = null,
                    foodId = item.food.id,
                    createdAt = currentTime
                )
            }

            intakeRecordRepository.insertRecords(records)
            
            // 更新食物的最近使用时间
            _pendingItems.value.forEach { item ->
                foodRepository.updateLastUsedTime(item.food.id, currentTime)
            }

            _isSaving.value = false
            _saveCompleted.value = true
        }
    }

    /**
     * 保存单条自定义食物记录
     */
    fun saveRecord(
        foodName: String,
        amount: Double,
        caloriesPer100g: Double,
        carbsPer100g: Double,
        proteinPer100g: Double,
        fatPer100g: Double,
        mealType: Int,
        unit: String? = null,
        amountInUnit: Double? = null,
        gramsPerUnit: Double? = null,
        note: String? = null,
        saveAsCustomFood: Boolean = false,
        icon: String? = null
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            val currentTime = System.currentTimeMillis()
            val date = DateTimeUtils.getStartOfDay(currentTime)

            // 计算营养值 (基于总克数)
            val ratio = amount / 100.0
            val record = IntakeRecordEntity(
                foodName = foodName,
                foodIcon = icon ?: FoodEmojiUtils.getDefaultEmojiForFood(foodName),
                date = date,
                amount = amount,
                calories = caloriesPer100g * ratio,
                carbohydrates = carbsPer100g * ratio,
                protein = proteinPer100g * ratio,
                fat = fatPer100g * ratio,
                mealType = mealType,
                caloriesPer100g = caloriesPer100g,
                carbsPer100g = carbsPer100g,
                proteinPer100g = proteinPer100g,
                fatPer100g = fatPer100g,
                unit = unit ?: "克",
                // 修复逻辑：如果提供了单位但没提供份数，且有总克数，则默认为 1 份，每份重量等于总克数
                amountInUnit = amountInUnit ?: (if (unit != null && unit != "克" && unit != "毫升") 1.0 else amount),
                gramsPerUnit = gramsPerUnit ?: (if (unit != null && unit != "克" && unit != "毫升") amount else 1.0),
                note = note,
                createdAt = currentTime
            )

            intakeRecordRepository.insertRecords(listOf(record))

            if (saveAsCustomFood) {
                val food = FoodEntity(
                    name = foodName,
                    category = FoodEmojiUtils.inferCategoryByName(foodName),
                    calories = caloriesPer100g,
                    carbohydrates = carbsPer100g,
                    protein = proteinPer100g,
                    fat = fatPer100g,
                    icon = icon ?: "🍴",
                    unit = unit ?: "克",
                    gramsPerUnit = gramsPerUnit ?: 1.0,
                    isCustom = true,
                    lastUsedTime = currentTime
                )
                foodRepository.insertFood(food)
            }

            _isSaving.value = false
            _saveCompleted.value = true
        }
    }

    fun resetSaveStatus() {
        _saveCompleted.value = false
    }
}
