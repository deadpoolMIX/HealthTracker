package com.example.healthtracker.data.backup

import com.example.healthtracker.data.local.entity.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder

/**
 * 数据备份结构
 */
data class BackupData(
    val version: Int = 1,
    val exportTime: Long = System.currentTimeMillis(),
    val intakeRecords: List<IntakeRecordEntity>,
    val bodyRecords: List<BodyRecordEntity>,
    val sleepRecords: List<SleepRecordEntity>,
    val mealPlans: List<MealPlanEntity>,
    val mealPlanItems: List<MealPlanItemEntity>,
    val customFoods: List<FoodEntity>,
    val userSettings: UserSettingsEntity?
)

/**
 * 数据备份工具类
 */
object DataBackupUtil {
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    /**
     * 将数据序列化为JSON字符串
     */
    fun toJson(backupData: BackupData): String {
        return gson.toJson(backupData)
    }

    /**
     * 从JSON字符串反序列化数据
     */
    fun fromJson(json: String): BackupData {
        return gson.fromJson(json, BackupData::class.java)
    }

    /**
     * 生成备份文件名
     */
    fun generateFileName(): String {
        val time = System.currentTimeMillis()
        return "health_tracker_backup_$time.json"
    }
}