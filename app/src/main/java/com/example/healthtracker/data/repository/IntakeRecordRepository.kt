package com.example.healthtracker.data.repository

import com.example.healthtracker.data.local.dao.IntakeRecordDao
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

    suspend fun updateRecord(record: IntakeRecordEntity) =
        intakeRecordDao.updateRecord(record)

    suspend fun deleteRecord(record: IntakeRecordEntity) =
        intakeRecordDao.deleteRecord(record)

    suspend fun deleteRecordsByIds(ids: List<Long>) =
        intakeRecordDao.deleteRecordsByIds(ids)

    fun getRecentRecords(limit: Int = 50): Flow<List<IntakeRecordEntity>> =
        intakeRecordDao.getRecentRecords(limit)
}