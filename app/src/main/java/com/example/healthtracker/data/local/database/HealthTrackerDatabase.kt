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
        UserSettingsEntity::class,
        TestRecordEntity::class
    ],
    version = 12,
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
    abstract fun testRecordDao(): TestRecordDao

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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 添加收藏字段
                db.execSQL("ALTER TABLE foods ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 修改 meal_plan_items 表，移除外键约束并添加新字段
                // SQLite 不支持直接修改外键，需要重建表
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS meal_plan_items_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        planId INTEGER NOT NULL,
                        foodId INTEGER,
                        foodName TEXT NOT NULL,
                        amount REAL NOT NULL,
                        mealType INTEGER NOT NULL,
                        dayOfWeek INTEGER,
                        dayOfMonth INTEGER,
                        caloriesPer100g REAL NOT NULL DEFAULT 0,
                        carbsPer100g REAL NOT NULL DEFAULT 0,
                        proteinPer100g REAL NOT NULL DEFAULT 0,
                        fatPer100g REAL NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(planId) REFERENCES meal_plans(id) ON DELETE CASCADE
                    )
                """)
                db.execSQL("""
                    INSERT INTO meal_plan_items_new
                    SELECT id, planId, foodId, foodName, amount, mealType, dayOfWeek, dayOfMonth, 0, 0, 0, 0, createdAt
                    FROM meal_plan_items
                """)
                db.execSQL("DROP TABLE meal_plan_items")
                db.execSQL("ALTER TABLE meal_plan_items_new RENAME TO meal_plan_items")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_meal_plan_items_planId ON meal_plan_items(planId)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 添加主题设置字段
                db.execSQL("ALTER TABLE user_settings ADD COLUMN themeMode INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_settings ADD COLUMN themeColor INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 添加用户基本信息字段
                db.execSQL("ALTER TABLE user_settings ADD COLUMN age INTEGER")
                db.execSQL("ALTER TABLE user_settings ADD COLUMN weight REAL")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 添加导航栏顺序字段
                db.execSQL("ALTER TABLE user_settings ADD COLUMN navBarOrder TEXT NOT NULL DEFAULT '0,1,2,3'")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 添加食物单位字段
                db.execSQL("ALTER TABLE foods ADD COLUMN unit TEXT")
                db.execSQL("ALTER TABLE foods ADD COLUMN gramsPerUnit REAL")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 添加摄入记录的食物图标字段
                db.execSQL("ALTER TABLE intake_records ADD COLUMN foodIcon TEXT")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 添加报表设置字段
                db.execSQL("ALTER TABLE user_settings ADD COLUMN showNutritionChart INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE user_settings ADD COLUMN showBodyChart INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE user_settings ADD COLUMN showSleepChart INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE user_settings ADD COLUMN defaultChartPeriod INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 删除 isFavorite 字段，需要重建 foods 表
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS foods_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        category TEXT NOT NULL,
                        calories REAL NOT NULL,
                        carbohydrates REAL NOT NULL,
                        protein REAL NOT NULL,
                        fat REAL NOT NULL,
                        icon TEXT NOT NULL,
                        isCustom INTEGER NOT NULL DEFAULT 0,
                        unit TEXT,
                        gramsPerUnit REAL,
                        createdAt INTEGER NOT NULL
                    )
                """)
                db.execSQL("""
                    INSERT INTO foods_new (id, name, category, calories, carbohydrates, protein, fat, icon, isCustom, unit, gramsPerUnit, createdAt)
                    SELECT id, name, category, calories, carbohydrates, protein, fat, icon, isCustom, unit, gramsPerUnit, createdAt
                    FROM foods
                """)
                db.execSQL("DROP TABLE foods")
                db.execSQL("ALTER TABLE foods_new RENAME TO foods")
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 创建测试数据表
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS test_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        recordType TEXT NOT NULL,
                        date INTEGER NOT NULL,
                        dataJson TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """)
            }
        }
    }
}