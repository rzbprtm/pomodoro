package com.pomodoro.focus.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.pomodoro.focus.FocusForgeApp
import com.pomodoro.focus.MainActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PomodoroForegroundService : Service() {

    private val binder = LocalBinder()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _remainingMs = MutableStateFlow(0L)
    val remainingMs: StateFlow<Long> = _remainingMs

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private var timerJob: Job? = null

    inner class LocalBinder : Binder() {
        fun getService(): PomodoroForegroundService = this@PomodoroForegroundService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val durationMs = intent.getLongExtra(EXTRA_DURATION_MS, 25 * 60 * 1000L)
                startTimer(durationMs)
            }
            ACTION_STOP -> stopTimer()
        }
        return START_STICKY
    }

    private fun startTimer(durationMs: Long) {
        _remainingMs.value = durationMs
        _isRunning.value = true

        val notification = buildNotification(durationMs)
        startForeground(NOTIFICATION_ID, notification)

        timerJob?.cancel()
        timerJob = scope.launch {
            while (_remainingMs.value > 0 && _isRunning.value) {
                delay(1000)
                _remainingMs.value = (_remainingMs.value - 1000).coerceAtLeast(0)
                updateNotification(_remainingMs.value)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        _isRunning.value = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(remainingMs: Long): Notification {
        val minutes = (remainingMs / 1000 / 60).toInt()
        val seconds = ((remainingMs / 1000) % 60).toInt()

        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, FocusForgeApp.CHANNEL_ID)
            .setContentTitle("FocusForge - Fokus")
            .setContentText("%02d:%02d tersisa".format(minutes, seconds))
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(remainingMs: Long) {
        val notification = buildNotification(remainingMs)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        scope.cancel()
    }

    companion object {
        const val ACTION_START = "com.pomodoro.focus.START"
        const val ACTION_STOP = "com.pomodoro.focus.STOP"
        const val EXTRA_DURATION_MS = "duration_ms"
        const val NOTIFICATION_ID = 1001

        fun startIntent(context: Context, durationMs: Long): Intent =
            Intent(context, PomodoroForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_DURATION_MS, durationMs)
            }

        fun stopIntent(context: Context): Intent =
            Intent(context, PomodoroForegroundService::class.java).apply {
                action = ACTION_STOP
            }
    }
}
