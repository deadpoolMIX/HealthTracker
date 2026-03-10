package com.example.healthtracker.ui.screens.cyclefood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.data.local.entity.CycleFoodEntity
import com.example.healthtracker.data.repository.CycleFoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditCycleFoodViewModel @Inject constructor(
    private val cycleFoodRepository: CycleFoodRepository
) : ViewModel() {

    private val _cycleFood = MutableStateFlow<CycleFoodEntity?>(null)
    val cycleFood: StateFlow<CycleFoodEntity?> = _cycleFood.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    fun loadCycleFood(id: Long) {
        viewModelScope.launch {
            _cycleFood.value = cycleFoodRepository.getCycleFoodById(id)
        }
    }

    fun updateCycleFood(
        id: Long,
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

            val existing = cycleFoodRepository.getCycleFoodById(id)
            if (existing != null) {
                // 计算已吃的比例
                val eatenRatio = if (existing.totalCalories > 0) {
                    (existing.totalCalories - existing.remainingCalories) / existing.totalCalories
                } else 0.0

                // 更新总数，同时按比例更新剩余量
                val updatedCycleFood = existing.copy(
                    name = name,
                    icon = icon,
                    totalCalories = totalCalories,
                    totalCarbs = totalCarbs,
                    totalProtein = totalProtein,
                    totalFat = totalFat,
                    remainingCalories = totalCalories * (1 - eatenRatio),
                    remainingCarbs = totalCarbs * (1 - eatenRatio),
                    remainingProtein = totalProtein * (1 - eatenRatio),
                    remainingFat = totalFat * (1 - eatenRatio),
                    expectedDays = expectedDays
                )
                cycleFoodRepository.updateCycleFood(updatedCycleFood)
            }

            _isSaving.value = false
            _saveSuccess.value = true
        }
    }
}