package com.example.healthtracker.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.data.local.entity.IntakeRecordEntity
import com.example.healthtracker.data.repository.IntakeRecordRepository
import com.example.healthtracker.util.DateTimeUtils
import com.example.healthtracker.util.SelectedDateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class CalendarUiState(
    val selectedDate: Long = System.currentTimeMillis(),
    val selectedMonth: Long = System.currentTimeMillis(),
    val dailyCalories: Map<Long, Double> = emptyMap(), // 日期 -> 热量
    val selectedDayRecords: List<IntakeRecordEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val intakeRecordRepository: IntakeRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadMonthData()
    }

    fun selectDate(date: Long) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        loadDayRecords(date)
        // 同步到全局状态，首页会显示选中日期的数据
        SelectedDateManager.setSelectedDate(date)
    }

    fun selectMonth(month: Long) {
        _uiState.value = _uiState.value.copy(selectedMonth = month)
        loadMonthData()
    }

    fun previousMonth() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = _uiState.value.selectedMonth
        calendar.add(Calendar.MONTH, -1)
        selectMonth(calendar.timeInMillis)
    }

    fun nextMonth() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = _uiState.value.selectedMonth
        calendar.add(Calendar.MONTH, 1)
        selectMonth(calendar.timeInMillis)
    }

    private fun loadMonthData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // 获取当月的开始和结束日期
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = _uiState.value.selectedMonth
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val monthStart = calendar.timeInMillis

            calendar.add(Calendar.MONTH, 1)
            calendar.add(Calendar.MILLISECOND, -1)
            val monthEnd = calendar.timeInMillis

            // 获取当月所有摄入记录
            val records = intakeRecordRepository.getRecordsBetweenSync(monthStart, monthEnd)

            // 按日期汇总热量
            val caloriesMap = records
                .groupBy { DateTimeUtils.getStartOfDay(it.date) }
                .mapValues { (_, dayRecords) -> dayRecords.sumOf { it.calories } }

            _uiState.value = _uiState.value.copy(
                dailyCalories = caloriesMap,
                isLoading = false
            )

            // 加载选中日期的记录
            loadDayRecords(_uiState.value.selectedDate)
        }
    }

    private fun loadDayRecords(date: Long) {
        viewModelScope.launch {
            val dayStart = DateTimeUtils.getStartOfDay(date)
            val dayEnd = DateTimeUtils.getEndOfDay(date)
            val records = intakeRecordRepository.getRecordsBetweenSync(dayStart, dayEnd)
            _uiState.value = _uiState.value.copy(selectedDayRecords = records.sortedByDescending { it.createdAt })
        }
    }

    fun getMonthName(): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = _uiState.value.selectedMonth
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        return "${year}年${month}月"
    }

    fun getMaxCalories(): Double {
        return _uiState.value.dailyCalories.values.maxOrNull() ?: 2000.0
    }

    // 设置月份（用于年月选择对话框）
    fun setMonth(month: Long) {
        selectMonth(month)
    }
}