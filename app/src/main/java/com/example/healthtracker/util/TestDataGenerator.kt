package com.example.healthtracker.util

import com.example.healthtracker.data.local.entity.BodyRecordEntity
import com.example.healthtracker.data.local.entity.FoodEntity
import com.example.healthtracker.data.local.entity.IntakeRecordEntity
import com.example.healthtracker.data.local.entity.SleepRecordEntity
import java.util.Calendar
import kotlin.random.Random

/**
 * 测试数据生成器
 * 用于生成模拟的用户数据，方便测试报表功能
 * 生成最近一个月（30天）的所有数据（摄入、体重、睡眠）
 * 从食物库中选择食物生成摄入记录
 */
object TestDataGenerator {

    /**
     * 生成最近一个月（30天）的摄入记录
     * @param foods 食物库中的食物列表
     */
    fun generateIntakeRecords(foods: List<FoodEntity>): List<IntakeRecordEntity> {
        if (foods.isEmpty()) return emptyList()

        val records = mutableListOf<IntakeRecordEntity>()
        val today = System.currentTimeMillis()

        for (dayOffset in 0..29) {
            val date = getStartOfDay(today, -dayOffset)

            // 每天三餐 + 可能的加餐
            val mealTypes = listOf(0, 1, 2) // 早餐、午餐、晚餐

            mealTypes.forEach { mealType ->
                // 每顿1-3种食物，从食物库中随机选择
                val foodCount = Random.nextInt(1, 4)
                val selectedFoods = foods.shuffled().take(foodCount)

                selectedFoods.forEach { food ->
                    val amount = Random.nextDouble(80.0, 300.0)
                    val actualCalories = (amount / 100.0) * food.calories
                    val actualCarbs = (amount / 100.0) * food.carbohydrates
                    val actualProtein = (amount / 100.0) * food.protein
                    val actualFat = (amount / 100.0) * food.fat

                    // 使用食物库中的图标
                    val foodIcon = food.icon.ifEmpty { null }

                    records.add(
                        IntakeRecordEntity(
                            foodId = food.id,
                            foodName = food.name,
                            foodIcon = foodIcon,
                            date = date,
                            amount = amount,
                            calories = actualCalories,
                            carbohydrates = actualCarbs,
                            protein = actualProtein,
                            fat = actualFat,
                            mealType = mealType,
                            caloriesPer100g = food.calories,
                            carbsPer100g = food.carbohydrates,
                            proteinPer100g = food.protein,
                            fatPer100g = food.fat,
                            unit = "克",
                            createdAt = date + Random.nextLong(28800000, 82800000) // 8:00 - 23:00
                        )
                    )
                }
            }

            // 随机加餐（50%概率）
            if (Random.nextBoolean() && foods.isNotEmpty()) {
                // 选择适合做加餐的食物（水果、酸奶等）
                val snackKeywords = listOf("果", "奶", "饮", "蕉", "苹", "橙", "葡", "莓")
                val snackFoods = foods.filter { food ->
                    snackKeywords.any { keyword -> food.name.contains(keyword) }
                }.ifEmpty { foods }

                if (snackFoods.isNotEmpty()) {
                    val snack = snackFoods.random()
                    val amount = Random.nextDouble(100.0, 200.0)

                    records.add(
                        IntakeRecordEntity(
                            foodId = snack.id,
                            foodName = snack.name,
                            foodIcon = snack.icon.ifEmpty { null },
                            date = date,
                            amount = amount,
                            calories = (amount / 100.0) * snack.calories,
                            carbohydrates = (amount / 100.0) * snack.carbohydrates,
                            protein = (amount / 100.0) * snack.protein,
                            fat = (amount / 100.0) * snack.fat,
                            mealType = 3, // 加餐
                            caloriesPer100g = snack.calories,
                            carbsPer100g = snack.carbohydrates,
                            proteinPer100g = snack.protein,
                            fatPer100g = snack.fat,
                            unit = "克",
                            createdAt = date + Random.nextLong(36000000, 61200000) // 10:00 - 17:00
                        )
                    )
                }
            }
        }

        return records
    }

    /**
     * 生成最近一个月（30天）的身体数据记录
     */
    fun generateBodyRecords(baseWeight: Double = 70.0): List<BodyRecordEntity> {
        val records = mutableListOf<BodyRecordEntity>()
        val today = System.currentTimeMillis()
        var currentWeight = baseWeight

        for (dayOffset in 29 downTo 0) {
            val date = getStartOfDay(today, -dayOffset)

            // 体重轻微波动，模拟真实数据
            currentWeight += Random.nextDouble(-0.3, 0.3)
            val weight = currentWeight.coerceIn(baseWeight - 3, baseWeight + 3)

            // 体脂率 18-22% 波动
            val bodyFat = 20.0 + Random.nextDouble(-2.0, 2.0)

            // 肌肉量根据体重计算
            val muscle = weight * 0.42 + Random.nextDouble(-0.5, 0.5)

            // 三围数据
            val chest = 90.0 + Random.nextDouble(-1.0, 1.0)
            val waist = 78.0 + Random.nextDouble(-1.0, 1.0)
            val hip = 95.0 + Random.nextDouble(-1.0, 1.0)

            records.add(
                BodyRecordEntity(
                    date = date,
                    weight = String.format("%.1f", weight).toDouble(),
                    bodyFatRate = String.format("%.1f", bodyFat).toDouble(),
                    muscleMass = String.format("%.1f", muscle).toDouble(),
                    chest = String.format("%.1f", chest).toDouble(),
                    waist = String.format("%.1f", waist).toDouble(),
                    hip = String.format("%.1f", hip).toDouble(),
                    createdAt = date + Random.nextLong(25200000, 36000000) // 7:00 - 10:00
                )
            )
        }

        return records
    }

    /**
     * 生成最近一个月（30天）的睡眠记录
     */
    fun generateSleepRecords(): List<SleepRecordEntity> {
        val records = mutableListOf<SleepRecordEntity>()
        val today = System.currentTimeMillis()

        for (dayOffset in 29 downTo 0) {
            val date = getStartOfDay(today, -dayOffset)

            // 入睡时间：22:00 - 01:00
            val sleepHour = Random.nextInt(22, 26) % 24
            val sleepMinute = Random.nextInt(0, 60)

            // 睡眠时长：6-8.5小时
            val sleepDuration = Random.nextInt(360, 510) // 分钟

            // 计算入睡时间
            val sleepCalendar = Calendar.getInstance()
            sleepCalendar.timeInMillis = date
            if (sleepHour >= 22) {
                // 22:00 - 23:59 入睡，是当天晚上
                sleepCalendar.set(Calendar.HOUR_OF_DAY, sleepHour)
            } else {
                // 00:00 - 01:00 入睡，是前一天晚上
                sleepCalendar.add(Calendar.DAY_OF_YEAR, -1)
                sleepCalendar.set(Calendar.HOUR_OF_DAY, sleepHour)
            }
            sleepCalendar.set(Calendar.MINUTE, sleepMinute)
            sleepCalendar.set(Calendar.SECOND, 0)
            sleepCalendar.set(Calendar.MILLISECOND, 0)

            val sleepTime = sleepCalendar.timeInMillis
            val wakeTime = sleepTime + sleepDuration * 60 * 1000L

            records.add(
                SleepRecordEntity(
                    date = date,
                    sleepTime = sleepTime,
                    wakeTime = wakeTime,
                    duration = sleepDuration.toLong(),
                    createdAt = date + Random.nextLong(0, 3600000)
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