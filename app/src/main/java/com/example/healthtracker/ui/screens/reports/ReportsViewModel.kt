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
    val intakeData: List<DailyNutritionData> = emptyList(), 
    val targetCalories: Float = 2000f,
    val bodyData: List<BodyRecordEntity> = emptyList(),
    val sleepData: List<SleepRecordEntity> = emptyList(),
    // 月模式下的每周聚合数据
    val weeklyIntakeData: List<DailyNutritionData> = emptyList(),
    val weeklyBodyData: List<Float?> = emptyList(), // 4周的平均体重
    val weeklySleepData: List<WeeklySleepTime?> = emptyList(), // 4周的平均睡眠
    
    val weekDates: List<Long> = emptyList(), 
    val isLoading: Boolean = true,
    // 报表设置
    val showNutritionChart: Boolean = true,
    val showBodyChart: Boolean = true,
    val showSleepChart: Boolean = true,
    val defaultChartPeriod: Int = 0,
    val showSettingsDialog: Boolean = false
)

data class WeeklySleepTime(
    val weekLabel: String,
    val avgSleepHour: Float,
    val avgWakeHour: Float
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
        val now = System.currentTimeMillis()
        val cYear = com.example.healthtracker.util.ReportPeriodManager.getYear(now)
        val cWeek = com.example.healthtracker.util.ReportPeriodManager.getCustomWeekNumber(now)
        
        return if (_uiState.value.selectedPeriod == 0) {
            // 周模式：显示单周标签
            val targetStart = com.example.healthtracker.util.ReportPeriodManager.getStartTimestampOfWeek(cYear, cWeek) - offset * 7 * 24 * 60 * 60 * 1000L
            val targetWeek = com.example.healthtracker.util.ReportPeriodManager.getCustomWeekNumber(targetStart)
            "第 ${targetWeek} 周"
        } else {
            // 月模式：显示4周范围
            val currentBlockStart = com.example.healthtracker.util.ReportPeriodManager.getStartTimestampOfWeek(cYear, cWeek) - offset * 4 * 7 * 24 * 60 * 60 * 1000L
            val blockEndWeek = com.example.healthtracker.util.ReportPeriodManager.getCustomWeekNumber(currentBlockStart)
            val blockStartWeek = (blockEndWeek - 3).coerceAtLeast(1)
            "第 ${blockStartWeek}-${blockEndWeek} 周"
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val offset = _uiState.value.periodOffset
            val now = System.currentTimeMillis()
            val isWeekMode = _uiState.value.selectedPeriod == 0

            val cYear = com.example.healthtracker.util.ReportPeriodManager.getYear(now)
            val cWeek = com.example.healthtracker.util.ReportPeriodManager.getCustomWeekNumber(now)

            if (isWeekMode) {
                // --- 周模式 (7天) ---
                val currentStart = com.example.healthtracker.util.ReportPeriodManager.getStartTimestampOfWeek(cYear, cWeek)
                val targetStart = currentStart - offset * 7 * 24 * 60 * 60 * 1000L
                val targetYear = com.example.healthtracker.util.ReportPeriodManager.getYear(targetStart)
                val targetWeek = com.example.healthtracker.util.ReportPeriodManager.getCustomWeekNumber(targetStart)
                
                val startDate = com.example.healthtracker.util.ReportPeriodManager.getStartTimestampOfWeek(targetYear, targetWeek)
                val endDate = com.example.healthtracker.util.ReportPeriodManager.getEndTimestampOfWeek(targetYear, targetWeek)
                
                val intakeRecords = intakeRecordRepository.getRecordsBetweenSync(startDate, endDate)
                val bodyRecords = bodyRecordRepository.getRecordsBetweenSync(startDate, endDate)
                val sleepRecords = sleepRecordRepository.getRecordsBetweenSync(startDate, endDate)

                val weekDates = (0..6).map { com.example.healthtracker.util.ReportPeriodManager.getTimestampForDayOfWeek(targetYear, targetWeek, it) }
                
                val intakeData = weekDates.mapIndexed { index, dayStart ->
                    val dayEnd = DateTimeUtils.getEndOfDay(dayStart)
                    val dayRecords = intakeRecords.filter { it.date in dayStart..dayEnd }
                    DailyNutritionData(
                        dayIndex = index,
                        dateLabel = com.example.healthtracker.util.ReportPeriodManager.getFormattedDateForDayOfWeek(targetYear, targetWeek, index),
                        timestamp = dayStart,
                        calories = dayRecords.sumOf { it.calories }.toFloat(),
                        carbs = dayRecords.sumOf { it.carbohydrates }.toFloat(),
                        protein = dayRecords.sumOf { it.protein }.toFloat(),
                        fat = dayRecords.sumOf { it.fat }.toFloat()
                    )
                }

                _uiState.value = _uiState.value.copy(
                    intakeData = intakeData,
                    bodyData = bodyRecords,
                    sleepData = sleepRecords,
                    weekDates = weekDates,
                    isLoading = false
                )
            } else {
                // --- 月模式 (最近4周) ---
                val currentBlockEndTs = com.example.healthtracker.util.ReportPeriodManager.getStartTimestampOfWeek(cYear, cWeek) - offset * 4 * 7 * 24 * 60 * 60 * 1000L
                val endWeek = com.example.healthtracker.util.ReportPeriodManager.getCustomWeekNumber(currentBlockEndTs)
                val startWeek = (endWeek - 3).coerceAtLeast(1)
                
                val startDate = com.example.healthtracker.util.ReportPeriodManager.getStartTimestampOfWeek(cYear, startWeek)
                val endDate = com.example.healthtracker.util.ReportPeriodManager.getEndTimestampOfWeek(cYear, endWeek)
                
                val intakeRecords = intakeRecordRepository.getRecordsBetweenSync(startDate, endDate)
                val bodyRecords = bodyRecordRepository.getRecordsBetweenSync(startDate, endDate)
                val sleepRecords = sleepRecordRepository.getRecordsBetweenSync(startDate, endDate)

                val weeklyIntake = mutableListOf<DailyNutritionData>()
                val weeklyBody = mutableListOf<Float?>()
                val weeklySleep = mutableListOf<WeeklySleepTime?>()

                for (w in startWeek..endWeek) {
                    val wStart = com.example.healthtracker.util.ReportPeriodManager.getStartTimestampOfWeek(cYear, w)
                    val wEnd = com.example.healthtracker.util.ReportPeriodManager.getEndTimestampOfWeek(cYear, w)
                    val wIntake = intakeRecords.filter { it.date in wStart..wEnd }
                    val wBody = bodyRecords.filter { it.date in wStart..wEnd }
                    val wSleep = sleepRecords.filter { it.date in wStart..wEnd }

                    // 摄入聚合 (求和)
                    weeklyIntake.add(DailyNutritionData(
                        dayIndex = w - startWeek,
                        dateLabel = "第${w}周",
                        timestamp = wStart,
                        calories = wIntake.sumOf { it.calories }.toFloat(),
                        carbs = wIntake.sumOf { it.carbohydrates }.toFloat(),
                        protein = wIntake.sumOf { it.protein }.toFloat(),
                        fat = wIntake.sumOf { it.fat }.toFloat()
                    ))

                    // 身体聚合 (平均)
                    if (wBody.isNotEmpty()) {
                        weeklyBody.add(wBody.mapNotNull { it.weight }.average().toFloat())
                    } else {
                        weeklyBody.add(null)
                    }

                    // 睡眠聚合 (平均时间)
                    if (wSleep.isNotEmpty()) {
                        val avgSleep = wSleep.map {
                            val cal = Calendar.getInstance().apply { timeInMillis = it.sleepTime }
                            var h = cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60f
                            if (h < 18) h += 24f // 跨天处理
                            h
                        }.average().toFloat()
                        
                        val avgWake = wSleep.map {
                            val cal = Calendar.getInstance().apply { timeInMillis = it.wakeTime }
                            cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60f
                        }.average().toFloat()
                        
                        weeklySleep.add(WeeklySleepTime("第${w}周", avgSleep, avgWake))
                    } else {
                        weeklySleep.add(null)
                    }
                }

                _uiState.value = _uiState.value.copy(
                    weeklyIntakeData = weeklyIntake,
                    weeklyBodyData = weeklyBody,
                    weeklySleepData = weeklySleep,
                    bodyData = bodyRecords, // 基础列表仍保留
                    sleepData = sleepRecords,
                    isLoading = false
                )
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

    // 获取平均睡眠时长
    fun getAverageSleepDuration(): Long {
        if (_uiState.value.sleepData.isEmpty()) return 0
        return _uiState.value.sleepData.map { it.duration }.average().toLong()
    }

    // 获取平均入睡时间
    fun getAverageSleepTime(): String {
        if (_uiState.value.sleepData.isEmpty()) return "--:--"
        val avgMinutes = _uiState.value.sleepData.map {
            val cal = Calendar.getInstance().apply { timeInMillis = it.sleepTime }
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val minute = cal.get(Calendar.MINUTE)
            if (hour < 12) (hour + 24) * 60 + minute else hour * 60 + minute
        }.average().toInt()
        val actualMinutes = avgMinutes % (24 * 60)
        return String.format("%02d:%02d", actualMinutes / 60, actualMinutes % 60)
    }

    // 获取平均起床时间
    fun getAverageWakeTime(): String {
        if (_uiState.value.sleepData.isEmpty()) return "--:--"
        val avgMinutes = _uiState.value.sleepData.map {
            val cal = Calendar.getInstance().apply { timeInMillis = it.wakeTime }
            cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        }.average().toInt()
        return String.format("%02d:%02d", avgMinutes / 60, avgMinutes % 60)
    }
}