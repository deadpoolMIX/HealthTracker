package com.example.healthtracker.ui.screens.mealplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.data.local.entity.MealPlanEntity
import com.example.healthtracker.data.local.entity.MealPlanItemEntity
import com.example.healthtracker.data.repository.MealPlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MealPlanUiState(
    val selectedPlanId: Long? = null
)

@HiltViewModel
class MealPlanViewModel @Inject constructor(
    private val mealPlanRepository: MealPlanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealPlanUiState())
    val uiState = _uiState.asStateFlow()

    // 所有饮食计划
    val allPlans = mealPlanRepository.getAllPlans()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // 当前选中计划的详情
    val selectedPlanItems = _uiState
        .flatMapLatest { state ->
            if (state.selectedPlanId != null) {
                mealPlanRepository.getItemsByPlanId(state.selectedPlanId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun selectPlan(planId: Long?) {
        _uiState.value = _uiState.value.copy(selectedPlanId = planId)
    }

    fun createPlan(name: String, planType: Int, items: List<MealPlanItemEntity>): Long {
        var planId = 0L
        viewModelScope.launch {
            val plan = MealPlanEntity(
                name = name,
                planType = planType
            )
            planId = mealPlanRepository.insertPlan(plan)

            // 插入计划项目
            if (items.isNotEmpty()) {
                val itemsWithPlanId = items.map { it.copy(planId = planId) }
                mealPlanRepository.insertItems(itemsWithPlanId)
            }
        }
        return planId
    }

    fun deletePlan(plan: MealPlanEntity) {
        viewModelScope.launch {
            mealPlanRepository.deletePlan(plan)
        }
    }

    fun togglePlanActive(plan: MealPlanEntity) {
        viewModelScope.launch {
            if (plan.isActive) {
                mealPlanRepository.deactivatePlan(plan.id)
            } else {
                mealPlanRepository.activatePlan(plan.id)
            }
        }
    }

    fun getPlanTypeName(planType: Int): String {
        return when (planType) {
            0 -> "单餐"
            1 -> "单日"
            2 -> "周计划"
            3 -> "月计划"
            else -> "未知"
        }
    }
}