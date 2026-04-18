package com.example.healthtracker.util

import java.util.Calendar

/**
 * 报表周期计算工具类
 * 核心规则：从一年的1号开始，每七天为一周，周次递增。
 */
object ReportPeriodManager {

    /**
     * 获取指定时间戳所在年份的自然第几周（1月1日为第一天，每7天一周）
     */
    fun getCustomWeekNumber(timestamp: Long): Int {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR) // 1月1日返回 1
        return ((dayOfYear - 1) / 7) + 1
    }

    /**
     * 获取指定年份
     */
    fun getYear(timestamp: Long): Int {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        return cal.get(Calendar.YEAR)
    }

    /**
     * 获取某年某定制周的第一天（0点）时间戳
     */
    fun getStartTimestampOfWeek(year: Int, weekNumber: Int): Long {
        val cal = Calendar.getInstance()
        cal.clear()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, Calendar.JANUARY)
        cal.set(Calendar.DAY_OF_MONTH, 1) // 1月1日
        cal.add(Calendar.DAY_OF_YEAR, (weekNumber - 1) * 7)
        return cal.timeInMillis
    }

    /**
     * 获取某年某定制周的最后一天（23:59:59.999）时间戳
     */
    fun getEndTimestampOfWeek(year: Int, weekNumber: Int): Long {
        val start = getStartTimestampOfWeek(year, weekNumber)
        val cal = Calendar.getInstance()
        cal.timeInMillis = start
        cal.add(Calendar.DAY_OF_YEAR, 6)
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    /**
     * 获取某周内特定一天（索引 0-6，0代表第一天）的时间戳
     */
    fun getTimestampForDayOfWeek(year: Int, weekNumber: Int, dayOfWeekIndex: Int): Long {
        require(dayOfWeekIndex in 0..6) { "dayOfWeekIndex must be between 0 and 6" }
        val start = getStartTimestampOfWeek(year, weekNumber)
        val cal = Calendar.getInstance()
        cal.timeInMillis = start
        cal.add(Calendar.DAY_OF_YEAR, dayOfWeekIndex)
        return cal.timeInMillis
    }

    /**
     * 格式化某周内特定一天的日期为 "M.d" 格式（如 5.6）
     */
    fun getFormattedDateForDayOfWeek(year: Int, weekNumber: Int, dayOfWeekIndex: Int): String {
        val timestamp = getTimestampForDayOfWeek(year, weekNumber, dayOfWeekIndex)
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        return "$month.$day"
    }
}
