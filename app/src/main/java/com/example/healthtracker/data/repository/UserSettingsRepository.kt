package com.example.healthtracker.data.repository

import com.example.healthtracker.data.local.dao.UserSettingsDao
import com.example.healthtracker.data.local.entity.UserSettingsEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSettingsRepository @Inject constructor(
    private val userSettingsDao: UserSettingsDao
) {
    suspend fun getSettings(): UserSettingsEntity? = userSettingsDao.getSettings()

    fun getSettingsFlow(): Flow<UserSettingsEntity?> = userSettingsDao.getSettingsFlow()

    suspend fun saveSettings(settings: UserSettingsEntity) =
        userSettingsDao.saveSettings(settings)

    suspend fun updateSettings(settings: UserSettingsEntity) =
        userSettingsDao.updateSettings(settings)

    suspend fun markAsNotFirstUse() = userSettingsDao.markAsNotFirstUse()

    suspend fun updateTargetWeight(weight: Double?) =
        userSettingsDao.updateTargetWeight(weight)

    suspend fun updateTargetBodyFatRate(rate: Double?) =
        userSettingsDao.updateTargetBodyFatRate(rate)

    suspend fun updateTargetCalories(calories: Double?) =
        userSettingsDao.updateTargetCalories(calories)

    suspend fun updateBmrTdee(bmr: Double, tdee: Double) =
        userSettingsDao.updateBmrTdee(bmr, tdee)

    suspend fun updateThemeMode(mode: Int) =
        userSettingsDao.upsertThemeMode(mode)

    suspend fun updateThemeColor(color: Int) =
        userSettingsDao.upsertThemeColor(color)

    suspend fun updateUserInfo(
        gender: Int,
        age: Int,
        height: Double,
        weight: Double,
        activityLevel: Int,
        bmr: Double,
        tdee: Double
    ) = userSettingsDao.updateUserInfo(gender, age, height, weight, activityLevel, bmr, tdee)

    // 报表设置
    suspend fun updateShowNutritionChart(show: Boolean) =
        userSettingsDao.updateShowNutritionChart(show)

    suspend fun updateShowBodyChart(show: Boolean) =
        userSettingsDao.updateShowBodyChart(show)

    suspend fun updateShowSleepChart(show: Boolean) =
        userSettingsDao.updateShowSleepChart(show)

    suspend fun updateDefaultChartPeriod(period: Int) =
        userSettingsDao.updateDefaultChartPeriod(period)

    suspend fun updateReportSettings(
        showNutritionChart: Boolean,
        showBodyChart: Boolean,
        showSleepChart: Boolean,
        defaultChartPeriod: Int
    ) = userSettingsDao.updateReportSettings(
        showNutritionChart,
        showBodyChart,
        showSleepChart,
        defaultChartPeriod
    )
}