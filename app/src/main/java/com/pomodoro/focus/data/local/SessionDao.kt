package com.pomodoro.focus.data.local

import androidx.room.*
import com.pomodoro.focus.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM pomodoro_sessions WHERE dateKey = :dateKey")
    fun getByDate(dateKey: String): Flow<List<SessionEntity>>

    @Query("SELECT * FROM pomodoro_sessions WHERE status = 'COMPLETED' AND dateKey = :dateKey")
    suspend fun getCompletedByDate(dateKey: String): List<SessionEntity>

    @Insert
    suspend fun insert(session: SessionEntity): Long

    @Update
    suspend fun update(session: SessionEntity)
}
