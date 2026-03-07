package com.example.healthtracker.ui.screens.mealplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.data.local.entity.FoodEntity
import com.example.healthtracker.data.local.entity.MealPlanItemEntity
import com.example.healthtracker.data.repository.FoodRepository
import com.example.healthtracker.data.repository.MealPlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddMealPlanUiState(
    val planName: String = "",
    val planType: Int = 1,
    val items: List<MealPlanItemEntity> = emptyList(),
    val currentMealType: Int = 0,
    val currentDayOfWeek: Int? = null
)

@HiltViewModel
class AddMealPlanViewModel @Inject constructor(
    private val mealPlanRepository: MealPlanRepository,
    private val foodRepository: FoodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMealPlanUiState())
    val uiState = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    @OptIn(FlowPreview::class)
    val searchResults: StateFlow<List<FoodEntity>> = _searchQuery
        .debounce(300)
        .mapLatest { query ->
            if (query.isBlank()) {
                emptyList()
            } else {
                foodRepository.searchFoodsSync(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setPlanName(name: String) {
        _uiState.value = _uiState.value.copy(planName = name)
    }

    fun setPlanType(type: Int) {
        _uiState.value = _uiState.value.copy(planType = type)
    }

    fun setCurrentMealType(mealType: Int) {
        _uiState.value = _uiState.value.copy(currentMealType = mealType)
    }

    fun setCurrentDayOfWeek(day: Int?) {
        _uiState.value = _uiState.value.copy(currentDayOfWeek = day)
    }

    fun searchFoods(query: String) {
        _searchQuery.value = query
    }

    fun addItem(item: MealPlanItemEntity) {
        val currentItems = _uiState.value.items.toMutableList()
        currentItems.add(item)
        _uiState.value = _uiState.value.copy(items = currentItems)
    }

    fun removeItem(index: Int) {
        val currentItems = _uiState.value.items.toMutableList()
        if (index in currentItems.indices) {
            currentItems.removeAt(index)
            _uiState.value = _uiState.value.copy(items = currentItems)
        }
    }

    fun updateItem(index: Int, item: MealPlanItemEntity) {
        val currentItems = _uiState.value.items.toMutableList()
        if (index in currentItems.indices) {
            currentItems[index] = item
            _uiState.value = _uiState.value.copy(items = currentItems)
        }
    }

    suspend fun savePlan(): Boolean {
        val state = _uiState.value
        if (state.planName.isBlank() || state.items.isEmpty()) {
            return false
        }

        val plan = com.example.healthtracker.data.local.entity.MealPlanEntity(
            name = state.planName,
            planType = state.planType
        )
        val planId = mealPlanRepository.insertPlan(plan)

        val itemsWithPlanId = state.items.map { it.copy(planId = planId) }
        mealPlanRepository.insertItems(itemsWithPlanId)

        return true
    }

    fun getMealTypeName(mealType: Int): String {
        return when (mealType) {
            0 -> "早餐"
            1 -> "午餐"
            2 -> "晚餐"
            3 -> "加餐"
            else -> "其他"
        }
    }

    fun getDayName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            0 -> "周一"
            1 -> "周二"
            2 -> "周三"
            3 -> "周四"
            4 -> "周五"
            5 -> "周六"
            6 -> "周日"
            else -> "未知"
        }
    }
}