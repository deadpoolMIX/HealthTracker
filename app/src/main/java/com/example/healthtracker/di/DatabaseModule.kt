package com.example.healthtracker.di

import android.content.Context
import androidx.room.Room
import com.example.healthtracker.data.local.database.DefaultFoods
import com.example.healthtracker.data.local.database.HealthTrackerDatabase
import com.example.healthtracker.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): HealthTrackerDatabase {
        return Room.databaseBuilder(
            context,
            HealthTrackerDatabase::class.java,
            "health_tracker_db"
        )
            .addMigrations(HealthTrackerDatabase.MIGRATION_1_2)
            .build().also { db ->
                // 在数据库创建后初始化默认食品数据
                runBlocking {
                    try {
                        val foodDao = db.foodDao()
                        // 使用同步方式检查，Flow 不能直接用于条件判断
                        val foodCount = foodDao.getFoodCount()
                        if (foodCount == 0) {
                            foodDao.insertFoods(DefaultFoods.foods)
                        }
                    } catch (e: Exception) {
                        // 忽略初始化错误，不影响应用启动
                        e.printStackTrace()
                    }
                }
            }
    }

    @Provides
    fun provideFoodDao(database: HealthTrackerDatabase): FoodDao {
        return database.foodDao()
    }

    @Provides
    fun provideIntakeRecordDao(database: HealthTrackerDatabase): IntakeRecordDao {
        return database.intakeRecordDao()
    }

    @Provides
    fun provideBodyRecordDao(database: HealthTrackerDatabase): BodyRecordDao {
        return database.bodyRecordDao()
    }

    @Provides
    fun provideSleepRecordDao(database: HealthTrackerDatabase): SleepRecordDao {
        return database.sleepRecordDao()
    }

    @Provides
    fun provideMealPlanDao(database: HealthTrackerDatabase): MealPlanDao {
        return database.mealPlanDao()
    }

    @Provides
    fun provideMealPlanItemDao(database: HealthTrackerDatabase): MealPlanItemDao {
        return database.mealPlanItemDao()
    }

    @Provides
    fun provideUserSettingsDao(database: HealthTrackerDatabase): UserSettingsDao {
        return database.userSettingsDao()
    }
}