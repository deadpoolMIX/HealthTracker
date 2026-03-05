package com.example.healthtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 身体数据记录实体
 */
@Entity(tableName = "body_records")
data class BodyRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long,                      // 记录日期（时间戳，仅日期部分）
    val weight: Double?,                 // 体重 (kg)
    val bodyFatRate: Double?,            // 体脂率 (%)
    val muscleMass: Double?,             // 肌肉量 (kg)
    val chest: Double?,                  // 胸围 (cm)
    val waist: Double?,                  // 腰围 (cm)
    val hip: Double?,                    // 臀围 (cm)
    val createdAt: Long = System.currentTimeMillis()
)