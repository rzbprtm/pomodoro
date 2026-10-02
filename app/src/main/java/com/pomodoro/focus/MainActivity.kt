package com.pomodoro.focus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.pomodoro.focus.data.local.AppDatabase
import com.pomodoro.focus.data.preferences.SettingsDataStore
import com.pomodoro.focus.data.repository.TaskRepository
import com.pomodoro.focus.ui.calendar.CalendarViewModel
import com.pomodoro.focus.ui.navigation.AppNavigation
import com.pomodoro.focus.ui.settings.SettingsViewModel
import com.pomodoro.focus.ui.tasks.TaskViewModel
import com.pomodoro.focus.ui.theme.FocusForgeTheme
import com.pomodoro.focus.ui.timer.TimerViewModel

class MainActivity : ComponentActivity() {

    private lateinit var db: AppDatabase
    private lateinit var repository: TaskRepository
    private lateinit var settingsDataStore: SettingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "focusforge.db"
        ).build()

        repository = TaskRepository(
            taskDao = db.taskDao(),
            sessionDao = db.sessionDao(),
            dailyRecordDao = db.dailyRecordDao()
        )

        settingsDataStore = SettingsDataStore(applicationContext)

        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(TimerViewModel::class.java) ->
                        TimerViewModel(application, repository) as T
                    modelClass.isAssignableFrom(TaskViewModel::class.java) ->
                        TaskViewModel(application, repository) as T
                    modelClass.isAssignableFrom(CalendarViewModel::class.java) ->
                        CalendarViewModel(application, repository) as T
                    modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                        SettingsViewModel(application, settingsDataStore) as T
                    else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
                }
            }
        }

        val timerVm = ViewModelProvider(this, factory)[TimerViewModel::class.java]
        val taskVm = ViewModelProvider(this, factory)[TaskViewModel::class.java]
        val calendarVm = ViewModelProvider(this, factory)[CalendarViewModel::class.java]
        val settingsVm = ViewModelProvider(this, factory)[SettingsViewModel::class.java]

        setContent {
            val isDarkMode by settingsVm.isDarkMode.collectAsState()

            FocusForgeTheme(darkTheme = isDarkMode) {
                AppNavigation(
                    timerViewModel = timerVm,
                    taskViewModel = taskVm,
                    calendarViewModel = calendarVm,
                    settingsViewModel = settingsVm,
                    isDarkTheme = isDarkMode
                )
            }
        }
    }
}
