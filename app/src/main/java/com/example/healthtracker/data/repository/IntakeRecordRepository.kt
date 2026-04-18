package com.example.healthtracker.data.repository

import com.example.healthtracker.data.local.dao.FoodLastRecord
import com.example.healthtracker.data.local.dao.IntakeRecordDao
import com.example.healthtracker.data.local.entity.FoodEntity
import com.example.healthtracker.data.local.entity.IntakeRecordEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntakeRecordRepository @Inject constructor(
    private val intakeRecordDao: IntakeRecordDao
) {
    suspend fun getRecordById(id: Long) = intakeRecordDao.getRecordById(id)

    fun getRecordsByDate(date: Long): Flow<List<IntakeRecordEntity>> =
        intakeRecordDao.getRecordsByDate(date)

    fun getRecordsBetween(startDate: Long, endDate: Long): Flow<List<IntakeRecordEntity>> =
        intakeRecordDao.getRecordsBetween(startDate, endDate)

    suspend fun getRecordsBetweenSync(startDate: Long, endDate: Long): List<IntakeRecordEntity> =
        intakeRecordDao.getRecordsBetweenSync(startDate, endDate)

    fun getTotalCaloriesBetween(startDate: Long, endDate: Long): Flow<Double?> =
        intakeRecordDao.getTotalCaloriesBetween(startDate, endDate)

    fun getTotalCarbsBetween(startDate: Long, endDate: Long): Flow<Double?> =
        intakeRecordDao.getTotalCarbsBetween(startDate, endDate)

    fun getTotalProteinBetween(startDate: Long, endDate: Long): Flow<Double?> =
        intakeRecordDao.getTotalProteinBetween(startDate, endDate)

    fun getTotalFatBetween(startDate: Long, endDate: Long): Flow<Double?> =
        intakeRecordDao.getTotalFatBetween(startDate, endDate)

    suspend fun insertRecord(record: IntakeRecordEntity): Long =
        intakeRecordDao.insertRecord(record)

    suspend fun insertRecords(records: List<IntakeRecordEntity>) =
        intakeRecordDao.insertRecords(records)

    suspend fun updateRecord(record: IntakeRecordEntity) =
        intakeRecordDao.updateRecord(record)

    suspend fun deleteRecord(record: IntakeRecordEntity) =
        intakeRecordDao.deleteRecord(record)

    suspend fun deleteRecordsByIds(ids: List<Long>) =
        intakeRecordDao.deleteRecordsByIds(ids)

    fun getRecentRecords(limit: Int = 50): Flow<List<IntakeRecordEntity>> =
        intakeRecordDao.getRecentRecords(limit)

    suspend fun getFoodLastRecordTimes(): List<FoodLastRecord> =
        intakeRecordDao.getFoodLastRecordTimes()

    // 根据 foodId 获取相关记录
    suspend fun getRecordsByFoodId(foodId: Long): List<IntakeRecordEntity> =
        intakeRecordDao.getRecordsByFoodId(foodId)

    // 根据 foodId 获取相关记录数量
    suspend fun getRecordCountByFoodId(foodId: Long): Int =
        intakeRecordDao.getRecordCountByFoodId(foodId)

    // 根据食物名称获取相关记录
    suspend fun getRecordsByFoodName(foodName: String): List<IntakeRecordEntity> =
        intakeRecordDao.getRecordsByFoodName(foodName)

    fun searchRecords(query: String): Flow<List<IntakeRecordEntity>> =
        intakeRecordDao.searchRecords(query)

    /**
     * 同步更新所有使用该食物的历史记录
     * @param food 更新后的食物数据
     * @param oldName 旧的食物名称，用于匹配尚未关联 foodId 的旧记录
     * @return 更新的记录数量
     */
    suspend fun syncRecordsWithFood(food: FoodEntity, oldName: String): Int {
        val recordsById = intakeRecordDao.getRecordsByFoodId(food.id)
        val recordsByName = intakeRecordDao.getRecordsByFoodName(oldName)
        
        // 合并去重
        val records = (recordsById + recordsByName).distinctBy { it.id }

        if (records.isEmpty()) return 0

        // 更新每条记录
        val updatedRecords = records.map { record ->
            // 重新计算营养值（根据新的 per100g 数据和原有的 amount）
            val amount = record.amount
            val newCalories = (amount / 100.0) * food.calories
            val newCarbs = (amount / 100.0) * food.carbohydrates
            val newProtein = (amount / 100.0) * food.protein
            val newFat = (amount / 100.0) * food.fat

            record.copy(
                foodId = food.id,
                foodName = food.name, // 同步更新名称
                foodIcon = food.icon,
                calories = newCalories,
                carbohydrates = newCarbs,
                protein = newProtein,
                fat = newFat,
                caloriesPer100g = food.calories,
                carbsPer100g = food.carbohydrates,
                proteinPer100g = food.protein,
                fatPer100g = food.fat
            )
        }

        intakeRecordDao.updateRecords(updatedRecords)
        return updatedRecords.size
    }
}