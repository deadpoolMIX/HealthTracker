package com.example.healthtracker.ui.screens.cyclefood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.data.local.entity.CycleFoodEntity
import com.example.healthtracker.data.local.entity.IntakeRecordEntity
import com.example.healthtracker.data.repository.CycleFoodRepository
import com.example.healthtracker.data.repository.IntakeRecordRepository
import com.example.healthtracker.util.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddCycleFoodViewModel @Inject constructor(
    private val cycleFoodRepository: CycleFoodRepository
) : ViewModel() {

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    fun saveCycleFood(
        name: String,
        icon: String,
        totalCalories: Double,
        totalCarbs: Double,
        totalProtein: Double,
        totalFat: Double,
        expectedDays: Int
    ) {
        viewModelScope.launch {
            _isSaving.value = true

            val cycleFood = CycleFoodEntity(
                name = name,
                icon = icon,
                totalCalories = totalCalories,
                totalCarbs = totalCarbs,
                totalProtein = totalProtein,
                totalFat = totalFat,
                remainingCalories = totalCalories,
                remainingCarbs = totalCarbs,
                remainingProtein = totalProtein,
                remainingFat = totalFat,
                expectedDays = expectedDays,
                startDate = System.currentTimeMillis(),
                isActive = true
            )

            cycleFoodRepository.insertCycleFood(cycleFood)

            _isSaving.value = false
            _saveSuccess.value = true
        }
    }
}