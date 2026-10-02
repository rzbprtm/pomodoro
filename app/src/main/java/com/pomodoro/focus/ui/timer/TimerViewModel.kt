package com.pomodoro.focus.ui.timer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pomodoro.focus.data.local.entity.SessionEntity
import com.pomodoro.focus.data.local.entity.TaskEntity
import com.pomodoro.focus.data.preferences.SettingsDataStore
import com.pomodoro.focus.data.repository.TaskRepository
import com.pomodoro.focus.service.FocusStateManager
import com.pomodoro.focus.service.PomodoroForegroundService
import com.pomodoro.focus.util.PermissionHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class TimerPhase { IDLE, FOCUS, REST }

data class TimerUiState(
    val phase: TimerPhase = TimerPhase.IDLE,
    val totalMs: Long = 50 * 60 * 1000L,
    val remainingMs: Long = 50 * 60 * 1000L,
    val focusMinutes: Int = 50,
    val restMinutes: Int = 10,
    val selectedTask: TaskEntity? = null,
    val currentSessionId: Long? = null,
    val isRunning: Boolean = false
)

class TimerViewModel(
    application: Application,
    private val repository: TaskRepository,
    private val settingsDataStore: SettingsDataStore
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(TimerUiState())
    val state: StateFlow<TimerUiState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var sessionEntity: SessionEntity? = null

    private var isAutoDndEnabled: Boolean = true

    val todoTasks = repository.getTodoTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unfinishedTasks = repository.getUnfinishedTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Observe settings changes
        viewModelScope.launch {
            settingsDataStore.focusMinutes.collect { minutes ->
                if (_state.value.phase == TimerPhase.IDLE) {
                    val ms = minutes * 60 * 1000L
                    _state.update { it.copy(focusMinutes = minutes, totalMs = ms, remainingMs = ms) }
                } else {
                    _state.update { it.copy(focusMinutes = minutes) }
                }
            }
        }

        viewModelScope.launch {
            settingsDataStore.restMinutes.collect { minutes ->
                _state.update { it.copy(restMinutes = minutes) }
            }
        }

        viewModelScope.launch {
            settingsDataStore.whitelistPackages.collect { pkgs ->
                FocusStateManager.updateWhitelist(pkgs)
            }
        }

        viewModelScope.launch {
            settingsDataStore.autoDnd.collect { enabled ->
                isAutoDndEnabled = enabled
            }
        }
    }

    fun setFocusMinutes(m: Int) {
        viewModelScope.launch {
            settingsDataStore.setFocusMinutes(m)
        }
    }

    fun setRestMinutes(m: Int) {
        viewModelScope.launch {
            settingsDataStore.setRestMinutes(m)
        }
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

        // Activate Accessibility App Blocker instantly
        FocusStateManager.setFocusActive(true)

        // Start Foreground Service
        PomodoroForegroundService.start(getApplication(), "FOCUS", totalMs)

        // Enable DND if permitted
        if (isAutoDndEnabled) {
            PermissionHelper.setDndMode(getApplication(), true)
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

        // Disable app blocker (100% free app access during rest!)
        FocusStateManager.setFocusActive(false)

        // Turn off DND
        PermissionHelper.setDndMode(getApplication(), false)

        // Update foreground service notification
        PomodoroForegroundService.start(getApplication(), "REST", totalMs)

        startCountdown()
    }

    private fun startCountdown() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var counter = 0
            while (_state.value.remainingMs > 0 && _state.value.isRunning) {
                delay(1000)
                _state.update { it.copy(remainingMs = (it.remainingMs - 1000).coerceAtLeast(0)) }
                counter++
                // Update persistent notification every 5 seconds or when low
                if (counter % 5 == 0 || _state.value.remainingMs <= 10000) {
                    val phaseName = if (_state.value.phase == TimerPhase.FOCUS) "FOCUS" else "REST"
                    PomodoroForegroundService.update(getApplication(), phaseName, _state.value.remainingMs)
                }
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
                // Focus complete -> Stop blocking
                FocusStateManager.setFocusActive(false)
                PermissionHelper.setDndMode(getApplication(), false)

                viewModelScope.launch {
                    sessionEntity?.let { repository.completeSession(it) }
                    current.selectedTask?.let {
                        repository.updateTaskFocusTime(it.id, current.totalMs)
                    }
                }
                // Transition to rest mode
                val restMs = current.restMinutes * 60 * 1000L
                _state.update {
                    it.copy(
                        phase = TimerPhase.REST,
                        totalMs = restMs,
                        remainingMs = restMs,
                        isRunning = false
                    )
                }
                PomodoroForegroundService.update(getApplication(), "REST", restMs)
            }
            TimerPhase.REST -> {
                // Rest complete
                _state.update {
                    val focusMs = it.focusMinutes * 60 * 1000L
                    it.copy(
                        phase = TimerPhase.IDLE,
                        totalMs = focusMs,
                        remainingMs = focusMs,
                        isRunning = false
                    )
                }
                PomodoroForegroundService.stop(getApplication())
            }
            TimerPhase.IDLE -> {}
        }
    }

    fun emergencyExit() {
        timerJob?.cancel()
        FocusStateManager.setFocusActive(false)
        PermissionHelper.setDndMode(getApplication(), false)
        PomodoroForegroundService.stop(getApplication())

        val current = _state.value
        val elapsedMs = current.totalMs - current.remainingMs

        viewModelScope.launch {
            sessionEntity?.let { repository.abortSession(it, elapsedMs) }
            current.selectedTask?.let { task ->
                repository.updateTaskFocusTime(task.id, elapsedMs)
                repository.markUnfinished(task)
            }
        }

        val focusMs = current.focusMinutes * 60 * 1000L
        _state.update {
            it.copy(
                phase = TimerPhase.IDLE,
                remainingMs = focusMs,
                totalMs = focusMs,
                isRunning = false,
                currentSessionId = null,
                selectedTask = null
            )
        }
        sessionEntity = null
    }

    fun completeTarget() {
        timerJob?.cancel()
        FocusStateManager.setFocusActive(false)
        PermissionHelper.setDndMode(getApplication(), false)
        PomodoroForegroundService.stop(getApplication())

        val current = _state.value
        val task = current.selectedTask ?: return
        val elapsedMs = current.totalMs - current.remainingMs

        viewModelScope.launch {
            repository.updateTaskFocusTime(task.id, elapsedMs)
            repository.markAchieved(task)
            sessionEntity?.let { repository.completeSession(it) }
        }

        val focusMs = current.focusMinutes * 60 * 1000L
        _state.update {
            it.copy(
                phase = TimerPhase.IDLE,
                remainingMs = focusMs,
                totalMs = focusMs,
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
        FocusStateManager.setFocusActive(false)
        PermissionHelper.setDndMode(getApplication(), false)
        PomodoroForegroundService.stop(getApplication())
    }
}
