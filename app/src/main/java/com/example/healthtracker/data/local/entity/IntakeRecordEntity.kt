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
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("foodId"), Index("date")]
)
data class IntakeRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val foodId: Long,                    // 食品ID
    val foodName: String,                // 食品名称（冗余存储，方便查询）
    val date: Long,                      // 记录日期（时间戳，仅日期部分）
    val amount: Double,                  // 摄入量 (g)
    val calories: Double,                // 实际热量 (kcal)
    val carbohydrates: Double,          // 实际碳水化合物 (g)
    val protein: Double,                 // 实际蛋白质 (g)
    val fat: Double,                     // 实际脂肪 (g)
    val mealType: Int,                   // 餐次：0-早餐, 1-午餐, 2-晚餐, 3-加餐
    val multiDayInfo: String? = null,    // 多天分摊信息 JSON: {"totalDays": n, "dayIndex": i, "recordId": xxx}
    val createdAt: Long = System.currentTimeMillis()
)