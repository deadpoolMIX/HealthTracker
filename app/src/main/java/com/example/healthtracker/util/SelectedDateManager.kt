package com.example.healthtracker.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 选中日期管理器（全局单例）
 * 用于在日历页面和首页之间同步选中的日期
 */
object SelectedDateManager {
    private val _selectedDate = MutableStateFlow(DateTimeUtils.getStartOfDay())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    /**
     * 更新选中的日期
     */
    fun setSelectedDate(date: Long) {
        _selectedDate.value = DateTimeUtils.getStartOfDay(date)
    }

    /**
     * 重置为今天
     */
    fun resetToToday() {
        _selectedDate.value = DateTimeUtils.getStartOfDay()
    }

    /**
     * 获取当前选中的日期
     */
    fun getSelectedDate(): Long = _selectedDate.value
}