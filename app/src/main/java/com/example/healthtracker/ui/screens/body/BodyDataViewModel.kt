package com.example.healthtracker.ui.screens.body

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.data.local.entity.BodyRecordEntity
import com.example.healthtracker.data.repository.BodyRecordRepository
import com.example.healthtracker.util.DateTimeUtils
import com.example.healthtracker.util.SelectedDateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BodyDataUiState(
    val date: Long = System.currentTimeMillis(),
    val weight: String = "",
    val bodyFatRate: String = "",
    val muscleMass: String = "",
    val chest: String = "",
    val waist: String = "",
    val hip: String = "",
    val existingRecord: BodyRecordEntity? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class BodyDataViewModel @Inject constructor(
    private val bodyRecordRepository: BodyRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BodyDataUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadCurrentData()
    }

    private fun loadCurrentData() {
        viewModelScope.launch {
            val selectedDate = SelectedDateManager.getSelectedDate()
            _uiState.value = _uiState.value.copy(date = selectedDate)

            // 加载选中日期的身体数据
            val record = bodyRecordRepository.getRecordByDate(selectedDate)
            if (record != null) {
                _uiState.value = _uiState.value.copy(
                    existingRecord = record,
                    weight = record.weight?.toString() ?: "",
                    bodyFatRate = record.bodyFatRate?.toString() ?: "",
                    muscleMass = record.muscleMass?.toString() ?: "",
                    chest = record.chest?.toString() ?: "",
                    waist = record.waist?.toString() ?: "",
                    hip = record.hip?.toString() ?: ""
                )
            }
        }
    }

    fun setWeight(value: String) {
        _uiState.value = _uiState.value.copy(weight = value)
    }

    fun setBodyFatRate(value: String) {
        _uiState.value = _uiState.value.copy(bodyFatRate = value)
    }

    fun setMuscleMass(value: String) {
        _uiState.value = _uiState.value.copy(muscleMass = value)
    }

    fun setChest(value: String) {
        _uiState.value = _uiState.value.copy(chest = value)
    }

    fun setWaist(value: String) {
        _uiState.value = _uiState.value.copy(waist = value)
    }

    fun setHip(value: String) {
        _uiState.value = _uiState.value.copy(hip = value)
    }

    fun save() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)

            val record = BodyRecordEntity(
                id = _uiState.value.existingRecord?.id ?: 0,
                date = _uiState.value.date,
                weight = _uiState.value.weight.toDoubleOrNull(),
                bodyFatRate = _uiState.value.bodyFatRate.toDoubleOrNull(),
                muscleMass = _uiState.value.muscleMass.toDoubleOrNull(),
                chest = _uiState.value.chest.toDoubleOrNull(),
                waist = _uiState.value.waist.toDoubleOrNull(),
                hip = _uiState.value.hip.toDoubleOrNull(),
                createdAt = _uiState.value.existingRecord?.createdAt ?: System.currentTimeMillis()
            )

            if (_uiState.value.existingRecord != null) {
                bodyRecordRepository.updateRecord(record)
            } else {
                bodyRecordRepository.insertRecord(record)
            }

            _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
        }
    }
}