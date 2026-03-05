package com.example.healthtracker.data.local.dao

import androidx.room.*
import com.example.healthtracker.data.local.entity.FoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM foods WHERE id = :id")
    suspend fun getFoodById(id: Long): FoodEntity?

    @Query("SELECT * FROM foods WHERE name LIKE '%' || :keyword || '%' ORDER BY name")
    fun searchFoods(keyword: String): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE category = :category ORDER BY name")
    fun getFoodsByCategory(category: String): Flow<List<FoodEntity>>

    @Query("SELECT DISTINCT category FROM foods")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT * FROM foods ORDER BY name")
    fun getAllFoods(): Flow<List<FoodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoods(foods: List<FoodEntity>)

    @Update
    suspend fun updateFood(food: FoodEntity)

    @Delete
    suspend fun deleteFood(food: FoodEntity)

    @Query("DELETE FROM foods WHERE isCustom = 1 AND id = :id")
    suspend fun deleteCustomFood(id: Long)
}