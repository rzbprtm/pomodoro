package com.pomodoro.focus.data.local

import androidx.room.*
import com.pomodoro.focus.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY createdAt DESC")
    fun getByStatus(status: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): TaskEntity?

    @Insert
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("UPDATE tasks SET status = 'UNFINISHED' WHERE status = 'TODO' AND createdAt < :cutoffMs")
    suspend fun moveOldTodosToUnfinished(cutoffMs: Long)
}
