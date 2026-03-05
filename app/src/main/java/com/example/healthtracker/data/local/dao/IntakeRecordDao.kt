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

    @Update
    suspend fun updateRecord(record: IntakeRecordEntity)

    @Delete
    suspend fun deleteRecord(record: IntakeRecordEntity)

    @Query("DELETE FROM intake_records WHERE date < :beforeDate")
    suspend fun deleteRecordsBefore(beforeDate: Long)
}