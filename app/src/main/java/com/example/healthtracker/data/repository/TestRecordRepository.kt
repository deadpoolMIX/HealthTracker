package com.example.healthtracker.data.repository

import com.example.healthtracker.data.local.dao.TestRecordDao
import com.example.healthtracker.data.local.entity.TestRecordEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestRecordRepository @Inject constructor(
    private val testRecordDao: TestRecordDao
) {
    fun getAllRecords(): Flow<List<TestRecordEntity>> = testRecordDao.getAllRecords()

    fun getRecordsByType(type: String): Flow<List<TestRecordEntity>> = testRecordDao.getRecordsByType(type)

    suspend fun getRecordsBetween(startDate: Long, endDate: Long): List<TestRecordEntity> =
        testRecordDao.getRecordsBetween(startDate, endDate)

    suspend fun getRecordCount(): Int = testRecordDao.getRecordCount()

    suspend fun insertRecord(record: TestRecordEntity): Long = testRecordDao.insertRecord(record)

    suspend fun insertRecords(records: List<TestRecordEntity>) = testRecordDao.insertRecords(records)

    suspend fun deleteAllRecords() = testRecordDao.deleteAllRecords()
}