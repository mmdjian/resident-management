package com.resident.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memos")
data class Memo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",          // 备忘标题
    val content: String = "",        // 备忘内容
    val isImmediate: Boolean = false, // 是否即时提醒（打开App立即提醒）
    val remindTime: Long = 0,        // 延迟提醒时间（时间戳，0表示不使用）
    val isCompleted: Boolean = false, // 是否已完成
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
