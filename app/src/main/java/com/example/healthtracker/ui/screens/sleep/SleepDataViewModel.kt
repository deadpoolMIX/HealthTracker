package com.example.healthtracker.ui.screens.sleep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.data.local.entity.SleepRecordEntity
import com.example.healthtracker.data.repository.SleepRecordRepository
import com.example.healthtracker.util.DateTimeUtils
import com.example.healthtracker.util.SelectedDateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class SleepDataUiState(
    val date: Long = System.currentTimeMillis(),
    // 入睡日期和时间
    val sleepDate: Long = System.currentTimeMillis(),
    val sleepHour: Int = 23,
    val sleepMinute: Int = 0,
    // 起床日期和时间
    val wakeDate: Long = System.currentTimeMillis(),
    val wakeHour: Int = 7,
    val wakeMinute: Int = 0,
    val existingRecord: SleepRecordEntity? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class SleepDataViewModel @Inject constructor(
    private val sleepRecordRepository: SleepRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SleepDataUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadCurrentData()
    }

    private fun loadCurrentData() {
        viewModelScope.launch {
            val selectedDate = SelectedDateManager.getSelectedDate()
            _uiState.value = _uiState.value.copy(date = selectedDate)

            // 加载选中日期的睡眠数据
            val record = sleepRecordRepository.getRecordByDate(selectedDate)
            if (record != null) {
                val sleepCalendar = Calendar.getInstance()
                sleepCalendar.timeInMillis = record.sleepTime
                val wakeCalendar = Calendar.getInstance()
                wakeCalendar.timeInMillis = record.wakeTime

                _uiState.value = _uiState.value.copy(
                    existingRecord = record,
                    sleepDate = DateTimeUtils.getStartOfDay(record.sleepTime),
                    sleepHour = sleepCalendar.get(Calendar.HOUR_OF_DAY),
                    sleepMinute = sleepCalendar.get(Calendar.MINUTE),
                    wakeDate = DateTimeUtils.getStartOfDay(record.wakeTime),
                    wakeHour = wakeCalendar.get(Calendar.HOUR_OF_DAY),
                    wakeMinute = wakeCalendar.get(Calendar.MINUTE)
                )
            } else {
                // 新建记录时，默认入睡日期为选中日期的前一天晚上
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = selectedDate
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                _uiState.value = _uiState.value.copy(
                    sleepDate = calendar.timeInMillis,
                    wakeDate = selectedDate
                )
            }
        }
    }

    fun setSleepDate(date: Long) {
        _uiState.value = _uiState.value.copy(sleepDate = date)
    }

    fun setSleepHour(value: Int) {
        _uiState.value = _uiState.value.copy(sleepHour = value)
    }

    fun setSleepMinute(value: Int) {
        _uiState.value = _uiState.value.copy(sleepMinute = value)
    }

    fun setWakeDate(date: Long) {
        _uiState.value = _uiState.value.copy(wakeDate = date)
    }

    fun setWakeHour(value: Int) {
        _uiState.value = _uiState.value.copy(wakeHour = value)
    }

    fun setWakeMinute(value: Int) {
        _uiState.value = _uiState.value.copy(wakeMinute = value)
    }

    fun save() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)

            // 计算入睡时间戳
            val sleepCalendar = Calendar.getInstance()
            sleepCalendar.timeInMillis = _uiState.value.sleepDate
            sleepCalendar.set(Calendar.HOUR_OF_DAY, _uiState.value.sleepHour)
            sleepCalendar.set(Calendar.MINUTE, _uiState.value.sleepMinute)
            sleepCalendar.set(Calendar.SECOND, 0)
            sleepCalendar.set(Calendar.MILLISECOND, 0)
            val sleepTime = sleepCalendar.timeInMillis

            // 计算起床时间戳
            val wakeCalendar = Calendar.getInstance()
            wakeCalendar.timeInMillis = _uiState.value.wakeDate
            wakeCalendar.set(Calendar.HOUR_OF_DAY, _uiState.value.wakeHour)
            wakeCalendar.set(Calendar.MINUTE, _uiState.value.wakeMinute)
            wakeCalendar.set(Calendar.SECOND, 0)
            wakeCalendar.set(Calendar.MILLISECOND, 0)
            val wakeTime = wakeCalendar.timeInMillis

            // 计算睡眠时长（分钟）
            val duration = (wakeTime - sleepTime) / (1000 * 60)

            val record = SleepRecordEntity(
                id = _uiState.value.existingRecord?.id ?: 0,
                date = _uiState.value.wakeDate, // 记录日期为起床日期
                sleepTime = sleepTime,
                wakeTime = wakeTime,
                duration = duration,
                createdAt = _uiState.value.existingRecord?.createdAt ?: System.currentTimeMillis()
            )

            if (_uiState.value.existingRecord != null) {
                sleepRecordRepository.updateRecord(record)
            } else {
                sleepRecordRepository.insertRecord(record)
            }

            _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
        }
    }
}