package com.example.healthtracker.data.repository

import com.example.healthtracker.data.local.dao.CycleFoodDao
import com.example.healthtracker.data.local.entity.CycleFoodEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CycleFoodRepository @Inject constructor(
    private val cycleFoodDao: CycleFoodDao
) {
    fun getActiveCycleFoods(): Flow<List<CycleFoodEntity>> = cycleFoodDao.getActiveCycleFoods()

    fun getAllCycleFoods(): Flow<List<CycleFoodEntity>> = cycleFoodDao.getAllCycleFoods()

    suspend fun getCycleFoodById(id: Long): CycleFoodEntity? = cycleFoodDao.getCycleFoodById(id)

    suspend fun insertCycleFood(cycleFood: CycleFoodEntity): Long = cycleFoodDao.insertCycleFood(cycleFood)

    suspend fun updateCycleFood(cycleFood: CycleFoodEntity) = cycleFoodDao.updateCycleFood(cycleFood)

    suspend fun deleteCycleFood(cycleFood: CycleFoodEntity) = cycleFoodDao.deleteCycleFood(cycleFood)

    suspend fun deleteCycleFoodById(id: Long) = cycleFoodDao.deleteCycleFoodById(id)

    suspend fun deleteAllCycleFoods() = cycleFoodDao.deleteAllCycleFoods()
}