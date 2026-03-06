package com.example.healthtracker.util

import com.example.healthtracker.data.local.entity.BodyRecordEntity
import com.example.healthtracker.data.local.entity.IntakeRecordEntity
import com.example.healthtracker.data.local.entity.SleepRecordEntity
import java.util.Calendar
import kotlin.random.Random

/**
 * 测试数据生成器
 * 用于生成模拟的用户数据，方便测试报表功能
 */
object TestDataGenerator {

    // 食物模板数据类
    private data class FoodTemplate(
        val name: String,
        val calories: Double,
        val carbs: Double,
        val protein: Double,
        val fat: Double
    )

    // 常见食物数据
    private val foodTemplates = listOf(
        FoodTemplate("米饭", 116.0, 25.9, 2.6, 0.3),
        FoodTemplate("馒头", 223.0, 47.0, 7.0, 1.1),
        FoodTemplate("鸡蛋", 144.0, 0.1, 13.3, 8.8),
        FoodTemplate("牛奶", 54.0, 3.4, 3.0, 3.2),
        FoodTemplate("苹果", 52.0, 13.5, 0.2, 0.2),
        FoodTemplate("香蕉", 93.0, 20.7, 1.2, 0.2),
        FoodTemplate("鸡胸肉", 133.0, 0.0, 19.4, 5.0),
        FoodTemplate("牛肉", 106.0, 0.1, 20.2, 2.3),
        FoodTemplate("西兰花", 36.0, 4.1, 4.1, 0.4),
        FoodTemplate("豆腐", 76.0, 1.8, 8.1, 3.7),
        FoodTemplate("燕麦", 367.0, 61.6, 14.2, 6.7),
        FoodTemplate("酸奶", 72.0, 9.3, 2.5, 2.7),
        FoodTemplate("西红柿", 15.0, 3.3, 0.9, 0.2),
        FoodTemplate("黄瓜", 15.0, 2.9, 0.8, 0.2),
        FoodTemplate("鱼", 109.0, 0.0, 17.6, 4.1)
    )

    /**
     * 生成指定天数的摄入记录
     * @param days 天数
     * @param startDate 开始日期的时间戳（默认为今天）
     */
    fun generateIntakeRecords(days: Int, startDate: Long = System.currentTimeMillis()): List<IntakeRecordEntity> {
        val records = mutableListOf<IntakeRecordEntity>()

        for (dayOffset in 0 until days) {
            val date = getStartOfDay(startDate, -dayOffset)

            // 每天随机3-5顿
            val mealCount = Random.nextInt(3, 6)

            for (i in 0 until mealCount) {
                // 随机选择餐次
                val mealType = if (i == 0) 0 // 第一顿是早餐
                    else if (i == 1 && mealCount > 3) 1 // 第二顿可能是午餐
                    else Random.nextInt(0, 4)

                // 每顿1-3种食物
                val foodCount = Random.nextInt(1, 4)
                val selectedFoods = foodTemplates.shuffled().take(foodCount)

                selectedFoods.forEach { food ->
                    // 随机份量 50-300g
                    val amount = Random.nextDouble(50.0, 300.0)
                    val actualCalories = (amount / 100.0) * food.calories
                    val actualCarbs = (amount / 100.0) * food.carbs
                    val actualProtein = (amount / 100.0) * food.protein
                    val actualFat = (amount / 100.0) * food.fat

                    records.add(
                        IntakeRecordEntity(
                            foodName = food.name,
                            date = date,
                            amount = amount,
                            calories = actualCalories,
                            carbohydrates = actualCarbs,
                            protein = actualProtein,
                            fat = actualFat,
                            mealType = mealType,
                            caloriesPer100g = food.calories,
                            carbsPer100g = food.carbs,
                            proteinPer100g = food.protein,
                            fatPer100g = food.fat,
                            createdAt = date + Random.nextLong(0, 86400000)
                        )
                    )
                }
            }
        }

        return records
    }

    /**
     * 生成指定天数的身体数据记录
     * @param days 天数
     * @param startDate 开始日期的时间戳
     * @param baseWeight 基准体重
     */
    fun generateBodyRecords(
        days: Int,
        startDate: Long = System.currentTimeMillis(),
        baseWeight: Double = 70.0
    ): List<BodyRecordEntity> {
        val records = mutableListOf<BodyRecordEntity>()
        var currentWeight = baseWeight

        for (dayOffset in 0 until days) {
            val date = getStartOfDay(startDate, -dayOffset)

            // 体重轻微波动
            currentWeight += Random.nextDouble(-0.3, 0.3)
            val weight = currentWeight.coerceIn(baseWeight - 3, baseWeight + 3)

            // 体脂率 15-25% 波动
            val bodyFat = 18.0 + Random.nextDouble(-3.0, 3.0)

            // 肌肉量根据体重计算
            val muscle = weight * 0.4 + Random.nextDouble(-1.0, 1.0)

            // 三围数据
            val chest = 90.0 + Random.nextDouble(-2.0, 2.0)
            val waist = 75.0 + Random.nextDouble(-2.0, 2.0)
            val hip = 95.0 + Random.nextDouble(-2.0, 2.0)

            records.add(
                BodyRecordEntity(
                    date = date,
                    weight = String.format("%.1f", weight).toDouble(),
                    bodyFatRate = String.format("%.1f", bodyFat).toDouble(),
                    muscleMass = String.format("%.1f", muscle).toDouble(),
                    chest = String.format("%.1f", chest).toDouble(),
                    waist = String.format("%.1f", waist).toDouble(),
                    hip = String.format("%.1f", hip).toDouble(),
                    createdAt = date + Random.nextLong(0, 86400000)
                )
            )
        }

        return records
    }

    /**
     * 生成指定天数的睡眠记录
     * @param days 天数
     * @param startDate 开始日期的时间戳
     */
    fun generateSleepRecords(
        days: Int,
        startDate: Long = System.currentTimeMillis()
    ): List<SleepRecordEntity> {
        val records = mutableListOf<SleepRecordEntity>()

        for (dayOffset in 0 until days) {
            val date = getStartOfDay(startDate, -dayOffset)

            // 入睡时间：21:00 - 01:00
            val sleepHour = Random.nextInt(21, 25) % 24
            val sleepMinute = Random.nextInt(0, 60)

            // 睡眠时长：5-9小时
            val sleepDuration = Random.nextInt(300, 540) // 分钟

            // 计算起床时间
            val sleepCalendar = Calendar.getInstance()
            sleepCalendar.timeInMillis = date
            sleepCalendar.set(Calendar.HOUR_OF_DAY, sleepHour)
            sleepCalendar.set(Calendar.MINUTE, sleepMinute)
            if (sleepHour < 12) {
                // 如果入睡时间在凌晨，说明是前一天晚上
                sleepCalendar.add(Calendar.DAY_OF_YEAR, -1)
            }

            val sleepTime = sleepCalendar.timeInMillis
            val wakeTime = sleepTime + sleepDuration * 60 * 1000

            records.add(
                SleepRecordEntity(
                    date = date,
                    sleepTime = sleepTime,
                    wakeTime = wakeTime,
                    duration = sleepDuration.toLong(),
                    createdAt = date + Random.nextLong(0, 86400000)
                )
            )
        }

        return records
    }

    private fun getStartOfDay(timestamp: Long, dayOffset: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.add(Calendar.DAY_OF_YEAR, dayOffset)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}