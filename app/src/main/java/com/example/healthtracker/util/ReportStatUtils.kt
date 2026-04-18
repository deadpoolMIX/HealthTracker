package com.example.healthtracker.util

/**
 * 报表统计工具类
 */
object ReportStatUtils {
    
    /**
     * 计算中位数。
     * 如果列表大小为偶数，取中间两数的平均值。
     */
    inline fun <T> Iterable<T>.medianBy(selector: (T) -> Float): Float {
        val sorted = this.map(selector).sorted()
        if (sorted.isEmpty()) return 0f
        val size = sorted.size
        return if (size % 2 == 0) {
            (sorted[size / 2 - 1] + sorted[size / 2]) / 2f
        } else {
            sorted[size / 2]
        }
    }

    /**
     * 计算基于实际记录天数的均值。
     * @param daySelector 用于区分不同日期的选择器（例如获取该记录所在当天的0点时间戳作为Key）
     * @param valueSelector 获取要计算的值
     */
    inline fun <T, K> Iterable<T>.averageByRealDays(
        daySelector: (T) -> K,
        valueSelector: (T) -> Float
    ): Float {
        val groups = this.groupBy(daySelector)
        val validDays = groups.size
        if (validDays == 0) return 0f
        val sum = this.sumOf { valueSelector(it).toDouble() }.toFloat()
        return sum / validDays
    }
    
    /**
     * 计算变化率（百分比）
     * 返回如 +5.0 或 -2.1，以 Float 表示
     */
    fun calculatePercentageChange(oldValue: Float, newValue: Float): Float {
        if (oldValue == 0f) return 0f
        return ((newValue - oldValue) / oldValue) * 100f
    }
}
