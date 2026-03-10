package com.example.healthtracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 测试数据记录实体
 * 用于存储模拟生成的测试数据，与正常数据分开存储
 */
@Entity(tableName = "test_records")
data class TestRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recordType: String,           // 记录类型: "intake", "body", "sleep"
    val date: Long,                   // 记录日期
    val dataJson: String,             // JSON格式的数据内容
    val createdAt: Long = System.currentTimeMillis()
)