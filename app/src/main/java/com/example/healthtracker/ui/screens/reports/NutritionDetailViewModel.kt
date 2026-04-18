package com.example.healthtracker.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.data.local.entity.IntakeRecordEntity
import com.example.healthtracker.data.repository.IntakeRecordRepository
import com.example.healthtracker.data.repository.UserSettingsRepository
import com.example.healthtracker.util.DateTimeUtils
import com.example.healthtracker.util.ReportPeriodManager
import com.example.healthtracker.util.ReportStatUtils.averageByRealDays
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.Calendar

data class NutritionDetailUiState(
    val year: Int,
    val weekNumber: Int,
    val targetCalories: Float = 2000f,
    val dailyData: List<DailyNutritionData> = emptyList(),
    val avgDailyCalories: Float = 0f,
    val avgDailyCarbs: Float = 0f,
    val avgDailyProtein: Float = 0f,
    val avgDailyFat: Float = 0f,
    val avgMealCalories: Float = 0f,
    val avgMealCarbs: Float = 0f,
    val avgMealProtein: Float = 0f,
    val avgMealFat: Float = 0f,
    val isLoading: Boolean = true
)

data class DailyNutritionData(
    val dayIndex: Int, 
    val dateLabel: String, 
    val timestamp: Long, 
    val calories: Float,
    val carbs: Float,
    val protein: Float,
    val fat: Float
)

@HiltViewModel
class NutritionDetailViewModel @Inject constructor(
    private val intakeRecordRepository: IntakeRecordRepository,
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        NutritionDetailUiState(
            year = ReportPeriodManager.getYear(System.currentTimeMillis()),
            weekNumber = ReportPeriodManager.getCustomWeekNumber(System.currentTimeMillis())
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun previousWeek() {
        val currentStart = ReportPeriodManager.getStartTimestampOfWeek(_uiState.value.year, _uiState.value.weekNumber)
        val prevWeekTime = currentStart - 7 * 24 * 60 * 60 * 1000L
        _uiState.value = _uiState.value.copy(
            year = ReportPeriodManager.getYear(prevWeekTime),
            weekNumber = ReportPeriodManager.getCustomWeekNumber(prevWeekTime)
        )
        loadData()
    }

    fun nextWeek() {
        val currentStart = ReportPeriodManager.getStartTimestampOfWeek(_uiState.value.year, _uiState.value.weekNumber)
        val nextWeekTime = currentStart + 7 * 24 * 60 * 60 * 1000L
        
        // Prevent navigating to future weeks
        if (nextWeekTime > System.currentTimeMillis()) return
        
        _uiState.value = _uiState.value.copy(
            year = ReportPeriodManager.getYear(nextWeekTime),
            weekNumber = ReportPeriodManager.getCustomWeekNumber(nextWeekTime)
        )
        loadData()
    }

    fun jumpToWeek(targetWeekNumber: Int) {
        val now = System.currentTimeMillis()
        val currentYear = ReportPeriodManager.getYear(now)
        val maxWeek = ReportPeriodManager.getCustomWeekNumber(now)
        val safeWeek = targetWeekNumber.coerceIn(1, maxWeek)
        
        _uiState.value = _uiState.value.copy(
            year = currentYear,
            weekNumber = safeWeek
        )
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val settings = userSettingsRepository.getSettings()
            val targetCals = settings?.targetCalories?.toFloat() ?: 2000f

            val year = _uiState.value.year
            val weekNumber = _uiState.value.weekNumber

            val startDate = ReportPeriodManager.getStartTimestampOfWeek(year, weekNumber)
            val endDate = ReportPeriodManager.getEndTimestampOfWeek(year, weekNumber)

            val records = intakeRecordRepository.getRecordsBetweenSync(startDate, endDate)

            // Generate daily data
            val dailyData = (0..6).map { dayIndex ->
                val dayStart = ReportPeriodManager.getTimestampForDayOfWeek(year, weekNumber, dayIndex)
                val dayEnd = DateTimeUtils.getEndOfDay(dayStart)
                val dayRecords = records.filter { it.date in dayStart..dayEnd }
                DailyNutritionData(
                    dayIndex = dayIndex,
                    dateLabel = ReportPeriodManager.getFormattedDateForDayOfWeek(year, weekNumber, dayIndex),
                    timestamp = dayStart,
                    calories = dayRecords.sumOf { it.calories }.toFloat(),
                    carbs = dayRecords.sumOf { it.carbohydrates }.toFloat(),
                    protein = dayRecords.sumOf { it.protein }.toFloat(),
                    fat = dayRecords.sumOf { it.fat }.toFloat()
                )
            }

            // Calculate stats using ReportStatUtils
            // 每天均值（基于实际有记录的天数）
            val avgDailyCalories = records.averageByRealDays({ DateTimeUtils.getStartOfDay(it.date) }, { it.calories.toFloat() })
            val avgDailyCarbs = records.averageByRealDays({ DateTimeUtils.getStartOfDay(it.date) }, { it.carbohydrates.toFloat() })
            val avgDailyProtein = records.averageByRealDays({ DateTimeUtils.getStartOfDay(it.date) }, { it.protein.toFloat() })
            val avgDailyFat = records.averageByRealDays({ DateTimeUtils.getStartOfDay(it.date) }, { it.fat.toFloat() })

            // 每餐均值（基于实际有记录的餐次：天 + 餐次）
            val avgMealCalories = records.averageByRealDays({ "${DateTimeUtils.getStartOfDay(it.date)}_${it.mealType}" }, { it.calories.toFloat() })
            val avgMealCarbs = records.averageByRealDays({ "${DateTimeUtils.getStartOfDay(it.date)}_${it.mealType}" }, { it.carbohydrates.toFloat() })
            val avgMealProtein = records.averageByRealDays({ "${DateTimeUtils.getStartOfDay(it.date)}_${it.mealType}" }, { it.protein.toFloat() })
            val avgMealFat = records.averageByRealDays({ "${DateTimeUtils.getStartOfDay(it.date)}_${it.mealType}" }, { it.fat.toFloat() })

            _uiState.value = _uiState.value.copy(
                targetCalories = targetCals,
                dailyData = dailyData,
                avgDailyCalories = avgDailyCalories,
                avgDailyCarbs = avgDailyCarbs,
                avgDailyProtein = avgDailyProtein,
                avgDailyFat = avgDailyFat,
                avgMealCalories = avgMealCalories,
                avgMealCarbs = avgMealCarbs,
                avgMealProtein = avgMealProtein,
                avgMealFat = avgMealFat,
                isLoading = false
            )
        }
    }
}