package com.example.healthtracker.ui.screens.intake

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.data.local.entity.FoodEntity
import com.example.healthtracker.data.local.entity.IntakeRecordEntity
import com.example.healthtracker.data.repository.FoodRepository
import com.example.healthtracker.data.repository.IntakeRecordRepository
import com.example.healthtracker.util.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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

    var saveCompleted = false
        private set

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
     * 批量保存所有记录
     */
    fun saveAllRecords(dateMillis: Long, mealType: Int) {
        viewModelScope.launch {
            _isSaving.value = true

            val date = DateTimeUtils.getStartOfDay(dateMillis)

            val records = _pendingItems.value.map { item ->
                IntakeRecordEntity(
                    foodName = item.food.name,
                    foodIcon = item.food.icon.ifEmpty { null },
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
            saveCompleted = true
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

            val record = IntakeRecordEntity(
                foodName = foodName,
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
            saveCompleted = true
        }
    }
}