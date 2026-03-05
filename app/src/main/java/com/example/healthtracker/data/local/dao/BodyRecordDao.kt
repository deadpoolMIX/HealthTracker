package com.example.healthtracker.data.local.dao

import androidx.room.*
import com.example.healthtracker.data.local.entity.BodyRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyRecordDao {
    @Query("SELECT * FROM body_records WHERE id = :id")
    suspend fun getRecordById(id: Long): BodyRecordEntity?

    @Query("SELECT * FROM body_records WHERE date = :date LIMIT 1")
    suspend fun getRecordByDate(date: Long): BodyRecordEntity?

    @Query("SELECT * FROM body_records WHERE date = :date LIMIT 1")
    fun getRecordByDateFlow(date: Long): Flow<BodyRecordEntity?>

    @Query("SELECT * FROM body_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getRecordsBetween(startDate: Long, endDate: Long): Flow<List<BodyRecordEntity>>

    @Query("SELECT * FROM body_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getRecordsBetweenSync(startDate: Long, endDate: Long): List<BodyRecordEntity>

    @Query("SELECT * FROM body_records ORDER BY date DESC LIMIT 1")
    suspend fun getLatestRecord(): BodyRecordEntity?

    @Query("SELECT * FROM body_records ORDER BY date DESC")
    fun getAllRecords(): Flow<List<BodyRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: BodyRecordEntity): Long

    @Update
    suspend fun updateRecord(record: BodyRecordEntity)

    @Delete
    suspend fun deleteRecord(record: BodyRecordEntity)

    @Query("DELETE FROM body_records WHERE date < :beforeDate")
    suspend fun deleteRecordsBefore(beforeDate: Long)
}