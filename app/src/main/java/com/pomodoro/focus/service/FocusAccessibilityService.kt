package com.pomodoro.focus.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.pm.PackageManager
import android.view.accessibility.AccessibilityEvent
import com.pomodoro.focus.ui.blocker.BlockerActivity

class FocusAccessibilityService : AccessibilityService() {

    private val systemAllowedPackages = mutableSetOf<String>()
    private var lastBlockedPackage: String? = null
    private var lastBlockedTime: Long = 0

    override fun onServiceConnected() {
        super.onServiceConnected()
        // Collect launcher (home) packages to allow navigating the home screen
        try {
            val pm = packageManager
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
        systemAllowedPackages.add("com.google.android.inputmethod.latin")
        systemAllowedPackages.add("com.android.inputmethod.latin")
        systemAllowedPackages.add(packageName) // Own app
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val pkgName = event.packageName?.toString() ?: return

        // 1. If focus mode is NOT active (IDLE or REST), do nothing. Free access!
        if (!FocusStateManager.isFocusActive.value) {
            return
        }

        // 2. Allow our own app, launcher, system UI, and keyboards
        if (pkgName in systemAllowedPackages || pkgName == packageName) {
            return
        }

        // 3. Allow user-whitelisted apps (e.g. WhatsApp)
        val whitelist = FocusStateManager.whitelistedPackages.value
        if (whitelist.contains(pkgName)) {
            return
        }

        // 4. Violation detected! Debounce to avoid multi-triggers
        val now = System.currentTimeMillis()
        if (pkgName != lastBlockedPackage || (now - lastBlockedTime) > 1000) {
            lastBlockedPackage = pkgName
            lastBlockedTime = now

            // Immediately send user to home screen or show blocker screen
            performGlobalAction(GLOBAL_ACTION_HOME)

            // Launch BlockerActivity from Accessibility Service context (fully permitted by Android)
            BlockerActivity.start(this, pkgName)
        }
    }

    override fun onInterrupt() {
        // Required callback
    }
}
