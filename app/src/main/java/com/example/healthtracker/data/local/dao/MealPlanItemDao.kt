package com.example.healthtracker.data.local.dao

import androidx.room.*
import com.example.healthtracker.data.local.entity.MealPlanItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealPlanItemDao {
    @Query("SELECT * FROM meal_plan_items WHERE planId = :planId")
    fun getItemsByPlanId(planId: Long): Flow<List<MealPlanItemEntity>>

    @Query("SELECT * FROM meal_plan_items WHERE planId = :planId")
    suspend fun getItemsByPlanIdSync(planId: Long): List<MealPlanItemEntity>

    @Query("SELECT * FROM meal_plan_items WHERE planId = :planId AND mealType = :mealType")
    fun getItemsByPlanIdAndMealType(planId: Long, mealType: Int): Flow<List<MealPlanItemEntity>>

    @Query("""
        SELECT * FROM meal_plan_items
        WHERE planId = :planId AND (dayOfWeek IS NULL OR dayOfWeek = :dayOfWeek)
    """)
    fun getItemsForDayOfWeek(planId: Long, dayOfWeek: Int): Flow<List<MealPlanItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: MealPlanItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<MealPlanItemEntity>)

    @Update
    suspend fun updateItem(item: MealPlanItemEntity)

    @Delete
    suspend fun deleteItem(item: MealPlanItemEntity)

    @Query("DELETE FROM meal_plan_items WHERE planId = :planId")
    suspend fun deleteItemsByPlanId(planId: Long)
}