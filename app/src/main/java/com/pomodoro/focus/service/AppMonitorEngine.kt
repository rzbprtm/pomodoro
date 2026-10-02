package com.pomodoro.focus.service

import android.app.usage.UsageStatsManager
import android.content.Context
import kotlinx.coroutines.*

/**
 * Polls foreground app every 800ms using UsageStatsManager.
 * Calls onViolation when a non-whitelisted app is detected.
 */
class AppMonitorEngine(
    private val context: Context,
    private val ownPackage: String = context.packageName,
    private val onViolation: (String) -> Unit
) {
    private var job: Job? = null
    private var whitelistedPackages: Set<String> = setOf("com.whatsapp")

    fun setWhitelist(packages: Set<String>) {
        whitelistedPackages = packages + ownPackage
    }

    fun start(scope: CoroutineScope) {
        job?.cancel()
        job = scope.launch(Dispatchers.Default) {
            while (isActive) {
                val foregroundPkg = getForegroundPackage()
                if (foregroundPkg != null && foregroundPkg !in whitelistedPackages) {
                    withContext(Dispatchers.Main) {
                        onViolation(foregroundPkg)
                    }
                }
                delay(800)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    private fun getForegroundPackage(): String? {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val stats = usm.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            now - 10_000, now
        )
        if (stats.isNullOrEmpty()) return null
        return stats.maxByOrNull { it.lastTimeUsed }?.packageName
    }
}
