package com.example.healthtracker.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.data.repository.BodyRecordRepository
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
    private val sleepRecordRepository: SleepRecordRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TestDataState())
    val state = _state.asStateFlow()

    fun generateTestData(days: Int = 10) {
        viewModelScope.launch {
            _state.value = TestDataState(isGenerating = true)

            try {
                // 生成摄入数据
                val intakeRecords = TestDataGenerator.generateIntakeRecords(days)
                intakeRecords.forEach { record ->
                    intakeRecordRepository.insertRecord(record)
                }

                // 生成身体数据
                val bodyRecords = TestDataGenerator.generateBodyRecords(days)
                bodyRecords.forEach { record ->
                    bodyRecordRepository.insertRecord(record)
                }

                // 生成睡眠数据
                val sleepRecords = TestDataGenerator.generateSleepRecords(days)
                sleepRecords.forEach { record ->
                    sleepRecordRepository.insertRecord(record)
                }

                _state.value = TestDataState(
                    isGenerating = false,
                    message = "成功生成 ${days} 天的测试数据！\n" +
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