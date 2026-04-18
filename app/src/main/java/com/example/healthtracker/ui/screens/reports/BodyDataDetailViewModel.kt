package com.example.healthtracker.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.data.repository.BodyRecordRepository
import com.example.healthtracker.util.ReportPeriodManager
import com.example.healthtracker.util.ReportStatUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

data class BodyChartPoint(
    val weekNumber: Int,
    val value: Float,
    val isDummy: Boolean = false // If true, data is missing for this week, maybe interpolate or just skip line drawing
)

data class BodySummary1(
    val changeValue: Float,
    val changePercent: Float,
    val maxValue: Float,
    val minValue: Float
)

data class WeekToWeekChange(
    val fromWeek: Int,
    val toWeek: Int,
    val changeValue: Float
)

data class BodyDataDetailUiState(
    val dataType: Int = 0, // 0=体重, 1=体脂, 2=肌肉, 3=胸围, 4=腰围, 5=臀围
    val statMode: Int = 1, // 0=平均数, 1=中位数
    val unitMode: Int = 0, // 0=kg(或标准单位), 1=斤
    val startWeek: Int = 1,
    val endWeek: Int = 8,
    val currentYear: Int = 2026,
    val maxWeekNumber: Int = 52,
    
    val chartData: List<BodyChartPoint> = emptyList(),
    val summary1: BodySummary1? = null,
    val summary2: List<WeekToWeekChange> = emptyList(),
    
    val isLoading: Boolean = true
)

@HiltViewModel
class BodyDataDetailViewModel @Inject constructor(
    private val bodyRecordRepository: BodyRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BodyDataDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        val now = System.currentTimeMillis()
        val currentYear = ReportPeriodManager.getYear(now)
        val currentWeek = ReportPeriodManager.getCustomWeekNumber(now)
        
        val startW = (currentWeek - 3).coerceAtLeast(1)
        val endW = currentWeek

        _uiState.value = _uiState.value.copy(
            currentYear = currentYear,
            maxWeekNumber = 52, // Approximate
            startWeek = startW,
            endWeek = endW
        )
        loadData()
    }

    fun updateFilters(
        dataType: Int? = null,
        statMode: Int? = null,
        unitMode: Int? = null,
        startWeek: Int? = null,
        endWeek: Int? = null
    ) {
        val state = _uiState.value
        
        var newStart = startWeek ?: state.startWeek
        var newEnd = endWeek ?: state.endWeek
        
        // 验证周次逻辑：最少选择两周，最多选择8周
        if (startWeek != null || endWeek != null) {
            if (newStart > newEnd) {
                val temp = newStart
                newStart = newEnd
                newEnd = temp
            }
            if (newStart == newEnd) {
                if (newStart > 1) newStart -= 1 else newEnd += 1
            }
            if (newEnd - newStart + 1 > 8) {
                // 如果超过8周，调整为8周
                if (startWeek != null) { // 改变的是start
                    newEnd = newStart + 7
                } else { // 改变的是end
                    newStart = newEnd - 7
                }
            }
        }
        
        // 如果改变数据类型并且不支持斤，强制重置为kg
        var newUnitMode = unitMode ?: state.unitMode
        val newDataType = dataType ?: state.dataType
        if (newDataType > 2) {
            newUnitMode = 0
        }

        _uiState.value = state.copy(
            dataType = newDataType,
            statMode = statMode ?: state.statMode,
            unitMode = newUnitMode,
            startWeek = newStart,
            endWeek = newEnd
        )
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val state = _uiState.value
            val year = state.currentYear
            
            // 拉取这一年内指定周次的所有数据
            val startTs = ReportPeriodManager.getStartTimestampOfWeek(year, state.startWeek)
            val endTs = ReportPeriodManager.getEndTimestampOfWeek(year, state.endWeek)
            
            val records = bodyRecordRepository.getRecordsBetweenSync(startTs, endTs)
            
            // 按照自然周分组
            val points = mutableListOf<BodyChartPoint>()
            for (week in state.startWeek..state.endWeek) {
                val wStart = ReportPeriodManager.getStartTimestampOfWeek(year, week)
                val wEnd = ReportPeriodManager.getEndTimestampOfWeek(year, week)
                
                val weekRecords = records.filter { it.date in wStart..wEnd }
                
                // 提取对应的数据
                val rawValues = weekRecords.mapNotNull {
                    when (state.dataType) {
                        0 -> it.weight
                        1 -> it.bodyFatRate // OPML说可以选择kg/斤，虽然体脂率通常是%，我们尊重OPML的统一转换逻辑
                        2 -> it.muscleMass
                        3 -> it.chest
                        4 -> it.waist
                        5 -> it.hip
                        else -> it.weight
                    }
                }.map { it.toFloat() }
                
                if (rawValues.isEmpty()) {
                    // 没有数据
                    points.add(BodyChartPoint(week, 0f, isDummy = true))
                } else {
                    var value = if (state.statMode == 0) {
                        // 平均数
                        rawValues.average().toFloat()
                    } else {
                        // 中位数
                        ReportStatUtils.run { rawValues.medianBy { it } }
                    }
                    
                    // 单位转换：kg -> 斤
                    if (state.unitMode == 1 && state.dataType <= 2) {
                        value *= 2f
                    }
                    
                    points.add(BodyChartPoint(week, value, isDummy = false))
                }
            }
            
            // 计算总结数据
            val validPoints = points.filter { !it.isDummy }
            
            val summary1 = if (validPoints.size >= 2) {
                val firstVal = validPoints.first().value
                val lastVal = validPoints.last().value
                val minVal = validPoints.minOf { it.value }
                val maxVal = validPoints.maxOf { it.value }
                
                val change = lastVal - firstVal
                val changePct = ReportStatUtils.calculatePercentageChange(firstVal, lastVal)
                
                BodySummary1(change, changePct, maxVal, minVal)
            } else if (validPoints.size == 1) {
                BodySummary1(0f, 0f, validPoints[0].value, validPoints[0].value)
            } else {
                null
            }
            
            val summary2 = mutableListOf<WeekToWeekChange>()
            for (i in 0 until points.size - 1) {
                val p1 = points[i]
                val p2 = points[i + 1]
                if (!p1.isDummy && !p2.isDummy) {
                    summary2.add(WeekToWeekChange(p1.weekNumber, p2.weekNumber, p2.value - p1.value))
                }
            }
            
            _uiState.value = _uiState.value.copy(
                chartData = points,
                summary1 = summary1,
                summary2 = summary2,
                isLoading = false
            )
        }
    }
}