package com.pomodoro.focus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pomodoro_sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long? = null,
    val focusDurationMs: Long,    // configured duration
    val elapsedMs: Long = 0,      // actual elapsed
    val status: String = "RUNNING", // RUNNING, COMPLETED, ABORTED
    val startedAt: Long = System.currentTimeMillis(),
    val finishedAt: Long? = null,
    val dateKey: String // yyyy-MM-dd for streak lookup
)
