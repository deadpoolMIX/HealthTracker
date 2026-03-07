package com.example.healthtracker.ui.screens.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.data.local.entity.FoodEntity
import com.example.healthtracker.data.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditFoodUiState(
    val food: FoodEntity? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class EditFoodViewModel @Inject constructor(
    private val foodRepository: FoodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditFoodUiState())
    val uiState = _uiState.asStateFlow()

    fun loadFood(foodId: Long) {
        viewModelScope.launch {
            val food = foodRepository.getFoodById(foodId)
            _uiState.value = EditFoodUiState(
                food = food,
                isLoading = false
            )
        }
    }

    suspend fun updateFood(
        name: String,
        calories: Double,
        carbs: Double,
        protein: Double,
        fat: Double,
        category: String,
        icon: String = "🍽️",
        unit: String? = null,
        gramsPerUnit: Double? = null
    ): Boolean {
        val currentFood = _uiState.value.food ?: return false
        if (name.isBlank() || calories <= 0) return false

        val updatedFood = currentFood.copy(
            name = name,
            calories = calories,
            carbohydrates = carbs,
            protein = protein,
            fat = fat,
            category = category,
            icon = icon,
            unit = unit,
            gramsPerUnit = gramsPerUnit
        )
        foodRepository.updateFood(updatedFood)
        return true
    }

    suspend fun deleteFood() {
        val food = _uiState.value.food ?: return
        foodRepository.deleteFood(food)
    }
}