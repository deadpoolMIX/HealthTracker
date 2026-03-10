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
    val selectedPeriod: Int = 0, // 0=周, 1=月
    val periodOffset: Int = 0, // 0=本周/本月, 1=上周/上月, 2=上上周等
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
        _uiState.value = _uiState.value.copy(selectedPeriod = period, periodOffset = 0)
        loadData()
    }

    fun setPeriodOffset(offset: Int) {
        _uiState.value = _uiState.value.copy(periodOffset = offset.coerceAtLeast(0))
        loadData()
    }

    fun getPeriodLabel(): String {
        val offset = _uiState.value.periodOffset
        return if (_uiState.value.selectedPeriod == 0) {
            when (offset) {
                0 -> "本周"
                1 -> "上周"
                else -> "${offset}周前"
            }
        } else {
            when (offset) {
                0 -> "本月"
                1 -> "上月"
                else -> "${offset}个月前"
            }
        }
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
                intakeData = dailyNutrition.sortedBy { it.date },
                bodyData = bodyRecords.sortedByDescending { it.date },
                sleepData = sleepRecords.sortedByDescending { it.date },
                isLoading = false
            )
        }
    }

    private fun getDateRange(): Pair<Long, Long> {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        val offset = _uiState.value.periodOffset

        return when (_uiState.value.selectedPeriod) {
            0 -> { // 周 - 根据偏移量获取
                calendar.timeInMillis = now
                calendar.add(Calendar.WEEK_OF_YEAR, -offset)
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_MONTH, 6)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                val end = calendar.timeInMillis
                Pair(start, end)
            }
            1 -> { // 月 - 根据偏移量获取
                calendar.timeInMillis = now
                calendar.add(Calendar.MONTH, -offset)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                val end = calendar.timeInMillis
                Pair(start, end)
            }
            else -> {
                Pair(DateTimeUtils.getStartOfDay(now), DateTimeUtils.getEndOfDay(now))
            }
        }
    }

    private fun aggregateNutritionByDay(records: List<IntakeRecordEntity>): List<DailyNutrition> {
        val result = mutableListOf<DailyNutrition>()
        val calendar = Calendar.getInstance()
        val (startDate, _) = getDateRange()

        // 根据选择的周期生成固定天数的数据
        val days = if (_uiState.value.selectedPeriod == 0) 7 else 30

        calendar.timeInMillis = startDate
        for (i in 0 until days) {
            val dayStart = calendar.timeInMillis
            val dayEnd = DateTimeUtils.getEndOfDay(dayStart)

            val dayRecords = records.filter { it.date in dayStart..dayEnd }

            result.add(DailyNutrition(
                date = dayStart,
                calories = dayRecords.sumOf { it.calories },
                carbs = dayRecords.sumOf { it.carbohydrates },
                protein = dayRecords.sumOf { it.protein },
                fat = dayRecords.sumOf { it.fat }
            ))

            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return result
    }

    // 获取平均睡眠时长
    fun getAverageSleepDuration(): Long {
        if (_uiState.value.sleepData.isEmpty()) return 0
        return _uiState.value.sleepData.map { it.duration }.average().toLong()
    }

    // 获取平均入睡时间（处理跨午夜情况）
    fun getAverageSleepTime(): String {
        if (_uiState.value.sleepData.isEmpty()) return "--:--"

        // 入睡时间通常在 18:00 - 次日 06:00 之间
        // 对于凌晨入睡（0-12点）的时间，加 24 小时处理
        val avgMinutes = _uiState.value.sleepData.map {
            val cal = Calendar.getInstance()
            cal.timeInMillis = it.sleepTime
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val minute = cal.get(Calendar.MINUTE)
            // 如果是凌晨（0-12点），视为 24+ 小时
            if (hour < 12) {
                (hour + 24) * 60 + minute
            } else {
                hour * 60 + minute
            }
        }.average().toInt()

        // 转换回正常时间
        val actualMinutes = avgMinutes % (24 * 60)
        return String.format("%02d:%02d", actualMinutes / 60, actualMinutes % 60)
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