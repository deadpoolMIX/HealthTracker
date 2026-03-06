package com.example.healthtracker.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.data.local.entity.UserSettingsEntity
import com.example.healthtracker.data.repository.UserSettingsRepository
import com.example.healthtracker.util.HealthCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    val settings: StateFlow<UserSettingsEntity?> = userSettingsRepository.getSettingsFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    fun saveUserInfo(
        gender: Int,
        age: Int,
        height: Double,
        weight: Double,
        activityLevel: Int
    ) {
        viewModelScope.launch {
            // 计算 BMR 和 TDEE
            val bmr = HealthCalculator.calculateBMR(gender, weight, height, age)
            val tdee = HealthCalculator.calculateTDEE(bmr, activityLevel)

            userSettingsRepository.updateUserInfo(
                gender = gender,
                age = age,
                height = height,
                weight = weight,
                activityLevel = activityLevel,
                bmr = bmr,
                tdee = tdee
            )

            _saveSuccess.value = true
        }
    }

    fun resetSaveSuccess() {
        _saveSuccess.value = false
    }
}