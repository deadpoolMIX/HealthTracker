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

    fun getCustomFoods(): Flow<List<FoodEntity>> = foodDao.getCustomFoods()

    fun getFavoriteFoods(): Flow<List<FoodEntity>> = foodDao.getFavoriteFoods()

    suspend fun insertFood(food: FoodEntity): Long = foodDao.insertFood(food)

    suspend fun deleteCustomFood(id: Long) = foodDao.deleteCustomFood(id)

    suspend fun toggleFavorite(id: Long) = foodDao.toggleFavorite(id)

    suspend fun setFavorite(id: Long, isFavorite: Boolean) = foodDao.setFavorite(id, isFavorite)
}