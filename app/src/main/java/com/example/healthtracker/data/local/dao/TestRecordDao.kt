package com.example.healthtracker.data.local.dao

import androidx.room.*
import com.example.healthtracker.data.local.entity.TestRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TestRecordDao {
    @Query("SELECT * FROM test_records ORDER BY date ASC")
    fun getAllRecords(): Flow<List<TestRecordEntity>>

    @Query("SELECT * FROM test_records WHERE recordType = :type ORDER BY date ASC")
    fun getRecordsByType(type: String): Flow<List<TestRecordEntity>>

    @Query("SELECT * FROM test_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getRecordsBetween(startDate: Long, endDate: Long): List<TestRecordEntity>

    @Query("SELECT COUNT(*) FROM test_records")
    suspend fun getRecordCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: TestRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<TestRecordEntity>)

    @Query("DELETE FROM test_records")
    suspend fun deleteAllRecords()

    @Delete
    suspend fun deleteRecord(record: TestRecordEntity)
}