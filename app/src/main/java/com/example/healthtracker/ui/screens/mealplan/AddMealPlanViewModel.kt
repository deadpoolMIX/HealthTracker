package com.example.healthtracker.ui.screens.mealplan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.data.local.entity.FoodEntity
import com.example.healthtracker.data.local.entity.MealPlanEntity
import com.example.healthtracker.data.local.entity.MealPlanItemEntity
import com.example.healthtracker.data.repository.FoodRepository
import com.example.healthtracker.data.repository.MealPlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddMealPlanUiState(
    val planId: Long? = null,
    val planName: String = "",
    val planType: Int = 1,
    val items: List<MealPlanItemEntity> = emptyList(),
    val currentMealType: Int = 0,
    val currentDayOfWeek: Int? = null,
    val isLoading: Boolean = false,
    val isEditing: Boolean = false
)

@HiltViewModel
class AddMealPlanViewModel @Inject constructor(
    private val mealPlanRepository: MealPlanRepository,
    private val foodRepository: FoodRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val planId: Long? = savedStateHandle.get<String>("planId")?.toLongOrNull()

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

    init {
        if (planId != null && planId > 0) {
            loadPlanForEditing(planId)
        }
    }

    private fun loadPlanForEditing(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isEditing = true, planId = id)

            // 加载计划信息
            val plans = mealPlanRepository.getAllPlansSync()
            val plan = plans.find { it.id == id }

            if (plan != null) {
                // 加载计划项目
                val items = mealPlanRepository.getItemsByPlanIdSync(id)

                _uiState.value = _uiState.value.copy(
                    planName = plan.name,
                    planType = plan.planType,
                    items = items,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

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

        if (state.isEditing && state.planId != null) {
            // 更新现有计划
            val plan = MealPlanEntity(
                id = state.planId,
                name = state.planName,
                planType = state.planType
            )
            mealPlanRepository.updatePlan(plan)

            // 删除旧的项目，插入新的
            mealPlanRepository.deleteItemsByPlanId(state.planId)
            val itemsWithPlanId = state.items.map { it.copy(planId = state.planId) }
            mealPlanRepository.insertItems(itemsWithPlanId)
        } else {
            // 创建新计划
            val plan = MealPlanEntity(
                name = state.planName,
                planType = state.planType
            )
            val planId = mealPlanRepository.insertPlan(plan)

            val itemsWithPlanId = state.items.map { it.copy(planId = planId) }
            mealPlanRepository.insertItems(itemsWithPlanId)
        }

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