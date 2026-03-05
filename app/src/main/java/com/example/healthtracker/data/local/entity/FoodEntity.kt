package com.example.healthtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 食品营养数据实体
 * 内置的食品营养数据库
 */
@Entity(tableName = "foods")
data class FoodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,                    // 食品名称
    val category: String,                // 分类：肉类、蔬菜、水果、主食、豆类、蛋奶等
    val calories: Double,                // 热量 (kcal/100g)
    val carbohydrates: Double,           // 碳水化合物 (g/100g)
    val protein: Double,                 // 蛋白质 (g/100g)
    val fat: Double,                     // 脂肪 (g/100g)
    val icon: String,                    // 图标名称
    val isCustom: Boolean = false,       // 是否为用户自定义
    val createdAt: Long = System.currentTimeMillis()
)