package com.pomodoro.focus.ui.blocker

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pomodoro.focus.MainActivity
import com.pomodoro.focus.ui.theme.FocusForgeTheme
import com.pomodoro.focus.ui.theme.ForestBackground
import com.pomodoro.focus.ui.theme.ForestPrimary
import com.pomodoro.focus.ui.theme.ForestSurfaceVariant
import com.pomodoro.focus.ui.theme.ForestTextPrimary
import com.pomodoro.focus.ui.theme.ForestTextSecondary

class BlockerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val blockedPackage = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE) ?: ""
        val appName = try {
            val pm = packageManager
            val info = pm.getApplicationInfo(blockedPackage, 0)
            pm.getApplicationLabel(info).toString()
        } catch (_: Exception) {
            "Aplikasi Ini"
        }

        setContent {
            FocusForgeTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ForestBackground
                ) {
                    BlockerScreen(
                        appName = appName,
                        onBackToPomodoro = {
                            val mainIntent = Intent(this, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            }
                            startActivity(mainIntent)
                            finish()
                        },
                        onOpenSettings = {
                            val mainIntent = Intent(this, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                putExtra("navigate_to", "settings")
                            }
                            startActivity(mainIntent)
                            finish()
                        }
                    )
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Prevent bypassing via back button, route back to Pomodoro
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(mainIntent)
        finish()
    }

    companion object {
        const val EXTRA_BLOCKED_PACKAGE = "blocked_package"

        fun start(context: Context, packageName: String) {
            val intent = Intent(context, BlockerActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_NO_ANIMATION
                putExtra(EXTRA_BLOCKED_PACKAGE, packageName)
            }
            context.startActivity(intent)
        }
    }
}

@Composable
fun BlockerScreen(
    appName: String,
    onBackToPomodoro: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Sandglass / Pause icon like Digital Wellbeing
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(ForestSurfaceVariant, RoundedCornerShape(40.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "\u23F3", fontSize = 36.sp)
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Aplikasi Dijeda",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = ForestTextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "$appName sedang tidak diizinkan selama sesi fokus.",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = ForestPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tetap tenang dan selesaikan targetmu. Kamu bisa membuka aplikasi ini lagi saat waktu istirahat tiba.",
                style = MaterialTheme.typography.bodyMedium,
                color = ForestTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            Button(
                onClick = onBackToPomodoro,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForestPrimary
                ),
                shape = RoundedCornerShape(26.dp)
            ) {
                Text(
                    text = "Kembali ke FocusForge",
                    style = MaterialTheme.typography.labelLarge,
                    color = ForestBackground,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(
                    text = "Kelola Whitelist Aplikasi",
                    style = MaterialTheme.typography.labelMedium,
                    color = ForestTextPrimary
                )
            }
        }
    }
}
