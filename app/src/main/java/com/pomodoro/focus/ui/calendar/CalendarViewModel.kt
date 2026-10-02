package com.pomodoro.focus.ui.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pomodoro.focus.data.local.entity.DailyRecordEntity
import com.pomodoro.focus.data.repository.TaskRepository
import com.pomodoro.focus.util.StreakCalculator
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val records: List<DailyRecordEntity> = emptyList(),
    val streak: Int = 0,
    val streakMessage: String = StreakCalculator.streakMessage(0)
)

class CalendarViewModel(
    application: Application,
    private val repository: TaskRepository
) : AndroidViewModel(application) {

    private val _currentMonth = MutableStateFlow(YearMonth.now())

    private val allRecords = repository.getAllDailyRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val state: StateFlow<CalendarUiState> = combine(_currentMonth, allRecords) { month, records ->
        val streak = StreakCalculator.calculateStreak(records)
        CalendarUiState(
            currentMonth = month,
            records = records,
            streak = streak,
            streakMessage = StreakCalculator.streakMessage(streak)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalendarUiState())

    fun previousMonth() {
        _currentMonth.update { it.minusMonths(1) }
    }

    fun nextMonth() {
        _currentMonth.update { it.plusMonths(1) }
    }
}
