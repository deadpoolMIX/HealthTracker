package com.example.healthtracker.data.backup

import android.content.Context
import android.net.Uri
import com.example.healthtracker.data.local.dao.*
import com.example.healthtracker.data.local.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

/**
 * 数据备份管理器
 */
class DataBackupManager(
    private val context: Context,
    private val foodDao: FoodDao,
    private val intakeRecordDao: IntakeRecordDao,
    private val bodyRecordDao: BodyRecordDao,
    private val sleepRecordDao: SleepRecordDao,
    private val mealPlanDao: MealPlanDao,
    private val mealPlanItemDao: MealPlanItemDao,
    private val userSettingsDao: UserSettingsDao
) {
    /**
     * 导出所有数据
     */
    suspend fun exportData(): BackupData = withContext(Dispatchers.IO) {
        val intakeRecords = intakeRecordDao.getRecordsBetweenSync(0, Long.MAX_VALUE)
        val bodyRecords = bodyRecordDao.getRecordsBetweenSync(0, Long.MAX_VALUE)
        val sleepRecords = sleepRecordDao.getRecordsBetweenSync(0, Long.MAX_VALUE)
        val mealPlans = mealPlanDao.getAllPlans().first()
        val mealPlanItems = mealPlans.flatMap { mealPlanItemDao.getItemsByPlanIdSync(it.id) }
        val customFoods = foodDao.getCustomFoods().first()
        val userSettings = userSettingsDao.getSettingsSync()

        BackupData(
            intakeRecords = intakeRecords,
            bodyRecords = bodyRecords,
            sleepRecords = sleepRecords,
            mealPlans = mealPlans,
            mealPlanItems = mealPlanItems,
            customFoods = customFoods,
            userSettings = userSettings
        )
    }

    /**
     * 导出数据到文件
     */
    suspend fun exportToFile(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val backupData = exportData()
            val json = DataBackupUtil.toJson(backupData)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream, "UTF-8").use { writer ->
                    writer.write(json)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 从文件导入数据
     */
    suspend fun importFromFile(uri: Uri): ImportResult = withContext(Dispatchers.IO) {
        try {
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, "UTF-8")).use { reader ->
                    reader.readText()
                }
            } ?: return@withContext ImportResult.Error("无法读取文件")

            val backupData = DataBackupUtil.fromJson(json)
            importData(backupData)
        } catch (e: Exception) {
            e.printStackTrace()
            ImportResult.Error("解析文件失败: ${e.message}")
        }
    }

    /**
     * 导入数据到数据库
     */
    private suspend fun importData(backupData: BackupData): ImportResult {
        try {
            // 导入自定义食物
            if (backupData.customFoods.isNotEmpty()) {
                foodDao.insertFoods(backupData.customFoods)
            }

            // 导入摄入记录
            backupData.intakeRecords.forEach { record ->
                intakeRecordDao.insertRecord(record)
            }

            // 导入身体数据
            backupData.bodyRecords.forEach { record ->
                bodyRecordDao.insertRecord(record)
            }

            // 导入睡眠记录
            backupData.sleepRecords.forEach { record ->
                sleepRecordDao.insertRecord(record)
            }

            // 导入饮食计划
            val planIdMap = mutableMapOf<Long, Long>()
            backupData.mealPlans.forEach { plan ->
                val oldId = plan.id
                val newId = mealPlanDao.insertPlan(plan.copy(id = 0))
                planIdMap[oldId] = newId
            }

            // 导入饮食计划项目（更新planId）
            backupData.mealPlanItems.forEach { item ->
                val newPlanId = planIdMap[item.planId] ?: item.planId
                mealPlanItemDao.insertItem(item.copy(planId = newPlanId, id = 0))
            }

            // 导入用户设置
            backupData.userSettings?.let { settings ->
                userSettingsDao.insertSettings(settings)
            }

            return ImportResult.Success(
                intakeCount = backupData.intakeRecords.size,
                bodyCount = backupData.bodyRecords.size,
                sleepCount = backupData.sleepRecords.size,
                foodCount = backupData.customFoods.size
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return ImportResult.Error("导入失败: ${e.message}")
        }
    }
}

sealed class ImportResult {
    data class Success(
        val intakeCount: Int,
        val bodyCount: Int,
        val sleepCount: Int,
        val foodCount: Int
    ) : ImportResult()

    data class Error(val message: String) : ImportResult()
}