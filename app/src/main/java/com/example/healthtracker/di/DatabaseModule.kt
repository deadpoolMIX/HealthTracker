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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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
        ).build().also { db ->
            // 在数据库创建后初始化默认食品数据
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                val foodDao = db.foodDao()
                if (foodDao.getAllFoods().toString().isEmpty()) {
                    foodDao.insertFoods(DefaultFoods.foods)
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