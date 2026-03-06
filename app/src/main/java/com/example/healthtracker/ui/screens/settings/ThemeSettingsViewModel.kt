package com.example.healthtracker.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.data.local.entity.UserSettingsEntity
import com.example.healthtracker.data.repository.UserSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeSettingsViewModel @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    val settings = userSettingsRepository.getSettingsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateThemeMode(mode: Int) {
        viewModelScope.launch {
            userSettingsRepository.updateThemeMode(mode)
        }
    }

    fun updateThemeColor(color: Int) {
        viewModelScope.launch {
            userSettingsRepository.updateThemeColor(color)
        }
    }
}