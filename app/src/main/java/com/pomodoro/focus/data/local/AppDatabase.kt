package com.pomodoro.focus.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pomodoro.focus.data.local.entity.DailyRecordEntity
import com.pomodoro.focus.data.local.entity.SessionEntity
import com.pomodoro.focus.data.local.entity.TaskEntity

@Database(
    entities = [TaskEntity::class, SessionEntity::class, DailyRecordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun sessionDao(): SessionDao
    abstract fun dailyRecordDao(): DailyRecordDao
}
