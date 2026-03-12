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
    val targetCarbs: Double = 0.0,
    val targetProtein: Double = 0.0,
    val targetFat: Double = 0.0,
    val nutrientMode: Int = 0,
    val carbsRatio: Double = 50.0,
    val proteinRatio: Double = 20.0,
    val fatRatio: Double = 30.0,
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

                // 计算营养素目标
                val (targetCarbs, targetProtein, targetFat) = calculateNutrientTargets(settings, targetCalories)

                _uiState.value = _uiState.value.copy(
                    todayIntake = records,
                    totalCalories = totalCalories,
                    totalCarbs = totalCarbs,
                    totalProtein = totalProtein,
                    totalFat = totalFat,
                    targetCalories = targetCalories,
                    targetCarbs = targetCarbs,
                    targetProtein = targetProtein,
                    targetFat = targetFat,
                    nutrientMode = settings?.nutrientMode ?: 0,
                    carbsRatio = settings?.carbsRatio ?: 50.0,
                    proteinRatio = settings?.proteinRatio ?: 20.0,
                    fatRatio = settings?.fatRatio ?: 30.0,
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

    /**
     * 计算营养素目标
     * 自动模式：根据热量和比例计算
     * 手动模式：使用已存储的目标值
     */
    private fun calculateNutrientTargets(
        settings: UserSettingsEntity?,
        targetCalories: Double
    ): Triple<Double, Double, Double> {
        if (settings == null || targetCalories <= 0) {
            return Triple(0.0, 0.0, 0.0)
        }

        return if (settings.nutrientMode == 0) {
            // 自动计算模式
            val carbs = (targetCalories * (settings.carbsRatio / 100.0)) / 4.0
            val protein = (targetCalories * (settings.proteinRatio / 100.0)) / 4.0
            val fat = (targetCalories * (settings.fatRatio / 100.0)) / 9.0
            Triple(carbs, protein, fat)
        } else {
            // 手动模式
            Triple(
                settings.targetCarbs ?: 0.0,
                settings.targetProtein ?: 0.0,
                settings.targetFat ?: 0.0
            )
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
            // 重新计算营养素目标
            val settings = userSettingsRepository.getSettings()
            val (targetCarbs, targetProtein, targetFat) = calculateNutrientTargets(settings, calories)
            // 更新 UI 状态
            val caloriePercentage = HealthCalculator.calculateCaloriePercentage(
                _uiState.value.totalCalories, calories
            )
            _uiState.value = _uiState.value.copy(
                targetCalories = calories,
                targetCarbs = targetCarbs,
                targetProtein = targetProtein,
                targetFat = targetFat,
                caloriePercentage = caloriePercentage
            )
        }
    }

    /**
     * 更新营养素目标设置
     */
    fun updateNutrientSettings(
        nutrientMode: Int,
        carbsRatio: Double,
        proteinRatio: Double,
        fatRatio: Double,
        targetCarbs: Double?,
        targetProtein: Double?,
        targetFat: Double?
    ) {
        viewModelScope.launch {
            userSettingsRepository.updateNutrientSettings(
                nutrientMode, carbsRatio, proteinRatio, fatRatio,
                targetCarbs, targetProtein, targetFat
            )
            // 更新 UI 状态
            _uiState.value = _uiState.value.copy(
                nutrientMode = nutrientMode,
                carbsRatio = carbsRatio,
                proteinRatio = proteinRatio,
                fatRatio = fatRatio,
                targetCarbs = targetCarbs ?: 0.0,
                targetProtein = targetProtein ?: 0.0,
                targetFat = targetFat ?: 0.0
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