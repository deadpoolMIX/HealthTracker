package com.example.healthtracker.data.repository

import com.example.healthtracker.data.local.dao.BodyRecordDao
import com.example.healthtracker.data.local.entity.BodyRecordEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BodyRecordRepository @Inject constructor(
    private val bodyRecordDao: BodyRecordDao
) {
    suspend fun getRecordById(id: Long) = bodyRecordDao.getRecordById(id)

    suspend fun getRecordByDate(date: Long) = bodyRecordDao.getRecordByDate(date)

    fun getRecordByDateFlow(date: Long): Flow<BodyRecordEntity?> =
        bodyRecordDao.getRecordByDateFlow(date)

    fun getRecordsBetween(startDate: Long, endDate: Long): Flow<List<BodyRecordEntity>> =
        bodyRecordDao.getRecordsBetween(startDate, endDate)

    suspend fun getRecordsBetweenSync(startDate: Long, endDate: Long): List<BodyRecordEntity> =
        bodyRecordDao.getRecordsBetweenSync(startDate, endDate)

    suspend fun getLatestRecord() = bodyRecordDao.getLatestRecord()

    fun getAllRecords(): Flow<List<BodyRecordEntity>> = bodyRecordDao.getAllRecords()

    suspend fun getAllRecordsSync(): List<BodyRecordEntity> = bodyRecordDao.getAllRecordsSync()

    suspend fun insertRecord(record: BodyRecordEntity): Long =
        bodyRecordDao.insertRecord(record)

    suspend fun updateRecord(record: BodyRecordEntity) =
        bodyRecordDao.updateRecord(record)

    suspend fun deleteRecord(record: BodyRecordEntity) =
        bodyRecordDao.deleteRecord(record)
}