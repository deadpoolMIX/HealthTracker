package com.example.healthtracker.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthtracker.data.local.entity.IntakeRecordEntity
import com.example.healthtracker.data.repository.IntakeRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val intakeRecordRepository: IntakeRecordRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedMealTypes = MutableStateFlow<Set<Int>>(emptySet())
    val selectedMealTypes = _selectedMealTypes.asStateFlow()

    private val _startDate = MutableStateFlow<Long?>(null)
    val startDate = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow<Long?>(null)
    val endDate = _endDate.asStateFlow()

    // 搜索结果
    val searchResults = combine(
        _searchQuery,
        _selectedMealTypes,
        _startDate,
        _endDate
    ) { query, mealTypes, start, end ->
        if (query.isBlank() && mealTypes.isEmpty() && start == null && end == null) {
            emptyList<IntakeRecordEntity>()
        } else {
            intakeRecordRepository.searchRecords(query).first().filter { record ->
                val mealMatch = mealTypes.isEmpty() || record.mealType in mealTypes
                val startMatch = start == null || record.date >= start
                val endMatch = end == null || record.date <= end
                mealMatch && startMatch && endMatch
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun toggleMealType(mealType: Int) {
        _selectedMealTypes.value = if (_selectedMealTypes.value.contains(mealType)) {
            _selectedMealTypes.value - mealType
        } else {
            _selectedMealTypes.value + mealType
        }
    }

    fun setDateRange(start: Long?, end: Long?) {
        _startDate.value = start
        _endDate.value = end
    }

    fun clearFilters() {
        _selectedMealTypes.value = emptySet()
        _startDate.value = null
        _endDate.value = null
    }

    fun deleteRecord(record: IntakeRecordEntity) {
        viewModelScope.launch {
            intakeRecordRepository.deleteRecord(record)
        }
    }
}
