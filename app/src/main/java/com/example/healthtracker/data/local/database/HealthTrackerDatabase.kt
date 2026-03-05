package com.example.healthtracker.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.healthtracker.data.local.dao.*
import com.example.healthtracker.data.local.entity.*

@Database(
    entities = [
        FoodEntity::class,
        IntakeRecordEntity::class,
        BodyRecordEntity::class,
        SleepRecordEntity::class,
        MealPlanEntity::class,
        MealPlanItemEntity::class,
        UserSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HealthTrackerDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun intakeRecordDao(): IntakeRecordDao
    abstract fun bodyRecordDao(): BodyRecordDao
    abstract fun sleepRecordDao(): SleepRecordDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun mealPlanItemDao(): MealPlanItemDao
    abstract fun userSettingsDao(): UserSettingsDao
}