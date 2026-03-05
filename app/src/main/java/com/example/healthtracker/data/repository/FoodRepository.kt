package com.example.healthtracker.data.repository

import com.example.healthtracker.data.local.dao.FoodDao
import com.example.healthtracker.data.local.entity.FoodEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodRepository @Inject constructor(
    private val foodDao: FoodDao
) {
    fun getFoodById(id: Long) = suspend { foodDao.getFoodById(id) }

    fun searchFoods(keyword: String): Flow<List<FoodEntity>> = foodDao.searchFoods(keyword)

    fun getFoodsByCategory(category: String): Flow<List<FoodEntity>> = foodDao.getFoodsByCategory(category)

    fun getAllCategories(): Flow<List<String>> = foodDao.getAllCategories()

    fun getAllFoods(): Flow<List<FoodEntity>> = foodDao.getAllFoods()

    suspend fun insertFood(food: FoodEntity): Long = foodDao.insertFood(food)

    suspend fun deleteCustomFood(id: Long) = foodDao.deleteCustomFood(id)
}