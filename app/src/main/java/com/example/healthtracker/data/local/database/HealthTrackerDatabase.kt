package com.example.healthtracker.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
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

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 添加新字段
                db.execSQL("ALTER TABLE intake_records ADD COLUMN caloriesPer100g REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE intake_records ADD COLUMN carbsPer100g REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE intake_records ADD COLUMN proteinPer100g REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE intake_records ADD COLUMN fatPer100g REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE intake_records ADD COLUMN unit TEXT")
                db.execSQL("ALTER TABLE intake_records ADD COLUMN amountInUnit REAL")
                db.execSQL("ALTER TABLE intake_records ADD COLUMN gramsPerUnit REAL")
                db.execSQL("ALTER TABLE intake_records ADD COLUMN note TEXT")
            }
        }
    }
}