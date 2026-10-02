package com.pomodoro.focus

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class FocusForgeApp : Application() {
    companion object {
        const val CHANNEL_ID = "pomodoro_timer"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Pomodoro Timer",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Timer countdown notification"
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
