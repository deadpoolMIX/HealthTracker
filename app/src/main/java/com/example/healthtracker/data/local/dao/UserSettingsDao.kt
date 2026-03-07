package com.example.healthtracker.data.local.dao

import androidx.room.*
import com.example.healthtracker.data.local.entity.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 1")
    suspend fun getSettings(): UserSettingsEntity?

    @Query("SELECT * FROM user_settings WHERE id = 1")
    suspend fun getSettingsSync(): UserSettingsEntity?

    @Query("SELECT * FROM user_settings WHERE id = 1")
    fun getSettingsFlow(): Flow<UserSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: UserSettingsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: UserSettingsEntity)

    @Update
    suspend fun updateSettings(settings: UserSettingsEntity)

    @Query("UPDATE user_settings SET isFirstUse = 0 WHERE id = 1")
    suspend fun markAsNotFirstUse()

    @Query("UPDATE user_settings SET targetWeight = :weight WHERE id = 1")
    suspend fun updateTargetWeight(weight: Double?)

    @Query("UPDATE user_settings SET targetBodyFatRate = :rate WHERE id = 1")
    suspend fun updateTargetBodyFatRate(rate: Double?)

    @Query("UPDATE user_settings SET targetCalories = :calories WHERE id = 1")
    suspend fun updateTargetCalories(calories: Double?)

    @Query("UPDATE user_settings SET bmr = :bmr, tdee = :tdee WHERE id = 1")
    suspend fun updateBmrTdee(bmr: Double, tdee: Double)

    @Query("UPDATE user_settings SET themeMode = :mode WHERE id = 1")
    suspend fun updateThemeMode(mode: Int)

    @Query("UPDATE user_settings SET themeColor = :color WHERE id = 1")
    suspend fun updateThemeColor(color: Int)

    @Query("UPDATE user_settings SET navBarOrder = :order WHERE id = 1")
    suspend fun updateNavBarOrder(order: String)

    @Transaction
    suspend fun upsertThemeMode(mode: Int) {
        val settings = getSettings()
        if (settings == null) {
            insertSettings(com.example.healthtracker.data.local.entity.UserSettingsEntity(themeMode = mode))
        } else {
            updateThemeMode(mode)
        }
    }

    @Transaction
    suspend fun upsertThemeColor(color: Int) {
        val settings = getSettings()
        if (settings == null) {
            insertSettings(com.example.healthtracker.data.local.entity.UserSettingsEntity(themeColor = color))
        } else {
            updateThemeColor(color)
        }
    }

    @Transaction
    suspend fun updateUserInfo(
        gender: Int,
        age: Int,
        height: Double,
        weight: Double,
        activityLevel: Int,
        bmr: Double,
        tdee: Double
    ) {
        val settings = getSettings()
        if (settings == null) {
            insertSettings(
                com.example.healthtracker.data.local.entity.UserSettingsEntity(
                    gender = gender,
                    age = age,
                    height = height,
                    weight = weight,
                    activityLevel = activityLevel,
                    bmr = bmr,
                    tdee = tdee
                )
            )
        } else {
            // 更新所有字段
            updateSettings(
                settings.copy(
                    gender = gender,
                    age = age,
                    height = height,
                    weight = weight,
                    activityLevel = activityLevel,
                    bmr = bmr,
                    tdee = tdee
                )
            )
        }
    }

    // 报表设置
    @Query("UPDATE user_settings SET showNutritionChart = :show WHERE id = 1")
    suspend fun updateShowNutritionChart(show: Boolean)

    @Query("UPDATE user_settings SET showBodyChart = :show WHERE id = 1")
    suspend fun updateShowBodyChart(show: Boolean)

    @Query("UPDATE user_settings SET showSleepChart = :show WHERE id = 1")
    suspend fun updateShowSleepChart(show: Boolean)

    @Query("UPDATE user_settings SET defaultChartPeriod = :period WHERE id = 1")
    suspend fun updateDefaultChartPeriod(period: Int)

    @Transaction
    suspend fun updateReportSettings(
        showNutritionChart: Boolean,
        showBodyChart: Boolean,
        showSleepChart: Boolean,
        defaultChartPeriod: Int
    ) {
        val settings = getSettings()
        if (settings == null) {
            insertSettings(
                com.example.healthtracker.data.local.entity.UserSettingsEntity(
                    showNutritionChart = showNutritionChart,
                    showBodyChart = showBodyChart,
                    showSleepChart = showSleepChart,
                    defaultChartPeriod = defaultChartPeriod
                )
            )
        } else {
            updateSettings(
                settings.copy(
                    showNutritionChart = showNutritionChart,
                    showBodyChart = showBodyChart,
                    showSleepChart = showSleepChart,
                    defaultChartPeriod = defaultChartPeriod
                )
            )
        }
    }
}