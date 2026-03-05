package com.example.healthtracker.data.repository

import com.example.healthtracker.data.local.dao.MealPlanDao
import com.example.healthtracker.data.local.dao.MealPlanItemDao
import com.example.healthtracker.data.local.entity.MealPlanEntity
import com.example.healthtracker.data.local.entity.MealPlanItemEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealPlanRepository @Inject constructor(
    private val mealPlanDao: MealPlanDao,
    private val mealPlanItemDao: MealPlanItemDao
) {
    suspend fun getPlanById(id: Long) = mealPlanDao.getPlanById(id)

    fun getActivePlans(): Flow<List<MealPlanEntity>> = mealPlanDao.getActivePlans()

    fun getAllPlans(): Flow<List<MealPlanEntity>> = mealPlanDao.getAllPlans()

    fun getActivePlansByType(planType: Int): Flow<List<MealPlanEntity>> =
        mealPlanDao.getActivePlansByType(planType)

    suspend fun insertPlan(plan: MealPlanEntity): Long = mealPlanDao.insertPlan(plan)

    suspend fun updatePlan(plan: MealPlanEntity) = mealPlanDao.updatePlan(plan)

    suspend fun deletePlan(plan: MealPlanEntity) = mealPlanDao.deletePlan(plan)

    suspend fun activatePlan(id: Long) = mealPlanDao.activatePlan(id)

    suspend fun deactivatePlan(id: Long) = mealPlanDao.deactivatePlan(id)

    // Plan Items
    fun getItemsByPlanId(planId: Long): Flow<List<MealPlanItemEntity>> =
        mealPlanItemDao.getItemsByPlanId(planId)

    suspend fun getItemsByPlanIdSync(planId: Long): List<MealPlanItemEntity> =
        mealPlanItemDao.getItemsByPlanIdSync(planId)

    fun getItemsByPlanIdAndMealType(planId: Long, mealType: Int): Flow<List<MealPlanItemEntity>> =
        mealPlanItemDao.getItemsByPlanIdAndMealType(planId, mealType)

    fun getItemsForDayOfWeek(planId: Long, dayOfWeek: Int): Flow<List<MealPlanItemEntity>> =
        mealPlanItemDao.getItemsForDayOfWeek(planId, dayOfWeek)

    suspend fun insertItem(item: MealPlanItemEntity): Long = mealPlanItemDao.insertItem(item)

    suspend fun insertItems(items: List<MealPlanItemEntity>) = mealPlanItemDao.insertItems(items)

    suspend fun deleteItem(item: MealPlanItemEntity) = mealPlanItemDao.deleteItem(item)

    suspend fun deleteItemsByPlanId(planId: Long) = mealPlanItemDao.deleteItemsByPlanId(planId)
}