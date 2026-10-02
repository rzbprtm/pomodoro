package com.pomodoro.focus.ui.tasks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pomodoro.focus.data.local.entity.TaskEntity
import com.pomodoro.focus.data.repository.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(
    application: Application,
    private val repository: TaskRepository
) : AndroidViewModel(application) {

    val todoTasks = repository.getTodoTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unfinishedTasks = repository.getUnfinishedTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievedTasks = repository.getAchievedTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTask(title: String) {
        viewModelScope.launch { repository.addTask(title) }
    }

    fun markAchieved(task: TaskEntity) {
        viewModelScope.launch { repository.markAchieved(task) }
    }

    fun markUnfinished(task: TaskEntity) {
        viewModelScope.launch { repository.markUnfinished(task) }
    }

    fun resetToTodo(task: TaskEntity) {
        viewModelScope.launch { repository.resetToTodo(task) }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch { repository.deleteTask(task) }
    }
}
