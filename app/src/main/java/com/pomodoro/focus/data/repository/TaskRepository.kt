package com.pomodoro.focus.data.repository

import com.pomodoro.focus.data.local.DailyRecordDao
import com.pomodoro.focus.data.local.SessionDao
import com.pomodoro.focus.data.local.TaskDao
import com.pomodoro.focus.data.local.entity.DailyRecordEntity
import com.pomodoro.focus.data.local.entity.SessionEntity
import com.pomodoro.focus.data.local.entity.TaskEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TaskRepository(
    private val taskDao: TaskDao,
    private val sessionDao: SessionDao,
    private val dailyRecordDao: DailyRecordDao
) {
    private val dateFmt = DateTimeFormatter.ISO_LOCAL_DATE

    fun getTodoTasks() = taskDao.getByStatus("TODO")
    fun getUnfinishedTasks() = taskDao.getByStatus("UNFINISHED")
    fun getAchievedTasks() = taskDao.getByStatus("ACHIEVED")
    fun getAllTasks() = taskDao.getAll()

    suspend fun addTask(title: String): Long =
        taskDao.insert(TaskEntity(title = title))

    suspend fun getTask(id: Long) = taskDao.getById(id)

    suspend fun markAchieved(task: TaskEntity) {
        val now = System.currentTimeMillis()
        taskDao.update(task.copy(status = "ACHIEVED", completedAt = now))
        val dateKey = LocalDate.now().format(dateFmt)
        incrementAchieved(dateKey)
    }

    suspend fun markUnfinished(task: TaskEntity) {
        taskDao.update(task.copy(status = "UNFINISHED"))
    }

    suspend fun resetToTodo(task: TaskEntity) {
        taskDao.update(task.copy(status = "TODO"))
    }

    suspend fun deleteTask(task: TaskEntity) = taskDao.delete(task)

    suspend fun updateTaskFocusTime(taskId: Long, additionalMs: Long) {
        val task = taskDao.getById(taskId) ?: return
        taskDao.update(task.copy(totalFocusTimeMs = task.totalFocusTimeMs + additionalMs))
    }

    // Sessions
    suspend fun startSession(taskId: Long?, focusDurationMs: Long): Long {
        val dateKey = LocalDate.now().format(dateFmt)
        return sessionDao.insert(
            SessionEntity(
                taskId = taskId,
                focusDurationMs = focusDurationMs,
                dateKey = dateKey
            )
        )
    }

    suspend fun completeSession(session: SessionEntity) {
        sessionDao.update(
            session.copy(
                status = "COMPLETED",
                elapsedMs = session.focusDurationMs,
                finishedAt = System.currentTimeMillis()
            )
        )
        incrementCompleted(session.dateKey, session.focusDurationMs)
    }

    suspend fun abortSession(session: SessionEntity, elapsedMs: Long) {
        sessionDao.update(
            session.copy(
                status = "ABORTED",
                elapsedMs = elapsedMs,
                finishedAt = System.currentTimeMillis()
            )
        )
    }

    fun getSessionsByDate(dateKey: String) = sessionDao.getByDate(dateKey)

    // Daily records
    fun getDailyRecordsRange(start: String, end: String) =
        dailyRecordDao.getRange(start, end)

    fun getAllDailyRecords() = dailyRecordDao.getAll()

    suspend fun getDailyRecord(dateKey: String) = dailyRecordDao.getByDate(dateKey)

    private suspend fun incrementCompleted(dateKey: String, focusMs: Long) {
        val existing = dailyRecordDao.getByDate(dateKey)
        if (existing != null) {
            dailyRecordDao.upsert(
                existing.copy(
                    completedSessions = existing.completedSessions + 1,
                    totalFocusMs = existing.totalFocusMs + focusMs
                )
            )
        } else {
            dailyRecordDao.upsert(
                DailyRecordEntity(
                    dateKey = dateKey,
                    completedSessions = 1,
                    totalFocusMs = focusMs
                )
            )
        }
    }

    private suspend fun incrementAchieved(dateKey: String) {
        val existing = dailyRecordDao.getByDate(dateKey)
        if (existing != null) {
            dailyRecordDao.upsert(existing.copy(achievedTargets = existing.achievedTargets + 1))
        } else {
            dailyRecordDao.upsert(DailyRecordEntity(dateKey = dateKey, achievedTargets = 1))
        }
    }
}
