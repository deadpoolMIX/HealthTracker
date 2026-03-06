package com.example.healthtracker.ui.screens.intake

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.data.local.entity.IntakeRecordEntity
import com.example.healthtracker.data.repository.IntakeRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditIntakeViewModel @Inject constructor(
    private val intakeRecordRepository: IntakeRecordRepository
) : ViewModel() {

    private val _record = MutableStateFlow<IntakeRecordEntity?>(null)
    val record = _record.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    fun loadRecord(recordId: Long) {
        viewModelScope.launch {
            _record.value = intakeRecordRepository.getRecordById(recordId)
        }
    }

    fun updateRecord(
        recordId: Long,
        foodName: String,
        amount: Double,
        caloriesPer100g: Double,
        carbsPer100g: Double,
        proteinPer100g: Double,
        fatPer100g: Double,
        mealType: Int,
        unit: String?,
        amountInUnit: Double?,
        gramsPerUnit: Double?,
        note: String?
    ) {
        viewModelScope.launch {
            _isSaving.value = true

            val existingRecord = _record.value ?: return@launch

            // 计算实际营养值
            val actualCalories = (amount / 100.0) * caloriesPer100g
            val actualCarbs = (amount / 100.0) * carbsPer100g
            val actualProtein = (amount / 100.0) * proteinPer100g
            val actualFat = (amount / 100.0) * fatPer100g

            val updatedRecord = existingRecord.copy(
                foodName = foodName,
                amount = amount,
                calories = actualCalories,
                carbohydrates = actualCarbs,
                protein = actualProtein,
                fat = actualFat,
                mealType = mealType,
                caloriesPer100g = caloriesPer100g,
                carbsPer100g = carbsPer100g,
                proteinPer100g = proteinPer100g,
                fatPer100g = fatPer100g,
                unit = unit,
                amountInUnit = amountInUnit,
                gramsPerUnit = gramsPerUnit,
                note = note
            )

            intakeRecordRepository.updateRecord(updatedRecord)
            _isSaving.value = false
        }
    }
}