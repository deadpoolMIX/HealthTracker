package com.example.healthtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 饮食计划实体
 */
@Entity(tableName = "meal_plans")
data class MealPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,                    // 计划名称
    val planType: Int,                   // 计划类型：0-单餐, 1-单日, 2-周计划, 3-月计划
    val isActive: Boolean = true,        // 是否激活
    val createdAt: Long = System.currentTimeMillis()
)