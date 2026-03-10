package com.example.healthtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 周期食物实体
 * 用于记录需要分多天吃完的食物
 */
@Entity(tableName = "cycle_foods")
data class CycleFoodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,                    // 食物名称
    val icon: String = "🍽️",             // 图标（emoji）
    // 总营养数据
    val totalCalories: Double,           // 总热量 (kcal)
    val totalCarbs: Double,              // 总碳水化合物 (g)
    val totalProtein: Double,            // 总蛋白质 (g)
    val totalFat: Double,                // 总脂肪 (g)
    // 剩余营养数据
    val remainingCalories: Double,       // 剩余热量
    val remainingCarbs: Double,          // 剩余碳水
    val remainingProtein: Double,        // 剩余蛋白质
    val remainingFat: Double,            // 剩余脂肪
    // 设置信息
    val expectedDays: Int,               // 预计天数
    val startDate: Long,                 // 开始日期
    val isActive: Boolean = true,        // 是否进行中
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * 获取每份的营养数据
     */
    fun getPortionCalories(): Double = if (expectedDays > 0) totalCalories / expectedDays else totalCalories
    fun getPortionCarbs(): Double = if (expectedDays > 0) totalCarbs / expectedDays else totalCarbs
    fun getPortionProtein(): Double = if (expectedDays > 0) totalProtein / expectedDays else totalProtein
    fun getPortionFat(): Double = if (expectedDays > 0) totalFat / expectedDays else totalFat

    /**
     * 获取剩余份数（向上取整，确保至少能吃一份）
     */
    fun getRemainingPortions(): Int {
        if (totalCalories <= 0) return 0
        val portion = getPortionCalories()
        if (portion <= 0) return 0
        return kotlin.math.ceil(remainingCalories / portion).toInt().coerceAtLeast(0)
    }
}