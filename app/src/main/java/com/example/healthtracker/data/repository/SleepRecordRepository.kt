package com.example.healthtracker.data.repository

import com.example.healthtracker.data.local.dao.SleepRecordDao
import com.example.healthtracker.data.local.entity.SleepRecordEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SleepRecordRepository @Inject constructor(
    private val sleepRecordDao: SleepRecordDao
) {
    suspend fun getRecordById(id: Long) = sleepRecordDao.getRecordById(id)

    suspend fun getRecordByDate(date: Long) = sleepRecordDao.getRecordByDate(date)

    fun getRecordByDateFlow(date: Long): Flow<SleepRecordEntity?> =
        sleepRecordDao.getRecordByDateFlow(date)

    fun getRecordsBetween(startDate: Long, endDate: Long): Flow<List<SleepRecordEntity>> =
        sleepRecordDao.getRecordsBetween(startDate, endDate)

    suspend fun getRecordsBetweenSync(startDate: Long, endDate: Long): List<SleepRecordEntity> =
        sleepRecordDao.getRecordsBetweenSync(startDate, endDate)

    fun getAverageDurationBetween(startDate: Long, endDate: Long): Flow<Double?> =
        sleepRecordDao.getAverageDurationBetween(startDate, endDate)

    suspend fun insertRecord(record: SleepRecordEntity): Long =
        sleepRecordDao.insertRecord(record)

    suspend fun updateRecord(record: SleepRecordEntity) =
        sleepRecordDao.updateRecord(record)

    suspend fun deleteRecord(record: SleepRecordEntity) =
        sleepRecordDao.deleteRecord(record)
}