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
    val intakeData: List<DailyNutritionData> = emptyList(), // 改为新的每日营养数据结构
    val targetCalories: Float = 2000f,
    val bodyData: List<BodyRecordEntity> = emptyList(),
    val sleepData: List<SleepRecordEntity> = emptyList(),
    val weekDates: List<Long> = emptyList(), // 添加：固定7天的日期列表
    val isLoading: Boolean = true,
    // 报表设置
    val showNutritionChart: Boolean = true,
    val showBodyChart: Boolean = true,
    val showSleepChart: Boolean = true,
    val defaultChartPeriod: Int = 0,
    val showSettingsDialog: Boolean = false
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
                    selectedPeriod = settings.defaultChartPeriod,
                    targetCalories = settings.targetCalories?.toFloat() ?: 2000f
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
        val calendar = Calendar.getInstance()
        val now = System.currentTimeMillis()

        if (_uiState.value.selectedPeriod == 0) {
            // 对于"周"视图，摄入图表使用 ReportPeriodManager，睡眠和身体使用旧逻辑？
            // 需求要求统一为主页的摄入区块使用和子页面一模一样的计算，而且报表页面的周数要和子页面同步
            // 所以这里直接返回当前所处周期的标签
            val cYear = com.example.healthtracker.util.ReportPeriodManager.getYear(now)
            val cWeek = com.example.healthtracker.util.ReportPeriodManager.getCustomWeekNumber(now)
            val currentStart = com.example.healthtracker.util.ReportPeriodManager.getStartTimestampOfWeek(cYear, cWeek)
            val targetStart = currentStart - offset * 7 * 24 * 60 * 60 * 1000L
            val targetYear = com.example.healthtracker.util.ReportPeriodManager.getYear(targetStart)
            val targetWeek = com.example.healthtracker.util.ReportPeriodManager.getCustomWeekNumber(targetStart)
            return "第 ${targetWeek} 周"
        }

        val startDate = when (_uiState.value.selectedPeriod) {
            1 -> { // 月
                calendar.timeInMillis = now
                calendar.add(Calendar.MONTH, -offset)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            else -> now
        }

        val cal = Calendar.getInstance()
        cal.timeInMillis = startDate
        return "${cal.get(Calendar.MONTH) + 1}.${cal.get(Calendar.DAY_OF_MONTH)}"
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

            // 1. 对于睡眠和旧逻辑图表，仍然获取 Calendar 日期范围
            val (calStart, calEnd) = getDateRange()
            val weekDates = generateWeekDates(calStart)
            val bodyRecords = bodyRecordRepository.getRecordsBetweenSync(calStart, calEnd)
            val sleepRecords = sleepRecordRepository.getRecordsBetweenSync(calStart, calEnd)

            // 2. 摄入数据 - 强制使用 ReportPeriodManager 逻辑（周模式）
            val offset = _uiState.value.periodOffset
            val now = System.currentTimeMillis()
            val intakeDailyData: List<DailyNutritionData> = if (_uiState.value.selectedPeriod == 0) {
                // 周模式：计算该周的 7 天
                val cYear = com.example.healthtracker.util.ReportPeriodManager.getYear(now)
                val cWeek = com.example.healthtracker.util.ReportPeriodManager.getCustomWeekNumber(now)
                val currentStart = com.example.healthtracker.util.ReportPeriodManager.getStartTimestampOfWeek(cYear, cWeek)
                val targetStart = currentStart - offset * 7 * 24 * 60 * 60 * 1000L
                val targetYear = com.example.healthtracker.util.ReportPeriodManager.getYear(targetStart)
                val targetWeek = com.example.healthtracker.util.ReportPeriodManager.getCustomWeekNumber(targetStart)
                
                val customStart = com.example.healthtracker.util.ReportPeriodManager.getStartTimestampOfWeek(targetYear, targetWeek)
                val customEnd = com.example.healthtracker.util.ReportPeriodManager.getEndTimestampOfWeek(targetYear, targetWeek)
                
                val intakeRecords = intakeRecordRepository.getRecordsBetweenSync(customStart, customEnd)
                
                (0..6).map { dayIndex ->
                    val dayStart = com.example.healthtracker.util.ReportPeriodManager.getTimestampForDayOfWeek(targetYear, targetWeek, dayIndex)
                    val dayEnd = DateTimeUtils.getEndOfDay(dayStart)
                    val dayRecords = intakeRecords.filter { it.date in dayStart..dayEnd }
                    DailyNutritionData(
                        dayIndex = dayIndex,
                        dateLabel = com.example.healthtracker.util.ReportPeriodManager.getFormattedDateForDayOfWeek(targetYear, targetWeek, dayIndex),
                        timestamp = dayStart,
                        calories = dayRecords.sumOf { it.calories }.toFloat(),
                        carbs = dayRecords.sumOf { it.carbohydrates }.toFloat(),
                        protein = dayRecords.sumOf { it.protein }.toFloat(),
                        fat = dayRecords.sumOf { it.fat }.toFloat()
                    )
                }
            } else {
                // 月模式：聚合日历月的每天
                val intakeRecords = intakeRecordRepository.getRecordsBetweenSync(calStart, calEnd)
                val cal = Calendar.getInstance()
                cal.timeInMillis = calStart
                val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                
                (0 until daysInMonth).map { dayIndex ->
                    val dayStart = cal.timeInMillis
                    val dayEnd = DateTimeUtils.getEndOfDay(dayStart)
                    val dayRecords = intakeRecords.filter { it.date in dayStart..dayEnd }
                    val month = cal.get(Calendar.MONTH) + 1
                    val day = cal.get(Calendar.DAY_OF_MONTH)
                    
                    val data = DailyNutritionData(
                        dayIndex = dayIndex,
                        dateLabel = "$month.$day",
                        timestamp = dayStart,
                        calories = dayRecords.sumOf { it.calories }.toFloat(),
                        carbs = dayRecords.sumOf { it.carbohydrates }.toFloat(),
                        protein = dayRecords.sumOf { it.protein }.toFloat(),
                        fat = dayRecords.sumOf { it.fat }.toFloat()
                    )
                    cal.add(Calendar.DAY_OF_MONTH, 1)
                    data
                }
            }

            _uiState.value = _uiState.value.copy(
                intakeData = intakeDailyData,
                bodyData = bodyRecords.sortedByDescending { it.date },
                sleepData = sleepRecords.sortedByDescending { it.date },
                weekDates = weekDates,
                isLoading = false
            )
        }
    }

    private fun generateWeekDates(startDate: Long): List<Long> {
        val result = mutableListOf<Long>()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startDate

        val days = if (_uiState.value.selectedPeriod == 0) 7 else 30
        for (i in 0 until days) {
            result.add(calendar.timeInMillis)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return result
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