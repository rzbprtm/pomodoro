package com.pomodoro.focus.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.pomodoro.focus.FocusForgeApp
import com.pomodoro.focus.MainActivity

class PomodoroForegroundService : Service() {

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): PomodoroForegroundService = this@PomodoroForegroundService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val phase = intent.getStringExtra(EXTRA_PHASE) ?: "Fokus"
                val remainingMs = intent.getLongExtra(EXTRA_REMAINING_MS, 50 * 60 * 1000L)
                val notification = buildNotification(phase, remainingMs)
                startForeground(NOTIFICATION_ID, notification)
            }
            ACTION_UPDATE -> {
                val phase = intent.getStringExtra(EXTRA_PHASE) ?: "Fokus"
                val remainingMs = intent.getLongExtra(EXTRA_REMAINING_MS, 0L)
                updateNotification(phase, remainingMs)
            }
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun buildNotification(phase: String, remainingMs: Long): Notification {
        val minutes = (remainingMs / 1000 / 60).toInt()
        val seconds = ((remainingMs / 1000) % 60).toInt()

        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = if (phase == "REST") "FocusForge - Istirahat \u2615" else "FocusForge - Fokus \uD83C\uDF33"
        val text = "%02d:%02d tersisa".format(minutes, seconds)

        return NotificationCompat.Builder(this, FocusForgeApp.CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(phase: String, remainingMs: Long) {
        val notification = buildNotification(phase, remainingMs)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val ACTION_START = "com.pomodoro.focus.START"
        const val ACTION_UPDATE = "com.pomodoro.focus.UPDATE"
        const val ACTION_STOP = "com.pomodoro.focus.STOP"
        const val EXTRA_PHASE = "extra_phase"
        const val EXTRA_REMAINING_MS = "extra_remaining_ms"
        const val NOTIFICATION_ID = 1001

        fun start(context: Context, phase: String, remainingMs: Long) {
            val intent = Intent(context, PomodoroForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_PHASE, phase)
                putExtra(EXTRA_REMAINING_MS, remainingMs)
            }
            context.startForegroundService(intent)
        }

        fun update(context: Context, phase: String, remainingMs: Long) {
            val intent = Intent(context, PomodoroForegroundService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_PHASE, phase)
                putExtra(EXTRA_REMAINING_MS, remainingMs)
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, PomodoroForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
