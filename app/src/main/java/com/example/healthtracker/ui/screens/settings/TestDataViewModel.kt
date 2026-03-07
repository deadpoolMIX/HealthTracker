package com.example.healthtracker.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.data.repository.BodyRecordRepository
import com.example.healthtracker.data.repository.FoodRepository
import com.example.healthtracker.data.repository.IntakeRecordRepository
import com.example.healthtracker.data.repository.SleepRecordRepository
import com.example.healthtracker.util.TestDataGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TestDataState(
    val isGenerating: Boolean = false,
    val message: String? = null,
    val success: Boolean? = null
)

@HiltViewModel
class TestDataViewModel @Inject constructor(
    private val intakeRecordRepository: IntakeRecordRepository,
    private val bodyRecordRepository: BodyRecordRepository,
    private val sleepRecordRepository: SleepRecordRepository,
    private val foodRepository: FoodRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TestDataState())
    val state = _state.asStateFlow()

    fun generateTestData() {
        viewModelScope.launch {
            _state.value = TestDataState(isGenerating = true)

            try {
                // 从食物库获取所有食物
                val foods = foodRepository.getAllFoodsOnce()

                if (foods.isEmpty()) {
                    _state.value = TestDataState(
                        isGenerating = false,
                        message = "食物库为空，请先添加食物数据！",
                        success = false
                    )
                    return@launch
                }

                // 生成最近一个月的摄入数据（使用食物库数据）
                val intakeRecords = TestDataGenerator.generateIntakeRecords(foods)
                intakeRecords.forEach { record ->
                    intakeRecordRepository.insertRecord(record)
                }

                // 生成最近一个月的身体数据
                val bodyRecords = TestDataGenerator.generateBodyRecords()
                bodyRecords.forEach { record ->
                    bodyRecordRepository.insertRecord(record)
                }

                // 生成最近一个月的睡眠数据
                val sleepRecords = TestDataGenerator.generateSleepRecords()
                sleepRecords.forEach { record ->
                    sleepRecordRepository.insertRecord(record)
                }

                _state.value = TestDataState(
                    isGenerating = false,
                    message = "成功生成最近一个月（30天）的测试数据！\n" +
                            "- 使用食物库：${foods.size} 种食物\n" +
                            "- 摄入记录：${intakeRecords.size} 条\n" +
                            "- 身体数据：${bodyRecords.size} 条\n" +
                            "- 睡眠记录：${sleepRecords.size} 条",
                    success = true
                )
            } catch (e: Exception) {
                _state.value = TestDataState(
                    isGenerating = false,
                    message = "生成失败：${e.message}",
                    success = false
                )
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null, success = null)
    }
}