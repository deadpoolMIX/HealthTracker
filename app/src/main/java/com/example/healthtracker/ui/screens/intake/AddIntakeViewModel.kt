package com.example.healthtracker.ui.screens.intake

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.data.local.entity.IntakeRecordEntity
import com.example.healthtracker.data.repository.IntakeRecordRepository
import com.example.healthtracker.util.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddIntakeViewModel @Inject constructor(
    private val intakeRecordRepository: IntakeRecordRepository
) : ViewModel() {

    fun saveRecord(
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
            // 计算实际营养值
            val actualCalories = (amount / 100.0) * caloriesPer100g
            val actualCarbs = (amount / 100.0) * carbsPer100g
            val actualProtein = (amount / 100.0) * proteinPer100g
            val actualFat = (amount / 100.0) * fatPer100g

            val record = IntakeRecordEntity(
                foodName = foodName,
                date = DateTimeUtils.getStartOfDay(),
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

            intakeRecordRepository.insertRecord(record)
        }
    }
}