package com.example.healthtracker.data.local.dao

import androidx.room.*
import com.example.healthtracker.data.local.entity.CycleFoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CycleFoodDao {
    @Query("SELECT * FROM cycle_foods WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveCycleFoods(): Flow<List<CycleFoodEntity>>

    @Query("SELECT * FROM cycle_foods ORDER BY createdAt DESC")
    fun getAllCycleFoods(): Flow<List<CycleFoodEntity>>

    @Query("SELECT * FROM cycle_foods WHERE id = :id")
    suspend fun getCycleFoodById(id: Long): CycleFoodEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCycleFood(cycleFood: CycleFoodEntity): Long

    @Update
    suspend fun updateCycleFood(cycleFood: CycleFoodEntity)

    @Delete
    suspend fun deleteCycleFood(cycleFood: CycleFoodEntity)

    @Query("DELETE FROM cycle_foods WHERE id = :id")
    suspend fun deleteCycleFoodById(id: Long)

    @Query("DELETE FROM cycle_foods")
    suspend fun deleteAllCycleFoods()
}