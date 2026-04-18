package com.example.healthtracker.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class ReportPeriodManagerTest {

    @Test
    fun testGetCustomWeekNumber() {
        val cal = Calendar.getInstance()
        cal.clear()
        
        // 1月1日应该是第一周
        cal.set(2026, Calendar.JANUARY, 1)
        assertEquals(1, ReportPeriodManager.getCustomWeekNumber(cal.timeInMillis))
        
        // 1月7日应该是第一周的最后一天
        cal.set(2026, Calendar.JANUARY, 7)
        assertEquals(1, ReportPeriodManager.getCustomWeekNumber(cal.timeInMillis))
        
        // 1月8日应该是第二周的第一天
        cal.set(2026, Calendar.JANUARY, 8)
        assertEquals(2, ReportPeriodManager.getCustomWeekNumber(cal.timeInMillis))
    }

    @Test
    fun testGetFormattedDateForDayOfWeek() {
        // 第一周的第一天：索引0
        assertEquals("1.1", ReportPeriodManager.getFormattedDateForDayOfWeek(2026, 1, 0))
        // 第一周的第七天：索引6
        assertEquals("1.7", ReportPeriodManager.getFormattedDateForDayOfWeek(2026, 1, 6))
        // 第二周的第一天：索引0
        assertEquals("1.8", ReportPeriodManager.getFormattedDateForDayOfWeek(2026, 2, 0))
    }
    
    @Test
    fun testGetStartAndEndTimestamp() {
        val start = ReportPeriodManager.getStartTimestampOfWeek(2026, 2) // 第二周
        val cal = Calendar.getInstance()
        cal.timeInMillis = start
        assertEquals(2026, cal.get(Calendar.YEAR))
        assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH))
        assertEquals(8, cal.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY))
        
        val end = ReportPeriodManager.getEndTimestampOfWeek(2026, 2)
        cal.timeInMillis = end
        assertEquals(14, cal.get(Calendar.DAY_OF_MONTH))
        assertEquals(23, cal.get(Calendar.HOUR_OF_DAY))
    }
}
