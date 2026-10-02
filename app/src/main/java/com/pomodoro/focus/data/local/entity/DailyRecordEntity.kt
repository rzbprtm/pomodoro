package com.pomodoro.focus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_records")
data class DailyRecordEntity(
    @PrimaryKey val dateKey: String, // yyyy-MM-dd
    val completedSessions: Int = 0,
    val achievedTargets: Int = 0,
    val totalFocusMs: Long = 0
)
