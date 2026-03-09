package com.example.healthtracker.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.data.local.entity.BodyRecordEntity
import com.example.healthtracker.data.repository.BodyRecordRepository
import com.example.healthtracker.util.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class BodyDataDetailUiState(
    val filterMode: Int = 1, // 0=自定义时间, 1=以周为点（默认周）
    val selectedDataType: Int = 0, // 0=体重, 1=体脂, 2=肌肉
    val startDate: Long = System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L,
    val endDate: Long = System.currentTimeMillis(),
    val rawData: List<BodyRecordEntity> = emptyList(),
    val weeklyData: List<WeeklyBodyData> = emptyList(),
    val weekOffset: Int = 0, // 周模式的偏移量，0=最近7周
    val isLoading: Boolean = true
)

data class WeeklyBodyData(
    val year: Int,
    val weekOfYear: Int,
    val weekLabel: String,
    val startDate: Long,
    val endDate: Long,
    val medianWeight: Double?,
    val medianBodyFat: Double?,
    val medianMuscle: Double?
)

@HiltViewModel
class BodyDataDetailViewModel @Inject constructor(
    private val bodyRecordRepository: BodyRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BodyDataDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun setFilterMode(mode: Int) {
        _uiState.value = _uiState.value.copy(filterMode = mode, weekOffset = 0)
        loadData()
    }

    fun setSelectedDataType(type: Int) {
        _uiState.value = _uiState.value.copy(selectedDataType = type)
    }

    fun setDateRange(start: Long, end: Long) {
        _uiState.value = _uiState.value.copy(
            startDate = start,
            endDate = end
        )
        loadData()
    }

    fun navigateWeek(direction: Int) {
        // direction: -1 = 前一组7周, 1 = 后一组7周
        val newOffset = (_uiState.value.weekOffset + direction).coerceAtLeast(0)
        _uiState.value = _uiState.value.copy(weekOffset = newOffset)
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            when (_uiState.value.filterMode) {
                0 -> {
                    // 自定义时间范围 - 原始数据
                    val startOfDay = DateTimeUtils.getStartOfDay(_uiState.value.startDate)
                    val endOfDay = DateTimeUtils.getEndOfDay(_uiState.value.endDate)

                    val records = bodyRecordRepository.getRecordsBetweenSync(startOfDay, endOfDay)
                    _uiState.value = _uiState.value.copy(
                        rawData = records.sortedBy { it.date },
                        weeklyData = emptyList(),
                        isLoading = false
                    )
                }
                1 -> {
                    // 以周为点 - 加载所有数据，然后聚合
                    loadWeeklyData()
                }
            }
        }
    }

    private fun loadWeeklyData() {
        viewModelScope.launch {
            // 获取所有身体数据
            val allRecords = bodyRecordRepository.getAllRecordsSync()

            if (allRecords.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    rawData = emptyList(),
                    weeklyData = emptyList(),
                    isLoading = false
                )
                return@launch
            }

            // 按年+周分组
            val groupedByWeek = allRecords.groupBy { record ->
                val cal = Calendar.getInstance()
                cal.timeInMillis = record.date
                val year = cal.get(Calendar.YEAR)
                val weekOfYear = cal.get(Calendar.WEEK_OF_YEAR)
                Pair(year, weekOfYear)
            }

            val allWeeklyData = groupedByWeek.map { (key, weekRecords) ->
                val (year, weekOfYear) = key

                // 计算这周的起始和结束日期
                val cal = Calendar.getInstance()
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.WEEK_OF_YEAR, weekOfYear)
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                val weekStart = cal.timeInMillis
                cal.add(Calendar.DAY_OF_MONTH, 6)
                val weekEnd = cal.timeInMillis

                WeeklyBodyData(
                    year = year,
                    weekOfYear = weekOfYear,
                    weekLabel = "${year}年第${weekOfYear}周",
                    startDate = weekStart,
                    endDate = weekEnd,
                    medianWeight = calculateMedian(weekRecords.mapNotNull { it.weight }),
                    medianBodyFat = calculateMedian(weekRecords.mapNotNull { it.bodyFatRate }),
                    medianMuscle = calculateMedian(weekRecords.mapNotNull { it.muscleMass })
                )
            }.sortedWith(compareBy({ it.year }, { it.weekOfYear }))

            // 根据偏移量获取7周数据
            val startIndex = (allWeeklyData.size - 7 - _uiState.value.weekOffset * 7).coerceAtLeast(0)
            val endIndex = (startIndex + 7).coerceAtMost(allWeeklyData.size)
            val visibleWeeklyData = allWeeklyData.subList(startIndex, endIndex)

            _uiState.value = _uiState.value.copy(
                rawData = emptyList(),
                weeklyData = visibleWeeklyData,
                isLoading = false
            )
        }
    }

    private fun calculateMedian(values: List<Double>): Double? {
        if (values.isEmpty()) return null

        val sorted = values.sorted()
        val mid = sorted.size / 2

        return if (sorted.size % 2 == 0) {
            (sorted[mid - 1] + sorted[mid]) / 2.0
        } else {
            sorted[mid]
        }
    }

    // 获取当前选中数据类型的变化
    fun getChangeValue(): String {
        val state = _uiState.value

        return when (state.filterMode) {
            0 -> {
                val data = state.rawData
                if (data.size < 2) return "--"

                val first = data.first()
                val last = data.last()

                when (state.selectedDataType) {
                    0 -> calculateChange(first.weight, last.weight)
                    1 -> calculateChange(first.bodyFatRate, last.bodyFatRate)
                    2 -> calculateChange(first.muscleMass, last.muscleMass)
                    else -> "--"
                }
            }
            1 -> {
                val data = state.weeklyData
                if (data.size < 2) return "--"

                val first = data.first()
                val last = data.last()

                when (state.selectedDataType) {
                    0 -> calculateChangeNullable(first.medianWeight, last.medianWeight)
                    1 -> calculateChangeNullable(first.medianBodyFat, last.medianBodyFat)
                    2 -> calculateChangeNullable(first.medianMuscle, last.medianMuscle)
                    else -> "--"
                }
            }
            else -> "--"
        }
    }

    private fun calculateChange(start: Double?, end: Double?): String {
        if (start == null || end == null) return "--"
        val change = end - start
        val sign = if (change >= 0) "+" else ""
        return "$sign${String.format("%.1f", change)}"
    }

    private fun calculateChangeNullable(start: Double?, end: Double?): String {
        if (start == null || end == null) return "--"
        val change = end - start
        val sign = if (change >= 0) "+" else ""
        return "$sign${String.format("%.1f", change)}"
    }
}