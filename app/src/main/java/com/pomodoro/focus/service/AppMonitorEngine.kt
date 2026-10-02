package com.pomodoro.focus.service

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.pomodoro.focus.ui.blocker.BlockerActivity
import kotlinx.coroutines.*

/**
 * Real-time foreground app monitoring engine inspired by Android Digital Wellbeing.
 * Uses UsageEvents (ACTIVITY_RESUMED) polled every 350ms for low latency.
 */
class AppMonitorEngine(
    private val context: Context,
    private val ownPackage: String = context.packageName
) {
    private var job: Job? = null
    private var whitelistedPackages: Set<String> = setOf("com.whatsapp")
    private val systemAllowedPackages = mutableSetOf<String>()
    private var lastBlockedPackage: String? = null
    private var lastBlockedTime: Long = 0

    init {
        // Collect launcher (home) packages to allow navigating the home screen
        try {
            val pm = context.packageManager
            val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val resolveInfos = pm.queryIntentActivities(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
            for (info in resolveInfos) {
                systemAllowedPackages.add(info.activityInfo.packageName)
            }
        } catch (_: Exception) {}

        // Add essential system UI packages
        systemAllowedPackages.add("com.android.systemui")
        systemAllowedPackages.add("com.miui.home")
        systemAllowedPackages.add("com.miui.securitycenter")
        systemAllowedPackages.add("com.google.android.inputmethod.latin") // GBoard
        systemAllowedPackages.add("com.android.inputmethod.latin")
        systemAllowedPackages.add(ownPackage)
    }

    fun setWhitelist(packages: Set<String>) {
        whitelistedPackages = packages + ownPackage
    }

    fun start(scope: CoroutineScope) {
        job?.cancel()
        job = scope.launch(Dispatchers.Default) {
            val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return@launch

            var lastQueryTime = System.currentTimeMillis() - 5000

            while (isActive) {
                val now = System.currentTimeMillis()
                val foregroundApp = getForegroundPackage(usm, lastQueryTime, now)
                lastQueryTime = now - 500 // slight overlap to never miss events

                if (foregroundApp != null &&
                    foregroundApp !in systemAllowedPackages &&
                    foregroundApp !in whitelistedPackages
                ) {
                    val nowMs = System.currentTimeMillis()
                    // Debounce to prevent reopening activity multiple times per second
                    if (foregroundApp != lastBlockedPackage || nowMs - lastBlockedTime > 1200) {
                        lastBlockedPackage = foregroundApp
                        lastBlockedTime = nowMs
                        withContext(Dispatchers.Main) {
                            BlockerActivity.start(context, foregroundApp)
                        }
                    }
                }

                delay(350)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        lastBlockedPackage = null
    }

    private fun getForegroundPackage(
        usm: UsageStatsManager,
        startTime: Long,
        endTime: Long
    ): String? {
        var lastResumed: String? = null
        try {
            val events = usm.queryEvents(startTime, endTime)
            val event = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                    lastResumed = event.packageName
                }
            }
        } catch (_: Exception) {}
        return lastResumed
    }
}
