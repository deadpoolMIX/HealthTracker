package com.example.healthtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户设置实体
 */
@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val id: Int = 1,                     // 单例
    // 基本信息
    val gender: Int = 0,                 // 性别：0-男, 1-女
    val birthDate: Long? = null,         // 出生日期
    val height: Double? = null,          // 身高 (cm)
    // 计算值
    val bmr: Double? = null,             // 基础代谢率
    val tdee: Double? = null,            // 每日总能量消耗
    val activityLevel: Int = 1,          // 活动水平：0-久坐, 1-轻度活动, 2-中度活动, 3-重度活动, 4-极重度活动
    // 目标设置
    val targetWeight: Double? = null,    // 目标体重
    val targetBodyFatRate: Double? = null, // 目标体脂率
    val targetCalories: Double? = null,  // 目标热量
    val targetCarbs: Double? = null,     // 目标碳水
    val targetProtein: Double? = null,   // 目标蛋白质
    val targetFat: Double? = null,       // 目标脂肪
    // 报表设置
    val showNutritionChart: Boolean = true,      // 显示营养素图表
    val showBodyChart: Boolean = true,            // 显示身体数据图表
    val showSleepChart: Boolean = true,           // 显示睡眠图表
    val defaultChartPeriod: Int = 0,              // 默认周期：0-天, 1-周, 2-月, 3-年
    // 主题设置
    val themeMode: Int = 0,              // 主题模式：0-跟随系统, 1-浅色, 2-深色
    val themeColor: Int = 0,             // 主题颜色：0-绿色, 1-蓝色, 2-紫色, 3-橙色, 4-红色
    // 首次使用标记
    val isFirstUse: Boolean = true,      // 是否首次使用
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)