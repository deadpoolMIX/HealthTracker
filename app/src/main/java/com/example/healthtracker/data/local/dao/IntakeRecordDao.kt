package com.example.healthtracker.data.local.dao

import androidx.room.*
import com.example.healthtracker.data.local.entity.IntakeRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IntakeRecordDao {
    @Query("SELECT * FROM intake_records WHERE id = :id")
    suspend fun getRecordById(id: Long): IntakeRecordEntity?

    @Query("SELECT * FROM intake_records WHERE date = :date ORDER BY createdAt DESC")
    fun getRecordsByDate(date: Long): Flow<List<IntakeRecordEntity>>

    @Query("SELECT * FROM intake_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC, createdAt DESC")
    fun getRecordsBetween(startDate: Long, endDate: Long): Flow<List<IntakeRecordEntity>>

    @Query("SELECT * FROM intake_records WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getRecordsBetweenSync(startDate: Long, endDate: Long): List<IntakeRecordEntity>

    @Query("""
        SELECT SUM(calories) FROM intake_records
        WHERE date BETWEEN :startDate AND :endDate
    """)
    fun getTotalCaloriesBetween(startDate: Long, endDate: Long): Flow<Double?>

    @Query("""
        SELECT SUM(carbohydrates) FROM intake_records
        WHERE date BETWEEN :startDate AND :endDate
    """)
    fun getTotalCarbsBetween(startDate: Long, endDate: Long): Flow<Double?>

    @Query("""
        SELECT SUM(protein) FROM intake_records
        WHERE date BETWEEN :startDate AND :endDate
    """)
    fun getTotalProteinBetween(startDate: Long, endDate: Long): Flow<Double?>

    @Query("""
        SELECT SUM(fat) FROM intake_records
        WHERE date BETWEEN :startDate AND :endDate
    """)
    fun getTotalFatBetween(startDate: Long, endDate: Long): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: IntakeRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<IntakeRecordEntity>)

    @Update
    suspend fun updateRecord(record: IntakeRecordEntity)

    @Delete
    suspend fun deleteRecord(record: IntakeRecordEntity)

    @Delete
    suspend fun deleteRecords(records: List<IntakeRecordEntity>)

    @Query("DELETE FROM intake_records WHERE id IN (:ids)")
    suspend fun deleteRecordsByIds(ids: List<Long>)

    @Query("DELETE FROM intake_records WHERE date < :beforeDate")
    suspend fun deleteRecordsBefore(beforeDate: Long)

    // 获取最近摄入的食物名称（去重）
    @Query("""
        SELECT DISTINCT foodName FROM intake_records
        ORDER BY createdAt DESC
        LIMIT :limit
    """)
    fun getRecentFoodNames(limit: Int = 20): Flow<List<String>>

    // 获取最近N条摄入记录（用于最近摄入的食物）
    @Query("SELECT * FROM intake_records ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentRecords(limit: Int = 50): Flow<List<IntakeRecordEntity>>

    // 获取每个食物名称的最近记录时间（返回食物名称和最近记录时间）
    @Query("""
        SELECT foodName, MAX(createdAt) as lastRecordTime
        FROM intake_records
        GROUP BY foodName
    """)
    suspend fun getFoodLastRecordTimes(): List<FoodLastRecord>
}

/**
 * 食物最近记录时间的数据类
 */
data class FoodLastRecord(
    val foodName: String,
    val lastRecordTime: Long
)