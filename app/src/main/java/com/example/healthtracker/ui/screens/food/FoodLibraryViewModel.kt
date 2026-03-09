package com.example.healthtracker.ui.screens.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.data.local.entity.FoodEntity
import com.example.healthtracker.data.repository.FoodRepository
import com.example.healthtracker.data.repository.IntakeRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FoodLibraryUiState(
    val selectedTabIndex: Int = 0
)

@HiltViewModel
class FoodLibraryViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    private val intakeRecordRepository: IntakeRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodLibraryUiState())
    val uiState = _uiState.asStateFlow()

    // 所有食物
    private val allFoods = foodRepository.getAllFoods()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // 最近摄入的食物 - 按最近被记录的顺序排序的所有食物
    val recentFoods = combine(
        allFoods,
        foodRepository.getAllFoods()
    ) { foods, _ ->
        // 获取每个食物的最近记录时间
        val lastRecordTimes = intakeRecordRepository.getFoodLastRecordTimes()
            .associate { it.foodName to it.lastRecordTime }

        // 按最近记录时间排序
        foods.sortedWith(compareByDescending<FoodEntity> { food ->
            lastRecordTimes[food.name] ?: 0L
        }.thenBy { it.name })
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // 自定义食物
    val customFoods = foodRepository.getCustomFoods()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setSelectedTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTabIndex = index)
    }

    fun addCustomFood(
        name: String,
        category: String,
        calories: Double,
        carbs: Double,
        protein: Double,
        fat: Double
    ) {
        viewModelScope.launch {
            val food = FoodEntity(
                name = name,
                category = category,
                calories = calories,
                carbohydrates = carbs,
                protein = protein,
                fat = fat,
                icon = getCategoryIcon(category),
                isCustom = true
            )
            foodRepository.insertFood(food)
        }
    }

    fun deleteCustomFood(food: FoodEntity) {
        viewModelScope.launch {
            foodRepository.deleteCustomFood(food.id)
        }
    }

    private fun getCategoryIcon(category: String): String {
        return when (category) {
            "主食" -> "rice"
            "肉类" -> "meat"
            "蔬菜" -> "vegetable"
            "水果" -> "fruit"
            "蛋奶" -> "egg"
            "豆类" -> "bean"
            "坚果" -> "nut"
            "海鲜" -> "seafood"
            "油脂" -> "oil"
            else -> "food"
        }
    }
}