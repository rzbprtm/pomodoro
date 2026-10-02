package com.pomodoro.focus.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pomodoro.focus.ui.calendar.CalendarHeatmapScreen
import com.pomodoro.focus.ui.calendar.CalendarViewModel
import com.pomodoro.focus.ui.settings.SettingsScreen
import com.pomodoro.focus.ui.settings.SettingsViewModel
import com.pomodoro.focus.ui.tasks.TaskManagementScreen
import com.pomodoro.focus.ui.tasks.TaskViewModel
import com.pomodoro.focus.ui.timer.TimerScreen
import com.pomodoro.focus.ui.timer.TimerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    timerViewModel: TimerViewModel,
    taskViewModel: TaskViewModel,
    calendarViewModel: CalendarViewModel,
    settingsViewModel: SettingsViewModel,
    isDarkTheme: Boolean
) {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "FocusForge",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.Calendar.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = "Kalender",
                            tint = if (currentRoute == Screen.Calendar.route)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = {
                        navController.navigate(Screen.Settings.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Pengaturan",
                            tint = if (currentRoute == Screen.Settings.route)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = currentRoute == Screen.Timer.route,
                    onClick = {
                        navController.navigate(Screen.Timer.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Timer, contentDescription = null) },
                    label = { Text("Timer") }
                )
                NavigationBarItem(
                    selected = currentRoute == Screen.Tasks.route,
                    onClick = {
                        navController.navigate(Screen.Tasks.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.TaskAlt, contentDescription = null) },
                    label = { Text("Target") }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Timer.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Timer.route) {
                TimerScreen(viewModel = timerViewModel)
            }
            composable(Screen.Tasks.route) {
                TaskManagementScreen(viewModel = taskViewModel)
            }
            composable(Screen.Calendar.route) {
                CalendarHeatmapScreen(
                    viewModel = calendarViewModel,
                    isDarkTheme = isDarkTheme
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}
