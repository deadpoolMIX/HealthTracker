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

@HiltViewModel
class AddIntakeViewModel @Inject constructor(
    private val intakeRecordRepository: IntakeRecordRepository,
    private val foodRepository: FoodRepository
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<FoodEntity>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    init {
        // 初始加载所有食物
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
        saveAsCustomFood: Boolean = false
    ) {
        viewModelScope.launch {
            _isSaving.value = true

            // 计算实际营养值
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

            // 如果需要保存为自定义食物
            if (saveAsCustomFood) {
                val customFood = FoodEntity(
                    name = foodName,
                    category = "自定义",
                    calories = caloriesPer100g,
                    carbohydrates = carbsPer100g,
                    protein = proteinPer100g,
                    fat = fatPer100g,
                    icon = "custom",
                    isCustom = true
                )
                foodRepository.insertFood(customFood)
            }

            _isSaving.value = false
        }
    }
}