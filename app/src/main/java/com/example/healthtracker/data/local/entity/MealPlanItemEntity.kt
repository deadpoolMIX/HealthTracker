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
        ),
        ForeignKey(
            entity = FoodEntity::class,
            parentColumns = ["id"],
            childColumns = ["foodId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("planId"), Index("foodId")]
)
data class MealPlanItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val planId: Long,                    // 所属计划ID
    val foodId: Long,                    // 食品ID
    val foodName: String,                // 食品名称
    val amount: Double,                  // 计划摄入量 (g)
    val mealType: Int,                   // 餐次：0-早餐, 1-午餐, 2-晚餐, 3-加餐
    val dayOfWeek: Int? = null,          // 周计划：星期几 (0-6)
    val dayOfMonth: Int? = null,         // 月计划：几号 (1-31)
    val createdAt: Long = System.currentTimeMillis()
)