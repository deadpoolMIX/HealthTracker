package com.example.healthtracker.data.local.dao

import androidx.room.*
import com.example.healthtracker.data.local.entity.FoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM foods WHERE id = :id")
    suspend fun getFoodById(id: Long): FoodEntity?

    @Query("SELECT COUNT(*) FROM foods")
    suspend fun getFoodCount(): Int

    @Query("SELECT * FROM foods WHERE name LIKE '%' || :keyword || '%' ORDER BY createdAt DESC")
    fun searchFoods(keyword: String): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE name LIKE '%' || :keyword || '%' ORDER BY createdAt DESC LIMIT 20")
    suspend fun searchFoodsSync(keyword: String): List<FoodEntity>

    @Query("SELECT * FROM foods WHERE category = :category ORDER BY createdAt DESC")
    fun getFoodsByCategory(category: String): Flow<List<FoodEntity>>

    @Query("SELECT DISTINCT category FROM foods")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT * FROM foods ORDER BY createdAt DESC")
    fun getAllFoods(): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods ORDER BY createdAt DESC")
    suspend fun getAllFoodsOnce(): List<FoodEntity>

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

    // 获取用户自定义食物
    @Query("SELECT * FROM foods WHERE isCustom = 1 ORDER BY createdAt DESC")
    fun getCustomFoods(): Flow<List<FoodEntity>>

    // 清空导入的食物数据（保留自定义食物）
    @Query("DELETE FROM foods WHERE isCustom = 0")
    suspend fun deleteImportedFoods()
}