package com.example.healthtracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 食物摄入记录实体
 */
@Entity(
    tableName = "intake_records",
    foreignKeys = [
        ForeignKey(
            entity = FoodEntity::class,
            parentColumns = ["id"],
            childColumns = ["foodId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("foodId"), Index("date")]
)
data class IntakeRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val foodId: Long? = null,             // 食品ID（可空，支持自定义输入）
    val foodName: String,                 // 食品名称
    val foodIcon: String? = null,         // 食品图标（emoji）
    val date: Long,                       // 记录日期（时间戳，仅日期部分）
    val amount: Double,                   // 摄入量 (g 或 ml)
    val calories: Double,                 // 实际热量 (kcal)
    val carbohydrates: Double,            // 实际碳水化合物 (g)
    val protein: Double,                  // 实际蛋白质 (g)
    val fat: Double,                      // 实际脂肪 (g)
    val mealType: Int,                    // 餐次：0-早餐, 1-午餐, 2-晚餐, 3-加餐
    // 每百克营养数据（用于记录时的参考值）
    val caloriesPer100g: Double = 0.0,    // 每百克热量
    val carbsPer100g: Double = 0.0,       // 每百克碳水
    val proteinPer100g: Double = 0.0,     // 每百克蛋白质
    val fatPer100g: Double = 0.0,         // 每百克脂肪
    // 单位相关
    val unit: String? = null,             // 单位（个/杯/瓶等）
    val amountInUnit: Double? = null,     // 按单位计的数量
    val gramsPerUnit: Double? = null,     // 每单位对应多少克
    // 其他
    val note: String? = null,             // 备注
    val multiDayInfo: String? = null,     // 多天分摊信息 JSON
    val createdAt: Long = System.currentTimeMillis()
)