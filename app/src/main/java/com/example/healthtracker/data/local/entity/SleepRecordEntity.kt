package com.example.healthtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 睡眠记录实体
 */
@Entity(tableName = "sleep_records")
data class SleepRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long,                      // 记录日期（起床日期，时间戳，仅日期部分）
    val sleepTime: Long,                 // 入睡时间（完整时间戳）
    val wakeTime: Long,                   // 起床时间（完整时间戳）
    val duration: Long,                  // 睡眠时长（分钟）
    val createdAt: Long = System.currentTimeMillis()
)