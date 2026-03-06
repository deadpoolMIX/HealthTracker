package com.example.healthtracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 饮食计划项目实体
 */
@Entity(
    tableName = "meal_plan_items",
    foreignKeys = [
        ForeignKey(
            entity = MealPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("planId")]
)
data class MealPlanItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val planId: Long,                    // 所属计划ID
    val foodId: Long? = null,            // 食品ID（可空，支持自定义）
    val foodName: String,                // 食品名称
    val amount: Double,                  // 计划摄入量 (g)
    val mealType: Int,                   // 餐次：0-早餐, 1-午餐, 2-晚餐, 3-加餐
    val dayOfWeek: Int? = null,          // 周计划：星期几 (0-6, 0=周一)
    val dayOfMonth: Int? = null,         // 月计划：几号 (1-31)
    // 每百克营养数据（用于自定义食物）
    val caloriesPer100g: Double = 0.0,
    val carbsPer100g: Double = 0.0,
    val proteinPer100g: Double = 0.0,
    val fatPer100g: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)