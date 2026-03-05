package com.example.healthtracker.data.local.dao

import androidx.room.*
import com.example.healthtracker.data.local.entity.SleepRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepRecordDao {
    @Query("SELECT * FROM sleep_records WHERE id = :id")
    suspend fun getRecordById(id: Long): SleepRecordEntity?

    @Query("SELECT * FROM sleep_records WHERE date = :date LIMIT 1")
    suspend fun getRecordByDate(date: Long): SleepRecordEntity?

    @Query("SELECT * FROM sleep_records WHERE date = :date LIMIT 1")
    fun getRecordByDateFlow(date: Long): Flow<SleepRecordEntity?>

    @Query("SELECT * FROM sleep_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getRecordsBetween(startDate: Long, endDate: Long): Flow<List<SleepRecordEntity>>

    @Query("SELECT * FROM sleep_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getRecordsBetweenSync(startDate: Long, endDate: Long): List<SleepRecordEntity>

    @Query("SELECT AVG(duration) FROM sleep_records WHERE date BETWEEN :startDate AND :endDate")
    fun getAverageDurationBetween(startDate: Long, endDate: Long): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: SleepRecordEntity): Long

    @Update
    suspend fun updateRecord(record: SleepRecordEntity)

    @Delete
    suspend fun deleteRecord(record: SleepRecordEntity)

    @Query("DELETE FROM sleep_records WHERE date < :beforeDate")
    suspend fun deleteRecordsBefore(beforeDate: Long)
}