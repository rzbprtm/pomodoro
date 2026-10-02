package com.pomodoro.focus.ui.navigation

sealed class Screen(val route: String) {
    data object Timer : Screen("timer")
    data object Tasks : Screen("tasks")
    data object Calendar : Screen("calendar")
    data object Settings : Screen("settings")
}
