package com.example.healthtracker.util

import kotlin.math.roundToInt

/**
 * 健康计算工具类
 */
object HealthCalculator {

    /**
     * 活动水平系数
     */
    private val activityMultipliers = listOf(
        1.2,   // 久坐
        1.375, // 轻度活动
        1.55,  // 中度活动
        1.725, // 重度活动
        1.9    // 极重度活动
    )

    /**
     * 计算基础代谢率 (BMR) - 使用 Mifflin-St Jeor 公式
     * @param gender 性别 (0-男, 1-女)
     * @param weight 体重 (kg)
     * @param height 身高 (cm)
     * @param age 年龄
     * @return BMR (kcal/day)
     */
    fun calculateBMR(gender: Int, weight: Double, height: Double, age: Int): Double {
        return if (gender == 0) {
            // 男性: BMR = 10 × 体重 + 6.25 × 身高 - 5 × 年龄 + 5
            10 * weight + 6.25 * height - 5 * age + 5
        } else {
            // 女性: BMR = 10 × 体重 + 6.25 × 身高 - 5 × 年龄 - 161
            10 * weight + 6.25 * height - 5 * age - 161
        }
    }

    /**
     * 计算每日总能量消耗 (TDEE)
     * @param bmr 基础代谢率
     * @param activityLevel 活动水平 (0-4)
     * @return TDEE (kcal/day)
     */
    fun calculateTDEE(bmr: Double, activityLevel: Int): Double {
        val multiplier = activityMultipliers.getOrElse(activityLevel) { 1.2 }
        return bmr * multiplier
    }

    /**
     * 计算身体质量指数 (BMI)
     * @param weight 体重 (kg)
     * @param height 身高 (cm)
     * @return BMI
     */
    fun calculateBMI(weight: Double, height: Double): Double {
        val heightInMeters = height / 100
        return weight / (heightInMeters * heightInMeters)
    }

    /**
     * 获取BMI分类
     * @param bmi BMI值
     * @return 分类描述
     */
    fun getBMICategory(bmi: Double): String {
        return when {
            bmi < 18.5 -> "偏瘦"
            bmi < 24 -> "正常"
            bmi < 28 -> "超重"
            else -> "肥胖"
        }
    }

    /**
     * 计算睡眠时长（分钟）
     * @param sleepTime 入睡时间戳
     * @param wakeTime 起床时间戳
     * @return 睡眠时长（分钟）
     */
    fun calculateSleepDuration(sleepTime: Long, wakeTime: Long): Long {
        return (wakeTime - sleepTime) / 60000
    }

    /**
     * 计算热量占比百分比
     * @param consumed 已摄入热量
     * @param target 目标热量
     * @return 百分比 (0-100)
     */
    fun calculateCaloriePercentage(consumed: Double, target: Double): Int {
        if (target <= 0) return 0
        return ((consumed / target) * 100).roundToInt().coerceIn(0, 100)
    }

    /**
     * 计算营养素热量
     * @param carbs 碳水化合物 (g)
     * @param protein 蛋白质 (g)
     * @param fat 脂肪 (g)
     * @return 总热量 (kcal)
     */
    fun calculateCaloriesFromMacros(carbs: Double, protein: Double, fat: Double): Double {
        return carbs * 4 + protein * 4 + fat * 9
    }

    /**
     * 计算列表的中位数
     */
    fun <T : Number> calculateMedian(values: List<T>): Double {
        if (values.isEmpty()) return 0.0
        val sorted = values.map { it.toDouble() }.sorted()
        val size = sorted.size
        return if (size % 2 == 0) {
            (sorted[size / 2 - 1] + sorted[size / 2]) / 2
        } else {
            sorted[size / 2]
        }
    }

    /**
     * 计算列表的平均值
     */
    fun <T : Number> calculateAverage(values: List<T>): Double {
        if (values.isEmpty()) return 0.0
        return values.map { it.toDouble() }.average()
    }
}