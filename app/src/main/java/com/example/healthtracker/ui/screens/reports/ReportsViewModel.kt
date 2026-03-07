package com.example.healthtracker.ui.screens.reports

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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class ReportsUiState(
    val selectedPeriod: Int = 0, // 0=天, 1=周, 2=月, 3=年
    val intakeData: List<DailyNutrition> = emptyList(),
    val bodyData: List<BodyRecordEntity> = emptyList(),
    val sleepData: List<SleepRecordEntity> = emptyList(),
    val isLoading: Boolean = true,
    // 报表设置
    val showNutritionChart: Boolean = true,
    val showBodyChart: Boolean = true,
    val showSleepChart: Boolean = true,
    val defaultChartPeriod: Int = 0,
    val showSettingsDialog: Boolean = false
)

// 每日营养汇总
data class DailyNutrition(
    val date: Long,
    val calories: Double,
    val carbs: Double,
    val protein: Double,
    val fat: Double
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val intakeRecordRepository: IntakeRecordRepository,
    private val bodyRecordRepository: BodyRecordRepository,
    private val sleepRecordRepository: SleepRecordRepository,
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadSettingsAndData()
    }

    private fun loadSettingsAndData() {
        viewModelScope.launch {
            // 加载设置
            val settings = userSettingsRepository.getSettings()
            if (settings != null) {
                _uiState.value = _uiState.value.copy(
                    showNutritionChart = settings.showNutritionChart,
                    showBodyChart = settings.showBodyChart,
                    showSleepChart = settings.showSleepChart,
                    defaultChartPeriod = settings.defaultChartPeriod,
                    selectedPeriod = settings.defaultChartPeriod
                )
            }
            loadData()
        }
    }

    fun setPeriod(period: Int) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        loadData()
    }

    fun showSettingsDialog() {
        _uiState.value = _uiState.value.copy(showSettingsDialog = true)
    }

    fun hideSettingsDialog() {
        _uiState.value = _uiState.value.copy(showSettingsDialog = false)
    }

    fun updateReportSettings(
        showNutritionChart: Boolean,
        showBodyChart: Boolean,
        showSleepChart: Boolean,
        defaultChartPeriod: Int
    ) {
        viewModelScope.launch {
            userSettingsRepository.updateReportSettings(
                showNutritionChart,
                showBodyChart,
                showSleepChart,
                defaultChartPeriod
            )
            _uiState.value = _uiState.value.copy(
                showNutritionChart = showNutritionChart,
                showBodyChart = showBodyChart,
                showSleepChart = showSleepChart,
                defaultChartPeriod = defaultChartPeriod,
                selectedPeriod = defaultChartPeriod,
                showSettingsDialog = false
            )
            loadData()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val (startDate, endDate) = getDateRange()

            // 获取摄入数据
            val intakeRecords = intakeRecordRepository.getRecordsBetweenSync(startDate, endDate)
            val dailyNutrition = aggregateNutritionByDay(intakeRecords)

            // 获取身体数据
            val bodyRecords = bodyRecordRepository.getRecordsBetweenSync(startDate, endDate)

            // 获取睡眠数据
            val sleepRecords = sleepRecordRepository.getRecordsBetweenSync(startDate, endDate)

            _uiState.value = _uiState.value.copy(
                intakeData = dailyNutrition.sortedByDescending { it.date },
                bodyData = bodyRecords.sortedByDescending { it.date },
                sleepData = sleepRecords.sortedByDescending { it.date },
                isLoading = false
            )
        }
    }

    private fun getDateRange(): Pair<Long, Long> {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        return when (_uiState.value.selectedPeriod) {
            0 -> { // 天 - 最近7天
                val end = DateTimeUtils.getEndOfDay(now)
                val start = DateTimeUtils.getStartOfDay(DateTimeUtils.getNDaysAgo(6, now))
                Pair(start, end)
            }
            1 -> { // 周 - 最近4周
                val end = DateTimeUtils.getEndOfDay(now)
                calendar.timeInMillis = now
                calendar.add(Calendar.WEEK_OF_YEAR, -3)
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                Pair(calendar.timeInMillis, end)
            }
            2 -> { // 月 - 最近6个月
                val end = DateTimeUtils.getEndOfDay(now)
                calendar.timeInMillis = now
                calendar.add(Calendar.MONTH, -5)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                Pair(calendar.timeInMillis, end)
            }
            3 -> { // 年 - 最近2年
                val end = DateTimeUtils.getEndOfDay(now)
                val start = DateTimeUtils.getStartOfYear(DateTimeUtils.getNYearsAgo(1, now))
                Pair(start, end)
            }
            else -> {
                Pair(DateTimeUtils.getStartOfDay(now), DateTimeUtils.getEndOfDay(now))
            }
        }
    }

    private fun aggregateNutritionByDay(records: List<IntakeRecordEntity>): List<DailyNutrition> {
        return records
            .groupBy { DateTimeUtils.getStartOfDay(it.date) }
            .map { (date, dayRecords) ->
                DailyNutrition(
                    date = date,
                    calories = dayRecords.sumOf { it.calories },
                    carbs = dayRecords.sumOf { it.carbohydrates },
                    protein = dayRecords.sumOf { it.protein },
                    fat = dayRecords.sumOf { it.fat }
                )
            }
    }

    // 获取平均睡眠时长
    fun getAverageSleepDuration(): Long {
        if (_uiState.value.sleepData.isEmpty()) return 0
        return _uiState.value.sleepData.map { it.duration }.average().toLong()
    }

    // 获取平均入睡时间
    fun getAverageSleepTime(): String {
        if (_uiState.value.sleepData.isEmpty()) return "--:--"
        val avgMinutes = _uiState.value.sleepData.map {
            val cal = Calendar.getInstance()
            cal.timeInMillis = it.sleepTime
            cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        }.average().toInt()
        return String.format("%02d:%02d", avgMinutes / 60, avgMinutes % 60)
    }

    // 获取平均起床时间
    fun getAverageWakeTime(): String {
        if (_uiState.value.sleepData.isEmpty()) return "--:--"
        val avgMinutes = _uiState.value.sleepData.map {
            val cal = Calendar.getInstance()
            cal.timeInMillis = it.wakeTime
            cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        }.average().toInt()
        return String.format("%02d:%02d", avgMinutes / 60, avgMinutes % 60)
    }
}