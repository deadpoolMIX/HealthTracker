package com.example.healthtracker.data.local.dao

import androidx.room.*
import com.example.healthtracker.data.local.entity.MealPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealPlanDao {
    @Query("SELECT * FROM meal_plans WHERE id = :id")
    suspend fun getPlanById(id: Long): MealPlanEntity?

    @Query("SELECT * FROM meal_plans WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActivePlans(): Flow<List<MealPlanEntity>>

    @Query("SELECT * FROM meal_plans ORDER BY createdAt DESC")
    fun getAllPlans(): Flow<List<MealPlanEntity>>

    @Query("SELECT * FROM meal_plans WHERE planType = :planType AND isActive = 1")
    fun getActivePlansByType(planType: Int): Flow<List<MealPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: MealPlanEntity): Long

    @Update
    suspend fun updatePlan(plan: MealPlanEntity)

    @Delete
    suspend fun deletePlan(plan: MealPlanEntity)

    @Query("UPDATE meal_plans SET isActive = 0 WHERE id = :id")
    suspend fun deactivatePlan(id: Long)

    @Query("UPDATE meal_plans SET isActive = 1 WHERE id = :id")
    suspend fun activatePlan(id: Long)
}