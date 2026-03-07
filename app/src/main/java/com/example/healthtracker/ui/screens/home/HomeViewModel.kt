package com.example.healthtracker.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.data.local.entity.BodyRecordEntity
import com.example.healthtracker.data.local.entity.IntakeRecordEntity
import com.example.healthtracker.data.local.entity.SleepRecordEntity
import com.example.healthtracker.data.local.entity.UserSettingsEntity
import com.example.healthtracker.data.repository.BodyRecordRepository
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
    private val userSettingsRepository: UserSettingsRepository
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
}