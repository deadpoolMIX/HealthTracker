package com.example.healthtracker.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.data.local.entity.SleepRecordEntity
import com.example.healthtracker.data.repository.SleepRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class SleepDetailUiState(
    val selectedPeriod: Int = 0, // 0=周, 1=月, 2=年
    val periodOffset: Int = 0, // 0=本周/本月/本年, 1=上周/上月/上年
    val sleepData: List<SleepRecordEntity> = emptyList(),
    val isLoading: Boolean = true,
    val yearPage: Int = 0 // 0=第一页(1-6月), 1=第二页(7-12月)
)

@HiltViewModel
class SleepDetailViewModel @Inject constructor(
    private val sleepRecordRepository: SleepRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SleepDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun setPeriod(period: Int) {
        _uiState.value = _uiState.value.copy(
            selectedPeriod = period,
            periodOffset = 0,
            yearPage = 0
        )
        loadData()
    }

    fun setPeriodOffset(offset: Int) {
        _uiState.value = _uiState.value.copy(periodOffset = offset.coerceAtLeast(0))
        loadData()
    }

    fun setYearPage(page: Int) {
        _uiState.value = _uiState.value.copy(yearPage = page.coerceIn(0, 1))
    }

    fun getPeriodLabel(): String {
        val offset = _uiState.value.periodOffset
        return when (_uiState.value.selectedPeriod) {
            0 -> when (offset) {
                0 -> "本周"
                1 -> "上周"
                else -> "${offset}周前"
            }
            1 -> when (offset) {
                0 -> "本月"
                1 -> "上月"
                else -> "${offset}个月前"
            }
            else -> when (offset) {
                0 -> "本年"
                1 -> "上年"
                else -> "${offset}年前"
            }
        }
    }

    fun getPreviousPeriodLabel(): String {
        val offset = _uiState.value.periodOffset - 1
        if (offset < 0) return ""
        return when (_uiState.value.selectedPeriod) {
            0 -> if (offset == 0) "本周" else "${offset}周前"
            1 -> if (offset == 0) "本月" else "${offset}个月前"
            else -> if (offset == 0) "本年" else "${offset}年前"
        }
    }

    fun getNextPeriodLabel(): String {
        val offset = _uiState.value.periodOffset + 1
        return when (_uiState.value.selectedPeriod) {
            0 -> if (_uiState.value.periodOffset == 0) "上周" else "${offset}周前"
            1 -> if (_uiState.value.periodOffset == 0) "上月" else "${offset}个月前"
            else -> if (_uiState.value.periodOffset == 0) "上年" else "${offset}年前"
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val (startDate, endDate) = getDateRange()
            val sleepRecords = sleepRecordRepository.getRecordsBetweenSync(startDate, endDate)

            _uiState.value = _uiState.value.copy(
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
            0 -> { // 周
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
                Pair(start, calendar.timeInMillis)
            }
            1 -> { // 月
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
                Pair(start, calendar.timeInMillis)
            }
            2 -> { // 年
                calendar.timeInMillis = now
                calendar.add(Calendar.YEAR, -offset)
                calendar.set(Calendar.MONTH, Calendar.JANUARY)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                calendar.set(Calendar.MONTH, Calendar.DECEMBER)
                calendar.set(Calendar.DAY_OF_MONTH, 31)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                Pair(start, calendar.timeInMillis)
            }
            else -> Pair(0L, Long.MAX_VALUE)
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

        val avgMinutes = _uiState.value.sleepData.map {
            val cal = Calendar.getInstance()
            cal.timeInMillis = it.sleepTime
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
            val cal = Calendar.getInstance()
            cal.timeInMillis = it.wakeTime
            cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        }.average().toInt()
        return String.format("%02d:%02d", avgMinutes / 60, avgMinutes % 60)
    }

    // 获取年度数据（按月分组）
    fun getMonthlyData(): List<MonthlySleepTime> {
        val calendar = Calendar.getInstance()
        val monthlyData = mutableListOf<MonthlySleepTime>()
        val startHour = 22

        for (month in 1..12) {
            val monthRecords = _uiState.value.sleepData.filter { record ->
                calendar.timeInMillis = record.date
                calendar.get(Calendar.MONTH) + 1 == month
            }

            val avgSleepHour = if (monthRecords.isNotEmpty()) {
                monthRecords.map { record ->
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = record.sleepTime
                    val hour = cal.get(Calendar.HOUR_OF_DAY)
                    val minute = cal.get(Calendar.MINUTE)
                    var h = hour + minute / 60f
                    if (h < startHour) h += 24
                    h
                }.average().toFloat()
            } else 23f

            val avgWakeHour = if (monthRecords.isNotEmpty()) {
                monthRecords.map { record ->
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = record.wakeTime
                    cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60f
                }.average().toFloat()
            } else 7f

            monthlyData.add(MonthlySleepTime(
                monthLabel = "${month}月",
                monthIndex = month,
                avgSleepHour = avgSleepHour,
                avgWakeHour = avgWakeHour
            ))
        }

        return monthlyData
    }
}

data class MonthlySleepTime(
    val monthLabel: String,
    val monthIndex: Int,
    val avgSleepHour: Float,
    val avgWakeHour: Float
)