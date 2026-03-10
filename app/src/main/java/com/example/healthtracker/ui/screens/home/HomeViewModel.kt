package com.example.healthtracker.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.data.local.entity.BodyRecordEntity
import com.example.healthtracker.data.local.entity.CycleFoodEntity
import com.example.healthtracker.data.local.entity.IntakeRecordEntity
import com.example.healthtracker.data.local.entity.SleepRecordEntity
import com.example.healthtracker.data.local.entity.UserSettingsEntity
import com.example.healthtracker.data.repository.BodyRecordRepository
import com.example.healthtracker.data.repository.CycleFoodRepository
import com.example.healthtracker.data.repository.IntakeRecordRepository
import com.example.healthtracker.data.repository.SleepRecordRepository
import com.example.healthtracker.data.repository.UserSettingsRepository
import com.example.healthtracker.util.DateTimeUtils
import com.example.healthtracker.util.HealthCalculator
import com.example.healthtracker.util.SelectedDateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val selectedDate: Long = System.currentTimeMillis(),
    val todayIntake: List<IntakeRecordEntity> = emptyList(),
    val todayBodyRecord: BodyRecordEntity? = null,
    val todaySleepRecord: SleepRecordEntity? = null,
    val activeCycleFoods: List<CycleFoodEntity> = emptyList(),
    val totalCalories: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalProtein: Double = 0.0,
    val totalFat: Double = 0.0,
    val targetCalories: Double = 2000.0,
    val bmr: Double = 0.0,
    val tdee: Double = 0.0,
    val caloriePercentage: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val intakeRecordRepository: IntakeRecordRepository,
    private val bodyRecordRepository: BodyRecordRepository,
    private val sleepRecordRepository: SleepRecordRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val cycleFoodRepository: CycleFoodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val userSettings: StateFlow<UserSettingsEntity?> = userSettingsRepository
        .getSettingsFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    init {
        // 观察选中日期的变化
        viewModelScope.launch {
            SelectedDateManager.selectedDate.collect { date ->
                _uiState.value = _uiState.value.copy(selectedDate = date)
                loadDataForDate(date)
            }
        }

        // 加载活跃的周期食物
        viewModelScope.launch {
            cycleFoodRepository.getActiveCycleFoods().collect { cycleFoods ->
                _uiState.value = _uiState.value.copy(activeCycleFoods = cycleFoods)
            }
        }
    }

    private fun loadDataForDate(date: Long) {
        viewModelScope.launch {
            val settings = userSettingsRepository.getSettings()

            // 加载指定日期的摄入记录
            intakeRecordRepository.getRecordsByDate(date).collect { records ->
                val totalCalories = records.sumOf { it.calories }
                val totalCarbs = records.sumOf { it.carbohydrates }
                val totalProtein = records.sumOf { it.protein }
                val totalFat = records.sumOf { it.fat }
                val targetCalories = settings?.targetCalories ?: 2000.0
                val caloriePercentage = HealthCalculator.calculateCaloriePercentage(
                    totalCalories, targetCalories
                )

                _uiState.value = _uiState.value.copy(
                    todayIntake = records,
                    totalCalories = totalCalories,
                    totalCarbs = totalCarbs,
                    totalProtein = totalProtein,
                    totalFat = totalFat,
                    targetCalories = targetCalories,
                    bmr = settings?.bmr ?: 0.0,
                    tdee = settings?.tdee ?: 0.0,
                    caloriePercentage = caloriePercentage,
                    isLoading = false
                )
            }
        }

        viewModelScope.launch {
            // 加载指定日期的身体数据
            bodyRecordRepository.getRecordByDateFlow(date).collect { record ->
                _uiState.value = _uiState.value.copy(todayBodyRecord = record)
            }
        }

        viewModelScope.launch {
            // 加载指定日期的睡眠数据
            sleepRecordRepository.getRecordByDateFlow(date).collect { record ->
                _uiState.value = _uiState.value.copy(todaySleepRecord = record)
            }
        }
    }

    fun refresh() {
        loadDataForDate(SelectedDateManager.getSelectedDate())
    }

    fun deleteRecord(record: IntakeRecordEntity) {
        viewModelScope.launch {
            intakeRecordRepository.deleteRecord(record)
        }
    }

    fun deleteRecordsByIds(ids: List<Long>) {
        viewModelScope.launch {
            intakeRecordRepository.deleteRecordsByIds(ids)
        }
    }

    fun updateRecord(record: IntakeRecordEntity) {
        viewModelScope.launch {
            intakeRecordRepository.updateRecord(record)
        }
    }

    fun updateTargetCalories(calories: Double) {
        viewModelScope.launch {
            userSettingsRepository.updateTargetCalories(calories)
            // 更新 UI 状态
            val caloriePercentage = HealthCalculator.calculateCaloriePercentage(
                _uiState.value.totalCalories, calories
            )
            _uiState.value = _uiState.value.copy(
                targetCalories = calories,
                caloriePercentage = caloriePercentage
            )
        }
    }

    /**
     * 记录周期食物的一份
     */
    fun eatCycleFoodPortion(cycleFood: CycleFoodEntity) {
        viewModelScope.launch {
            val portionCalories = cycleFood.getPortionCalories()
            val portionCarbs = cycleFood.getPortionCarbs()
            val portionProtein = cycleFood.getPortionProtein()
            val portionFat = cycleFood.getPortionFat()

            // 创建摄入记录
            val record = IntakeRecordEntity(
                foodName = cycleFood.name + "（一份）",
                foodIcon = cycleFood.icon,
                date = DateTimeUtils.getStartOfDay(_uiState.value.selectedDate),
                amount = 0.0, // 周期食物不记录具体重量
                calories = portionCalories,
                carbohydrates = portionCarbs,
                protein = portionProtein,
                fat = portionFat,
                mealType = 3, // 默认加餐
                note = "来自周期食物：${cycleFood.name}"
            )
            intakeRecordRepository.insertRecord(record)

            // 更新周期食物的剩余量
            val updatedCycleFood = cycleFood.copy(
                remainingCalories = (cycleFood.remainingCalories - portionCalories).coerceAtLeast(0.0),
                remainingCarbs = (cycleFood.remainingCarbs - portionCarbs).coerceAtLeast(0.0),
                remainingProtein = (cycleFood.remainingProtein - portionProtein).coerceAtLeast(0.0),
                remainingFat = (cycleFood.remainingFat - portionFat).coerceAtLeast(0.0)
            )
            cycleFoodRepository.updateCycleFood(updatedCycleFood)
        }
    }

    /**
     * 吃完周期食物的剩余部分
     */
    fun finishCycleFood(cycleFood: CycleFoodEntity) {
        viewModelScope.launch {
            // 创建摄入记录（使用剩余量）
            val record = IntakeRecordEntity(
                foodName = cycleFood.name + "（剩余）",
                foodIcon = cycleFood.icon,
                date = DateTimeUtils.getStartOfDay(_uiState.value.selectedDate),
                amount = 0.0,
                calories = cycleFood.remainingCalories,
                carbohydrates = cycleFood.remainingCarbs,
                protein = cycleFood.remainingProtein,
                fat = cycleFood.remainingFat,
                mealType = 3,
                note = "来自周期食物：${cycleFood.name}（已吃完）"
            )
            intakeRecordRepository.insertRecord(record)

            // 标记周期食物为已完成
            val finishedCycleFood = cycleFood.copy(
                remainingCalories = 0.0,
                remainingCarbs = 0.0,
                remainingProtein = 0.0,
                remainingFat = 0.0,
                isActive = false
            )
            cycleFoodRepository.updateCycleFood(finishedCycleFood)
        }
    }

    /**
     * 删除周期食物
     */
    fun deleteCycleFood(cycleFood: CycleFoodEntity) {
        viewModelScope.launch {
            cycleFoodRepository.deleteCycleFood(cycleFood)
        }
    }
}