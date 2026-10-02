package com.pomodoro.focus.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.pomodoro.focus.MainActivity
import com.pomodoro.focus.ui.theme.FocusForgeTheme
import com.pomodoro.focus.ui.theme.ForestBackground
import com.pomodoro.focus.ui.theme.ForestPrimary
import com.pomodoro.focus.ui.theme.ForestTextPrimary
import com.pomodoro.focus.ui.theme.ForestTextSecondary

class OverlayBlockerService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW -> showOverlay()
            ACTION_HIDE -> hideOverlay()
        }
        return START_NOT_STICKY
    }

    private fun showOverlay() {
        if (overlayView != null) return

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val params = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT,
            LayoutParams.TYPE_APPLICATION_OVERLAY,
            LayoutParams.FLAG_NOT_FOCUSABLE or
                    LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        // Make it intercept touches
        params.flags = params.flags and LayoutParams.FLAG_NOT_FOCUSABLE.inv()

        overlayView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OverlayBlockerService)
            setViewTreeSavedStateRegistryOwner(this@OverlayBlockerService)
            setContent {
                FocusForgeTheme(darkTheme = true) {
                    BlockerOverlayContent(
                        onBackToApp = {
                            val launchIntent = Intent(this@OverlayBlockerService, MainActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            }
                            startActivity(launchIntent)
                            hideOverlay()
                        }
                    )
                }
            }
        }

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        windowManager?.addView(overlayView, params)
    }

    private fun hideOverlay() {
        overlayView?.let {
            windowManager?.removeView(it)
            overlayView = null
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        hideOverlay()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
    }

    companion object {
        const val ACTION_SHOW = "com.pomodoro.focus.OVERLAY_SHOW"
        const val ACTION_HIDE = "com.pomodoro.focus.OVERLAY_HIDE"

        fun showIntent(context: Context): Intent =
            Intent(context, OverlayBlockerService::class.java).apply {
                action = ACTION_SHOW
            }

        fun hideIntent(context: Context): Intent =
            Intent(context, OverlayBlockerService::class.java).apply {
                action = ACTION_HIDE
            }
    }
}

@Composable
fun BlockerOverlayContent(onBackToApp: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ForestBackground.copy(alpha = 0.97f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "\uD83C\uDF33",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Fokusmu Sedang Berjalan",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = ForestTextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Kembali dan selesaikan targetmu!\nJangan biarkan pohon fokusmu layu.",
                style = MaterialTheme.typography.bodyLarge,
                color = ForestTextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onBackToApp,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForestPrimary
                ),
                shape = RoundedCornerShape(26.dp)
            ) {
                Text(
                    text = "Kembali ke FocusForge",
                    style = MaterialTheme.typography.labelLarge,
                    color = ForestBackground
                )
            }
        }
    }
}
