package com.example.healthtracker.util

import com.example.healthtracker.data.local.entity.BodyRecordEntity
import com.example.healthtracker.data.local.entity.IntakeRecordEntity
import com.example.healthtracker.data.local.entity.SleepRecordEntity
import java.util.Calendar
import kotlin.random.Random

/**
 * 测试数据生成器
 * 用于生成模拟的用户数据，方便测试报表功能
 * 生成最近一周的所有数据（摄入、体重、睡眠）
 */
object TestDataGenerator {

    // 食物模板数据类
    private data class FoodTemplate(
        val name: String,
        val calories: Double,
        val carbs: Double,
        val protein: Double,
        val fat: Double,
        val icon: String
    )

    // 常见食物数据（带图标）
    private val foodTemplates = listOf(
        FoodTemplate("米饭", 116.0, 25.9, 2.6, 0.3, "🍚"),
        FoodTemplate("馒头", 223.0, 47.0, 7.0, 1.1, "🥟"),
        FoodTemplate("鸡蛋", 144.0, 0.1, 13.3, 8.8, "🥚"),
        FoodTemplate("牛奶", 54.0, 3.4, 3.0, 3.2, "🥛"),
        FoodTemplate("苹果", 52.0, 13.5, 0.2, 0.2, "🍎"),
        FoodTemplate("香蕉", 93.0, 20.7, 1.2, 0.2, "🍌"),
        FoodTemplate("鸡胸肉", 133.0, 0.0, 19.4, 5.0, "🍗"),
        FoodTemplate("牛肉", 106.0, 0.1, 20.2, 2.3, "🥩"),
        FoodTemplate("西兰花", 36.0, 4.1, 4.1, 0.4, "🥦"),
        FoodTemplate("豆腐", 76.0, 1.8, 8.1, 3.7, "🫘"),
        FoodTemplate("燕麦", 367.0, 61.6, 14.2, 6.7, "🥣"),
        FoodTemplate("酸奶", 72.0, 9.3, 2.5, 2.7, "🥛"),
        FoodTemplate("西红柿", 15.0, 3.3, 0.9, 0.2, "🍅"),
        FoodTemplate("黄瓜", 15.0, 2.9, 0.8, 0.2, "🥒"),
        FoodTemplate("鱼", 109.0, 0.0, 17.6, 4.1, "🐟"),
        FoodTemplate("面包", 265.0, 50.0, 8.0, 3.0, "🍞"),
        FoodTemplate("豆浆", 31.0, 1.2, 1.8, 0.7, "🥛"),
        FoodTemplate("橙子", 47.0, 11.8, 0.7, 0.2, "🍊"),
        FoodTemplate("葡萄", 43.0, 10.3, 0.4, 0.4, "🍇"),
        FoodTemplate("土豆", 77.0, 17.2, 2.0, 0.2, "🥔")
    )

    /**
     * 生成最近一周（7天）的摄入记录
     */
    fun generateIntakeRecords(): List<IntakeRecordEntity> {
        val records = mutableListOf<IntakeRecordEntity>()
        val today = System.currentTimeMillis()

        for (dayOffset in 0..6) {
            val date = getStartOfDay(today, -dayOffset)

            // 每天三餐 + 可能的加餐
            val mealTypes = listOf(0, 1, 2) // 早餐、午餐、晚餐

            mealTypes.forEach { mealType ->
                // 每顿1-3种食物
                val foodCount = Random.nextInt(1, 4)
                val selectedFoods = foodTemplates.shuffled().take(foodCount)

                selectedFoods.forEach { food ->
                    val amount = Random.nextDouble(80.0, 300.0)
                    val actualCalories = (amount / 100.0) * food.calories
                    val actualCarbs = (amount / 100.0) * food.carbs
                    val actualProtein = (amount / 100.0) * food.protein
                    val actualFat = (amount / 100.0) * food.fat

                    records.add(
                        IntakeRecordEntity(
                            foodName = food.name,
                            foodIcon = food.icon,
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
                            unit = "克",
                            createdAt = date + Random.nextLong(28800000, 82800000) // 8:00 - 23:00
                        )
                    )
                }
            }

            // 随机加餐（50%概率）
            if (Random.nextBoolean()) {
                val snack = foodTemplates.filter {
                    it.name in listOf("苹果", "香蕉", "酸奶", "橙子", "葡萄")
                }.random()
                val amount = Random.nextDouble(100.0, 200.0)

                records.add(
                    IntakeRecordEntity(
                        foodName = snack.name,
                        foodIcon = snack.icon,
                        date = date,
                        amount = amount,
                        calories = (amount / 100.0) * snack.calories,
                        carbohydrates = (amount / 100.0) * snack.carbs,
                        protein = (amount / 100.0) * snack.protein,
                        fat = (amount / 100.0) * snack.fat,
                        mealType = 3, // 加餐
                        caloriesPer100g = snack.calories,
                        carbsPer100g = snack.carbs,
                        proteinPer100g = snack.protein,
                        fatPer100g = snack.fat,
                        unit = "克",
                        createdAt = date + Random.nextLong(36000000, 61200000) // 10:00 - 17:00
                    )
                )
            }
        }

        return records
    }

    /**
     * 生成最近一周（7天）的身体数据记录
     */
    fun generateBodyRecords(baseWeight: Double = 70.0): List<BodyRecordEntity> {
        val records = mutableListOf<BodyRecordEntity>()
        val today = System.currentTimeMillis()
        var currentWeight = baseWeight

        for (dayOffset in 6 downTo 0) {
            val date = getStartOfDay(today, -dayOffset)

            // 体重轻微波动，模拟真实数据
            currentWeight += Random.nextDouble(-0.2, 0.2)
            val weight = currentWeight.coerceIn(baseWeight - 2, baseWeight + 2)

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
     * 生成最近一周（7天）的睡眠记录
     */
    fun generateSleepRecords(): List<SleepRecordEntity> {
        val records = mutableListOf<SleepRecordEntity>()
        val today = System.currentTimeMillis()

        for (dayOffset in 6 downTo 0) {
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