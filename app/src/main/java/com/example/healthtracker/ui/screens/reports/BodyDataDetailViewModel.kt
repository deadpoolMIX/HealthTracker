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
    val filterMode: Int = 0, // 0=自定义时间, 1=以周为一点
    val dataType: Int = 0, // 0=体重体脂肌肉, 1=三围
    val startDate: Long = System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L,
    val endDate: Long = System.currentTimeMillis(),
    val rawData: List<BodyRecordEntity> = emptyList(),
    val weeklyData: List<WeeklyBodyData> = emptyList(),
    val isLoading: Boolean = true
)

data class WeeklyBodyData(
    val year: Int,
    val weekOfYear: Int,
    val weekLabel: String,
    val medianWeight: Double?,
    val medianBodyFat: Double?,
    val medianMuscle: Double?,
    val medianChest: Double?,
    val medianWaist: Double?,
    val medianHip: Double?
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
        _uiState.value = _uiState.value.copy(filterMode = mode)
        loadData()
    }

    fun setDataType(type: Int) {
        _uiState.value = _uiState.value.copy(dataType = type)
    }

    fun setDateRange(start: Long, end: Long) {
        _uiState.value = _uiState.value.copy(
            startDate = start,
            endDate = end
        )
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val startOfDay = DateTimeUtils.getStartOfDay(_uiState.value.startDate)
            val endOfDay = DateTimeUtils.getEndOfDay(_uiState.value.endDate)

            val records = bodyRecordRepository.getRecordsBetweenSync(startOfDay, endOfDay)

            when (_uiState.value.filterMode) {
                0 -> {
                    // 自定义时间范围 - 原始数据
                    _uiState.value = _uiState.value.copy(
                        rawData = records.sortedBy { it.date },
                        weeklyData = emptyList(),
                        isLoading = false
                    )
                }
                1 -> {
                    // 以周为一点 - 聚合数据
                    val weeklyData = aggregateByWeek(records)
                    _uiState.value = _uiState.value.copy(
                        rawData = emptyList(),
                        weeklyData = weeklyData,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun aggregateByWeek(records: List<BodyRecordEntity>): List<WeeklyBodyData> {
        if (records.isEmpty()) return emptyList()

        // 按年+周分组
        val groupedByWeek = records.groupBy { record ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = record.date
            val year = cal.get(Calendar.YEAR)
            val weekOfYear = cal.get(Calendar.WEEK_OF_YEAR)
            Pair(year, weekOfYear)
        }

        return groupedByWeek.map { (key, weekRecords) ->
            val (year, weekOfYear) = key

            WeeklyBodyData(
                year = year,
                weekOfYear = weekOfYear,
                weekLabel = "${year}年第${weekOfYear}周",
                medianWeight = calculateMedian(weekRecords.mapNotNull { it.weight }),
                medianBodyFat = calculateMedian(weekRecords.mapNotNull { it.bodyFatRate }),
                medianMuscle = calculateMedian(weekRecords.mapNotNull { it.muscleMass }),
                medianChest = calculateMedian(weekRecords.mapNotNull { it.chest }),
                medianWaist = calculateMedian(weekRecords.mapNotNull { it.waist }),
                medianHip = calculateMedian(weekRecords.mapNotNull { it.hip })
            )
        }.sortedWith(compareBy({ it.year }, { it.weekOfYear }))
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

    // 获取变化数据
    fun getChangeData(): Triple<String, String, String> {
        val state = _uiState.value

        return when (state.filterMode) {
            0 -> {
                // 自定义时间范围
                val data = state.rawData
                if (data.size < 2) return Triple("--", "--", "--")

                val first = data.first()
                val last = data.last()

                val weightChange = calculateChange(first.weight, last.weight)
                val bodyFatChange = calculateChange(first.bodyFatRate, last.bodyFatRate)
                val muscleChange = calculateChange(first.muscleMass, last.muscleMass)

                Triple(weightChange, bodyFatChange, muscleChange)
            }
            1 -> {
                // 以周为一点
                val data = state.weeklyData
                if (data.size < 2) return Triple("--", "--", "--")

                val first = data.first()
                val last = data.last()

                val weightChange = calculateChangeNullable(first.medianWeight, last.medianWeight)
                val bodyFatChange = calculateChangeNullable(first.medianBodyFat, last.medianBodyFat)
                val muscleChange = calculateChangeNullable(first.medianMuscle, last.medianMuscle)

                Triple(weightChange, bodyFatChange, muscleChange)
            }
            else -> Triple("--", "--", "--")
        }
    }

    fun getMeasurementsChangeData(): Triple<String, String, String> {
        val state = _uiState.value

        return when (state.filterMode) {
            0 -> {
                val data = state.rawData
                if (data.size < 2) return Triple("--", "--", "--")

                val first = data.first()
                val last = data.last()

                val chestChange = calculateChange(first.chest, last.chest)
                val waistChange = calculateChange(first.waist, last.waist)
                val hipChange = calculateChange(first.hip, last.hip)

                Triple(chestChange, waistChange, hipChange)
            }
            1 -> {
                val data = state.weeklyData
                if (data.size < 2) return Triple("--", "--", "--")

                val first = data.first()
                val last = data.last()

                val chestChange = calculateChangeNullable(first.medianChest, last.medianChest)
                val waistChange = calculateChangeNullable(first.medianWaist, last.medianWaist)
                val hipChange = calculateChangeNullable(first.medianHip, last.medianHip)

                Triple(chestChange, waistChange, hipChange)
            }
            else -> Triple("--", "--", "--")
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