package com.example.healthtracker.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.data.local.entity.IntakeRecordEntity
import com.example.healthtracker.data.repository.IntakeRecordRepository
import com.example.healthtracker.util.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class NutritionDetailUiState(
    val period: Int = 0, // 0=周, 1=月
    val periodOffset: Int = 0, // 0=本周/本月, 1=上周/上月, 2=上上周等
    val dailyData: List<DailyNutrition> = emptyList(),
    val weeklyData: List<WeeklyNutrition> = emptyList(),
    val avgCalories: Double = 0.0,
    val avgCarbs: Double = 0.0,
    val avgProtein: Double = 0.0,
    val avgFat: Double = 0.0,
    val totalCalories: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalProtein: Double = 0.0,
    val totalFat: Double = 0.0,
    val isLoading: Boolean = true
)

data class WeeklyNutrition(
    val weekStart: Long,
    val weekEnd: Long,
    val weekLabel: String,
    val calories: Double,
    val carbs: Double,
    val protein: Double,
    val fat: Double
)

@HiltViewModel
class NutritionDetailViewModel @Inject constructor(
    private val intakeRecordRepository: IntakeRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NutritionDetailUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun setPeriod(period: Int) {
        _uiState.value = _uiState.value.copy(period = period, periodOffset = 0)
        loadData()
    }

    fun setPeriodOffset(offset: Int) {
        _uiState.value = _uiState.value.copy(periodOffset = offset.coerceAtLeast(0))
        loadData()
    }

    fun getPeriodLabel(): String {
        // 显示图表第一天的日期，如"3.9"
        val calendar = Calendar.getInstance()
        val now = System.currentTimeMillis()
        val offset = _uiState.value.periodOffset

        val startDate = when (_uiState.value.period) {
            0 -> { // 周
                calendar.timeInMillis = now
                calendar.add(Calendar.WEEK_OF_YEAR, -offset)
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
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

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val calendar = Calendar.getInstance()
            val now = System.currentTimeMillis()
            val offset = _uiState.value.periodOffset

            val (startDate, endDate) = when (_uiState.value.period) {
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
                else -> Pair(DateTimeUtils.getStartOfDay(now), DateTimeUtils.getEndOfDay(now))
            }

            val records = intakeRecordRepository.getRecordsBetweenSync(startDate, endDate)

            when (_uiState.value.period) {
                0 -> {
                    // 按天聚合
                    val dailyData = aggregateByDay(records, startDate, endDate)
                    val stats = calculateStats(dailyData)
                    _uiState.value = _uiState.value.copy(
                        dailyData = dailyData,
                        weeklyData = emptyList(),
                        avgCalories = stats.first,
                        avgCarbs = stats.second,
                        avgProtein = stats.third,
                        avgFat = stats.fourth,
                        totalCalories = dailyData.sumOf { it.calories },
                        totalCarbs = dailyData.sumOf { it.carbs },
                        totalProtein = dailyData.sumOf { it.protein },
                        totalFat = dailyData.sumOf { it.fat },
                        isLoading = false
                    )
                }
                1 -> {
                    // 按周聚合
                    val weeklyData = aggregateByWeek(records, startDate)
                    val stats = calculateWeeklyStats(weeklyData)
                    _uiState.value = _uiState.value.copy(
                        dailyData = emptyList(),
                        weeklyData = weeklyData,
                        avgCalories = stats.first,
                        avgCarbs = stats.second,
                        avgProtein = stats.third,
                        avgFat = stats.fourth,
                        totalCalories = weeklyData.sumOf { it.calories },
                        totalCarbs = weeklyData.sumOf { it.carbs },
                        totalProtein = weeklyData.sumOf { it.protein },
                        totalFat = weeklyData.sumOf { it.fat },
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun aggregateByDay(
        records: List<IntakeRecordEntity>,
        startDate: Long,
        endDate: Long
    ): List<DailyNutrition> {
        val result = mutableListOf<DailyNutrition>()
        val calendar = Calendar.getInstance()

        // 生成7天的日期
        calendar.timeInMillis = startDate
        for (i in 0 until 7) {
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

    private fun aggregateByWeek(
        records: List<IntakeRecordEntity>,
        startDate: Long
    ): List<WeeklyNutrition> {
        val result = mutableListOf<WeeklyNutrition>()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startDate

        for (i in 0 until 4) {
            val weekStart = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_MONTH, 6)
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val weekEnd = calendar.timeInMillis

            val weekRecords = records.filter { it.date in weekStart..weekEnd }

            val weekLabel = "第${i + 1}周"

            result.add(WeeklyNutrition(
                weekStart = weekStart,
                weekEnd = weekEnd,
                weekLabel = weekLabel,
                calories = weekRecords.sumOf { it.calories },
                carbs = weekRecords.sumOf { it.carbohydrates },
                protein = weekRecords.sumOf { it.protein },
                fat = weekRecords.sumOf { it.fat }
            ))

            calendar.add(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
        }

        return result
    }

    private fun calculateStats(data: List<DailyNutrition>): Tuple4<Double, Double, Double, Double> {
        if (data.isEmpty()) return Tuple4(0.0, 0.0, 0.0, 0.0)
        return Tuple4(
            data.sumOf { it.calories } / data.size,
            data.sumOf { it.carbs } / data.size,
            data.sumOf { it.protein } / data.size,
            data.sumOf { it.fat } / data.size
        )
    }

    private fun calculateWeeklyStats(data: List<WeeklyNutrition>): Tuple4<Double, Double, Double, Double> {
        if (data.isEmpty()) return Tuple4(0.0, 0.0, 0.0, 0.0)
        return Tuple4(
            data.sumOf { it.calories } / data.size,
            data.sumOf { it.carbs } / data.size,
            data.sumOf { it.protein } / data.size,
            data.sumOf { it.fat } / data.size
        )
    }
}

private data class Tuple4<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)