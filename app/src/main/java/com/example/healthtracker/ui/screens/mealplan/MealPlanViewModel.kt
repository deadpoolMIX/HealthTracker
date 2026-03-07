package com.example.healthtracker.ui.screens.mealplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.data.local.entity.MealPlanEntity
import com.example.healthtracker.data.local.entity.MealPlanItemEntity
import com.example.healthtracker.data.repository.MealPlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

// 排序类型
enum class SortType {
    NAME_ASC,       // 名称拼音升序
    NAME_DESC,      // 名称拼音降序
    TIME_ASC,       // 添加时间升序
    TIME_DESC,      // 添加时间降序
    MANUAL          // 手动排序
}

data class MealPlanUiState(
    val selectedPlanId: Long? = null,
    val sortType: SortType = SortType.TIME_DESC,
    val showSortDialog: Boolean = false
)

@HiltViewModel
class MealPlanViewModel @Inject constructor(
    private val mealPlanRepository: MealPlanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealPlanUiState())
    val uiState = _uiState.asStateFlow()

    // 原始计划列表
    private val rawPlans = mealPlanRepository.getAllPlans()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // 排序后的计划列表
    val allPlans: StateFlow<List<MealPlanEntity>> = combine(
        rawPlans,
        _uiState.map { it.sortType }
    ) { plans, sortType ->
        sortPlans(plans, sortType)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

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

    private fun sortPlans(plans: List<MealPlanEntity>, sortType: SortType): List<MealPlanEntity> {
        return when (sortType) {
            SortType.NAME_ASC -> plans.sortedBy { getPinYinFirstLetter(it.name) }
            SortType.NAME_DESC -> plans.sortedByDescending { getPinYinFirstLetter(it.name) }
            SortType.TIME_ASC -> plans.sortedBy { it.createdAt }
            SortType.TIME_DESC -> plans.sortedByDescending { it.createdAt }
            SortType.MANUAL -> plans // 手动排序保持原顺序
        }
    }

    /**
     * 获取字符串的拼音首字母
     */
    private fun getPinYinFirstLetter(text: String): String {
        if (text.isEmpty()) return "#"
        val firstChar = text[0]

        // 如果是英文字母，直接返回大写
        if (firstChar in 'a'..'z') return firstChar.uppercaseChar().toString()
        if (firstChar in 'A'..'Z') return firstChar.toString()

        // 中文字符转拼音首字母
        return when (firstChar.code) {
            in 0x3400..0x4DBF, in 0x4E00..0x9FAF -> {
                // 常用汉字拼音首字母区间
                val pinyinCode = firstChar.code - 0x4E00
                when {
                    pinyinCode in 0..152 -> "A"
                    pinyinCode in 153..246 -> "B"
                    pinyinCode in 247..370 -> "C"
                    pinyinCode in 371..498 -> "D"
                    pinyinCode in 499..579 -> "E"
                    pinyinCode in 580..696 -> "F"
                    pinyinCode in 697..861 -> "G"
                    pinyinCode in 862..995 -> "H"
                    pinyinCode in 996..1099 -> "J"
                    pinyinCode in 1100..1263 -> "K"
                    pinyinCode in 1264..1461 -> "L"
                    pinyinCode in 1462..1623 -> "M"
                    pinyinCode in 1624..1719 -> "N"
                    pinyinCode in 1720..1868 -> "O"
                    pinyinCode in 1869..1972 -> "P"
                    pinyinCode in 1973..2067 -> "Q"
                    pinyinCode in 2068..2253 -> "R"
                    pinyinCode in 2254..2319 -> "S"
                    pinyinCode in 2320..2445 -> "T"
                    pinyinCode in 2446..2539 -> "W"
                    pinyinCode in 2540..2669 -> "X"
                    pinyinCode in 2670..2754 -> "Y"
                    pinyinCode in 2755..2906 -> "Z"
                    else -> "#"
                }
            }
            else -> "#"
        }
    }

    fun selectPlan(planId: Long?) {
        _uiState.value = _uiState.value.copy(selectedPlanId = planId)
    }

    fun setSortType(sortType: SortType) {
        _uiState.value = _uiState.value.copy(
            sortType = sortType,
            showSortDialog = false
        )
    }

    fun showSortDialog() {
        _uiState.value = _uiState.value.copy(showSortDialog = true)
    }

    fun hideSortDialog() {
        _uiState.value = _uiState.value.copy(showSortDialog = false)
    }

    fun createPlan(name: String, planType: Int, items: List<MealPlanItemEntity>): Long {
        var planId = 0L
        viewModelScope.launch {
            val plan = MealPlanEntity(
                name = name,
                planType = planType
            )
            planId = mealPlanRepository.insertPlan(plan)

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

    fun getSortTypeName(sortType: SortType): String {
        return when (sortType) {
            SortType.NAME_ASC -> "名称 A-Z"
            SortType.NAME_DESC -> "名称 Z-A"
            SortType.TIME_ASC -> "时间 旧-新"
            SortType.TIME_DESC -> "时间 新-旧"
            SortType.MANUAL -> "手动排序"
        }
    }
}