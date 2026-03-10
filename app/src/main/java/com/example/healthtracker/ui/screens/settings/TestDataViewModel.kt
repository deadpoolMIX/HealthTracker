package com.example.healthtracker.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.data.local.entity.TestRecordEntity
import com.example.healthtracker.data.repository.TestRecordRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class TestDataUiState(
    val isGenerating: Boolean = false,
    val isDeleting: Boolean = false,
    val generateSuccess: Boolean = false,
    val deleteSuccess: Boolean = false,
    val recordCount: Int = 0,
    val showDeleteConfirm: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class TestDataViewModel @Inject constructor(
    private val testRecordRepository: TestRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TestDataUiState())
    val uiState = _uiState.asStateFlow()

    private val gson = Gson()

    init {
        loadRecordCount()
    }

    private fun loadRecordCount() {
        viewModelScope.launch {
            val count = testRecordRepository.getRecordCount()
            _uiState.value = _uiState.value.copy(recordCount = count)
        }
    }

    fun generateTestData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGenerating = true, message = null)

            try {
                val records = mutableListOf<TestRecordEntity>()
                val calendar = Calendar.getInstance()

                // 生成过去30天的测试数据
                for (dayOffset in 0 until 30) {
                    calendar.timeInMillis = System.currentTimeMillis()
                    calendar.add(Calendar.DAY_OF_YEAR, -dayOffset)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val date = calendar.timeInMillis

                    // 生成饮食数据 (3餐)
                    records.addAll(generateIntakeRecords(date))

                    // 生成身体数据
                    records.add(generateBodyRecord(date))

                    // 生成睡眠数据
                    records.add(generateSleepRecord(date))
                }

                testRecordRepository.insertRecords(records)
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    generateSuccess = true,
                    recordCount = records.size,
                    message = "成功生成 ${records.size} 条测试数据"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    message = "生成失败: ${e.message}"
                )
            }
        }
    }

    private fun generateIntakeRecords(date: Long): List<TestRecordEntity> {
        val records = mutableListOf<TestRecordEntity>()
        val random = java.util.Random()

        // 食物列表
        val foods = listOf(
            FoodData("米饭", 116.0, 25.6, 2.6, 0.3),
            FoodData("鸡蛋", 144.0, 2.8, 13.3, 8.8),
            FoodData("牛奶", 54.0, 4.9, 3.0, 3.2),
            FoodData("苹果", 52.0, 13.8, 0.3, 0.2),
            FoodData("面包", 265.0, 50.0, 8.5, 3.2),
            FoodData("鸡肉", 167.0, 0.0, 19.3, 9.4),
            FoodData("牛肉", 125.0, 0.2, 20.0, 4.2),
            FoodData("西兰花", 36.0, 4.3, 4.1, 0.6),
            FoodData("豆腐", 81.0, 4.2, 8.1, 3.7),
            FoodData("香蕉", 91.0, 20.0, 1.4, 0.2)
        )

        // 生成早中晚三餐
        val mealTypes = listOf(0, 1, 2) // 早餐、午餐、晚餐
        for (mealType in mealTypes) {
            val food = foods[random.nextInt(foods.size)]
            val amount = 100 + random.nextInt(150) // 100-250g

            val intakeData = IntakeTestData(
                foodName = food.name,
                amount = amount.toDouble(),
                calories = food.calories * amount / 100,
                carbs = food.carbs * amount / 100,
                protein = food.protein * amount / 100,
                fat = food.fat * amount / 100,
                mealType = mealType
            )

            records.add(TestRecordEntity(
                recordType = "intake",
                date = date,
                dataJson = gson.toJson(intakeData)
            ))
        }

        return records
    }

    private fun generateBodyRecord(date: Long): TestRecordEntity {
        val random = java.util.Random()

        val bodyData = BodyTestData(
            weight = 60.0 + random.nextDouble() * 15, // 60-75kg
            bodyFatRate = 15.0 + random.nextDouble() * 10, // 15-25%
            muscleMass = 30.0 + random.nextDouble() * 10, // 30-40kg
            chest = 85.0 + random.nextDouble() * 10,
            waist = 70.0 + random.nextDouble() * 10,
            hip = 90.0 + random.nextDouble() * 10
        )

        return TestRecordEntity(
            recordType = "body",
            date = date,
            dataJson = gson.toJson(bodyData)
        )
    }

    private fun generateSleepRecord(date: Long): TestRecordEntity {
        val random = java.util.Random()

        val sleepHour = 22 + random.nextInt(2) // 22:00 - 23:59
        val sleepMinute = random.nextInt(60)
        val wakeHour = 6 + random.nextInt(2) // 6:00 - 7:59
        val wakeMinute = random.nextInt(60)

        val sleepData = SleepTestData(
            sleepHour = sleepHour,
            sleepMinute = sleepMinute,
            wakeHour = wakeHour,
            wakeMinute = wakeMinute,
            duration = ((24 - sleepHour + wakeHour) * 60L - sleepMinute + wakeMinute)
        )

        return TestRecordEntity(
            recordType = "sleep",
            date = date,
            dataJson = gson.toJson(sleepData)
        )
    }

    fun showDeleteConfirm() {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = true)
    }

    fun hideDeleteConfirm() {
        _uiState.value = _uiState.value.copy(showDeleteConfirm = false)
    }

    fun deleteAllTestData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true)
            try {
                testRecordRepository.deleteAllRecords()
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    deleteSuccess = true,
                    recordCount = 0,
                    showDeleteConfirm = false,
                    message = "已删除所有测试数据"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    showDeleteConfirm = false,
                    message = "删除失败: ${e.message}"
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    // 数据类
    data class FoodData(
        val name: String,
        val calories: Double,
        val carbs: Double,
        val protein: Double,
        val fat: Double
    )

    data class IntakeTestData(
        val foodName: String,
        val amount: Double,
        val calories: Double,
        val carbs: Double,
        val protein: Double,
        val fat: Double,
        val mealType: Int
    )

    data class BodyTestData(
        val weight: Double,
        val bodyFatRate: Double,
        val muscleMass: Double,
        val chest: Double,
        val waist: Double,
        val hip: Double
    )

    data class SleepTestData(
        val sleepHour: Int,
        val sleepMinute: Int,
        val wakeHour: Int,
        val wakeMinute: Int,
        val duration: Long
    )
}