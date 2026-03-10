package com.example.healthtracker.di

import android.content.Context
import androidx.room.Room
import com.example.healthtracker.data.local.database.HealthTrackerDatabase
import com.example.healthtracker.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
            .addMigrations(
                HealthTrackerDatabase.MIGRATION_1_2,
                HealthTrackerDatabase.MIGRATION_2_3,
                HealthTrackerDatabase.MIGRATION_3_4,
                HealthTrackerDatabase.MIGRATION_4_5,
                HealthTrackerDatabase.MIGRATION_5_6,
                HealthTrackerDatabase.MIGRATION_6_7,
                HealthTrackerDatabase.MIGRATION_7_8,
                HealthTrackerDatabase.MIGRATION_8_9,
                HealthTrackerDatabase.MIGRATION_9_10,
                HealthTrackerDatabase.MIGRATION_10_11,
                HealthTrackerDatabase.MIGRATION_11_12
            )
            .fallbackToDestructiveMigration()
            .build()
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

    @Provides
    fun provideTestRecordDao(database: HealthTrackerDatabase): TestRecordDao {
        return database.testRecordDao()
    }
}