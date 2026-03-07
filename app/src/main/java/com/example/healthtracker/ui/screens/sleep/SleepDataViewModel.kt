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
    val sleepHour: Int = 23,
    val sleepMinute: Int = 0,
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
                    sleepHour = sleepCalendar.get(Calendar.HOUR_OF_DAY),
                    sleepMinute = sleepCalendar.get(Calendar.MINUTE),
                    wakeHour = wakeCalendar.get(Calendar.HOUR_OF_DAY),
                    wakeMinute = wakeCalendar.get(Calendar.MINUTE)
                )
            }
        }
    }

    fun setSleepHour(value: Int) {
        _uiState.value = _uiState.value.copy(sleepHour = value)
    }

    fun setSleepMinute(value: Int) {
        _uiState.value = _uiState.value.copy(sleepMinute = value)
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

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = _uiState.value.date
            calendar.set(Calendar.HOUR_OF_DAY, _uiState.value.sleepHour)
            calendar.set(Calendar.MINUTE, _uiState.value.sleepMinute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            // 如果入睡时间在凌晨，说明是前一天晚上
            if (_uiState.value.sleepHour < 12) {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            }
            val sleepTime = calendar.timeInMillis

            // 计算起床时间
            val sleepMinutes = _uiState.value.sleepHour * 60 + _uiState.value.sleepMinute
            val wakeMinutes = _uiState.value.wakeHour * 60 + _uiState.value.wakeMinute
            val duration = if (wakeMinutes > sleepMinutes) {
                wakeMinutes - sleepMinutes
            } else {
                (24 * 60 - sleepMinutes) + wakeMinutes
            }

            calendar.timeInMillis = sleepTime
            calendar.add(Calendar.MINUTE, duration.toInt())
            val wakeTime = calendar.timeInMillis

            val record = SleepRecordEntity(
                id = _uiState.value.existingRecord?.id ?: 0,
                date = _uiState.value.date,
                sleepTime = sleepTime,
                wakeTime = wakeTime,
                duration = duration.toLong(),
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