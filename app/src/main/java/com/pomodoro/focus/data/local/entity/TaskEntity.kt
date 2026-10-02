package com.pomodoro.focus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val status: String = "TODO", // TODO, UNFINISHED, ACHIEVED
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val totalFocusTimeMs: Long = 0
)
