package com.pomodoro.focus.ui.settings

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.pomodoro.focus.util.PermissionHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val focusMinutes by viewModel.focusMinutes.collectAsState()
    val restMinutes by viewModel.restMinutes.collectAsState()
    val autoDnd by viewModel.autoDnd.collectAsState()
    val autostartConfirmed by viewModel.autostartConfirmed.collectAsState()
    val apps by viewModel.installedApps.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Live permission states
    var hasAccessibility by remember { mutableStateOf(PermissionHelper.isAccessibilityServiceEnabled(context)) }
    var hasUsageStats by remember { mutableStateOf(PermissionHelper.hasUsageStatsPermission(context)) }
    var hasOverlay by remember { mutableStateOf(PermissionHelper.hasOverlayPermission(context)) }
    var hasBatteryOpt by remember { mutableStateOf(PermissionHelper.isBatteryOptimizationIgnored(context)) }
    var hasDndPolicy by remember { mutableStateOf(PermissionHelper.hasNotificationPolicyPermission(context)) }

    // Auto refresh permissions when returning to app (ON_RESUME)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasAccessibility = PermissionHelper.isAccessibilityServiceEnabled(context)
                hasUsageStats = PermissionHelper.hasUsageStatsPermission(context)
                hasOverlay = PermissionHelper.hasOverlayPermission(context)
                hasBatteryOpt = PermissionHelper.isBatteryOptimizationIgnored(context)
                hasDndPolicy = PermissionHelper.hasNotificationPolicyPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Theme section
        item {
            Text(
                text = "Tampilan",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isDarkMode) "Mode Gelap" else "Mode Terang",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode(it) }
                    )
                }
            }
        }

        // Timer settings
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Waktu Pomodoro Default",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Durasi Fokus: $focusMinutes menit", style = MaterialTheme.typography.bodyLarge)
                    Slider(
                        value = focusMinutes.toFloat(),
                        onValueChange = { viewModel.setFocusMinutes(it.toInt()) },
                        valueRange = 5f..120f,
                        steps = 22
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Durasi Istirahat: $restMinutes menit", style = MaterialTheme.typography.bodyLarge)
                    Slider(
                        value = restMinutes.toFloat(),
                        onValueChange = { viewModel.setRestMinutes(it.toInt()) },
                        valueRange = 1f..30f,
                        steps = 28
                    )
                }
            }
        }

        // Focus & DND Mode
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Mode Senyap (Do Not Disturb)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Senyapkan Notifikasi saat Fokus",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Otomatis mengheningkan suara & notifikasi aplikasi lain selama fokus, dan kembali normal saat istirahat.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = autoDnd,
                        onCheckedChange = { viewModel.toggleAutoDnd(it) }
                    )
                }
            }
        }

        // Permissions
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Izin & Stabilitas Sistem (HyperOS / Xiaomi)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Accessibility Service (Critical Blocker)
                    PermissionRow(
                        title = "Layanan Aksesibilitas (App Blocker)",
                        description = "Izin utama untuk memblokir aplikasi lain secara instan saat sesi fokus (Pilih FocusForge > Aktifkan)",
                        isGranted = hasAccessibility,
                        onClick = {
                            context.startActivity(PermissionHelper.accessibilitySettingsIntent())
                        }
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Usage Access
                    PermissionRow(
                        title = "Usage Access",
                        description = "Mendeteksi aplikasi sosmed / game yang sedang dibuka",
                        isGranted = hasUsageStats,
                        onClick = {
                            context.startActivity(PermissionHelper.usageStatsSettingsIntent())
                        }
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Overlay
                    PermissionRow(
                        title = "Tampil di Atas Aplikasi Lain",
                        description = "Izin memunculkan peringatan saat membuka aplikasi terlarang",
                        isGranted = hasOverlay,
                        onClick = {
                            context.startActivity(PermissionHelper.overlaySettingsIntent(context))
                        }
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Battery Optimization (Real check)
                    PermissionRow(
                        title = "Penghemat Baterai: Tanpa Pembatasan",
                        description = "Wajib disetel 'Tidak Ada Pembatasan' agar timer tidak mati di Xiaomi",
                        isGranted = hasBatteryOpt,
                        onClick = {
                            try {
                                context.startActivity(PermissionHelper.batteryOptimizationIntent(context))
                            } catch (_: Exception) {
                                context.startActivity(PermissionHelper.appInfoIntent(context))
                            }
                        }
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // DND Policy Access
                    PermissionRow(
                        title = "Akses Jangan Ganggu (DND)",
                        description = "Mengizinkan aplikasi mematikan notifikasi saat sesi fokus",
                        isGranted = hasDndPolicy,
                        onClick = {
                            try {
                                context.startActivity(PermissionHelper.notificationPolicySettingsIntent())
                            } catch (_: Exception) {}
                        }
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Autostart Xiaomi / HyperOS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mulai Otomatis (Autostart Xiaomi)",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Aktifkan opsi 'Autostart' di pengaturan aplikasi Xiaomi agar layanan tidak dimatikan paksa.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            TextButton(
                                onClick = {
                                    try {
                                        context.startActivity(PermissionHelper.miuiAutostartIntent())
                                    } catch (_: Exception) {
                                        context.startActivity(PermissionHelper.appInfoIntent(context))
                                    }
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Buka Pengaturan Autostart HP")
                            }
                        }
                        Checkbox(
                            checked = autostartConfirmed,
                            onCheckedChange = { viewModel.setAutostartConfirmed(it) }
                        )
                    }
                }
            }
        }

        // Whitelist apps
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Whitelist Aplikasi (Diizinkan saat Fokus)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Cari aplikasi terpasang...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        items(apps, key = { it.packageName }) { app ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = app.label,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = app.packageName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = app.isWhitelisted,
                        onCheckedChange = { viewModel.toggleWhitelist(app.packageName, it) }
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionRow(
    title: String,
    description: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isGranted) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Diizinkan",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        } else {
            TextButton(onClick = onClick) {
                Text("Aktifkan")
            }
        }
    }
}
