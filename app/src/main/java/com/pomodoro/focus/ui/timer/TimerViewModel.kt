package com.pomodoro.focus.ui.timer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pomodoro.focus.data.local.entity.SessionEntity
import com.pomodoro.focus.data.local.entity.TaskEntity
import com.pomodoro.focus.data.repository.TaskRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class TimerPhase { IDLE, FOCUS, REST }

data class TimerUiState(
    val phase: TimerPhase = TimerPhase.IDLE,
    val totalMs: Long = 25 * 60 * 1000L,
    val remainingMs: Long = 25 * 60 * 1000L,
    val focusMinutes: Int = 25,
    val restMinutes: Int = 5,
    val selectedTask: TaskEntity? = null,
    val currentSessionId: Long? = null,
    val isRunning: Boolean = false
)

class TimerViewModel(
    application: Application,
    private val repository: TaskRepository
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(TimerUiState())
    val state: StateFlow<TimerUiState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var sessionEntity: SessionEntity? = null

    val todoTasks = repository.getTodoTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unfinishedTasks = repository.getUnfinishedTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFocusMinutes(m: Int) {
        _state.update {
            val ms = m * 60 * 1000L
            it.copy(focusMinutes = m, totalMs = ms, remainingMs = ms)
        }
    }

    fun setRestMinutes(m: Int) {
        _state.update { it.copy(restMinutes = m) }
    }

    fun selectTask(task: TaskEntity?) {
        _state.update { it.copy(selectedTask = task) }
    }

    fun startFocus() {
        val s = _state.value
        val totalMs = s.focusMinutes * 60 * 1000L
        _state.update {
            it.copy(
                phase = TimerPhase.FOCUS,
                totalMs = totalMs,
                remainingMs = totalMs,
                isRunning = true
            )
        }
        viewModelScope.launch {
            val sessionId = repository.startSession(s.selectedTask?.id, totalMs)
            val dateKey = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            sessionEntity = SessionEntity(
                id = sessionId,
                taskId = s.selectedTask?.id,
                focusDurationMs = totalMs,
                dateKey = dateKey
            )
            _state.update { it.copy(currentSessionId = sessionId) }
        }
        startCountdown()
    }

    fun startRest() {
        val totalMs = _state.value.restMinutes * 60 * 1000L
        _state.update {
            it.copy(
                phase = TimerPhase.REST,
                totalMs = totalMs,
                remainingMs = totalMs,
                isRunning = true
            )
        }
        startCountdown()
    }

    private fun startCountdown() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_state.value.remainingMs > 0 && _state.value.isRunning) {
                delay(1000)
                _state.update { it.copy(remainingMs = (it.remainingMs - 1000).coerceAtLeast(0)) }
            }
            if (_state.value.remainingMs <= 0) {
                onTimerComplete()
            }
        }
    }

    private fun onTimerComplete() {
        val current = _state.value
        when (current.phase) {
            TimerPhase.FOCUS -> {
                // Session completed
                viewModelScope.launch {
                    sessionEntity?.let { repository.completeSession(it) }
                    current.selectedTask?.let {
                        repository.updateTaskFocusTime(it.id, current.totalMs)
                    }
                }
                // Transition to rest
                _state.update {
                    it.copy(
                        phase = TimerPhase.REST,
                        totalMs = current.restMinutes * 60 * 1000L,
                        remainingMs = current.restMinutes * 60 * 1000L,
                        isRunning = false
                    )
                }
            }
            TimerPhase.REST -> {
                _state.update {
                    it.copy(phase = TimerPhase.IDLE, isRunning = false)
                }
            }
            TimerPhase.IDLE -> {}
        }
    }

    fun emergencyExit() {
        timerJob?.cancel()
        val current = _state.value
        val elapsedMs = current.totalMs - current.remainingMs

        viewModelScope.launch {
            // Abort session
            sessionEntity?.let { repository.abortSession(it, elapsedMs) }
            // Mark task as unfinished
            current.selectedTask?.let { task ->
                repository.updateTaskFocusTime(task.id, elapsedMs)
                repository.markUnfinished(task)
            }
        }

        _state.update {
            it.copy(
                phase = TimerPhase.IDLE,
                remainingMs = it.focusMinutes * 60 * 1000L,
                totalMs = it.focusMinutes * 60 * 1000L,
                isRunning = false,
                currentSessionId = null,
                selectedTask = null
            )
        }
        sessionEntity = null
    }

    fun completeTarget() {
        val current = _state.value
        val task = current.selectedTask ?: return
        val elapsedMs = current.totalMs - current.remainingMs

        timerJob?.cancel()
        viewModelScope.launch {
            // Mark task achieved
            repository.updateTaskFocusTime(task.id, elapsedMs)
            repository.markAchieved(task)
            // Also complete the session (target done = session done)
            sessionEntity?.let { repository.completeSession(it) }
        }

        _state.update {
            it.copy(
                phase = TimerPhase.IDLE,
                remainingMs = it.focusMinutes * 60 * 1000L,
                totalMs = it.focusMinutes * 60 * 1000L,
                isRunning = false,
                currentSessionId = null,
                selectedTask = null
            )
        }
        sessionEntity = null
    }

    fun addTask(title: String) {
        viewModelScope.launch { repository.addTask(title) }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
