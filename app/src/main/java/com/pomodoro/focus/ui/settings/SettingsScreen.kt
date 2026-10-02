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
import androidx.compose.ui.unit.dp
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
    val apps by viewModel.installedApps.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current

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
                text = "Timer",
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
                        valueRange = 5f..60f,
                        steps = 10
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Durasi Istirahat: $restMinutes menit", style = MaterialTheme.typography.bodyLarge)
                    Slider(
                        value = restMinutes.toFloat(),
                        onValueChange = { viewModel.setRestMinutes(it.toInt()) },
                        valueRange = 1f..30f,
                        steps = 5
                    )
                }
            }
        }

        // Permissions
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Izin Aplikasi",
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
                    PermissionRow(
                        title = "Usage Access",
                        description = "Untuk mendeteksi aplikasi foreground",
                        isGranted = PermissionHelper.hasUsageStatsPermission(context),
                        onClick = {
                            context.startActivity(PermissionHelper.usageStatsSettingsIntent())
                        }
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    PermissionRow(
                        title = "Overlay Permission",
                        description = "Untuk menampilkan layar blokir",
                        isGranted = PermissionHelper.hasOverlayPermission(context),
                        onClick = {
                            context.startActivity(PermissionHelper.overlaySettingsIntent(context))
                        }
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    PermissionRow(
                        title = "Battery Optimization",
                        description = "Agar timer tidak mati di background",
                        isGranted = false, // Can't check easily
                        onClick = {
                            try {
                                context.startActivity(PermissionHelper.batteryOptimizationIntent(context))
                            } catch (_: Exception) {}
                        }
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    PermissionRow(
                        title = "Autostart (MIUI/HyperOS)",
                        description = "Agar layanan tetap berjalan setelah reboot",
                        isGranted = false,
                        onClick = {
                            try {
                                context.startActivity(PermissionHelper.miuiAutostartIntent())
                            } catch (_: Exception) {
                                // Not MIUI device
                            }
                        }
                    )
                }
            }
        }

        // Whitelist apps
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Whitelist Aplikasi",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Cari aplikasi...") },
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
                contentDescription = "Granted",
                tint = MaterialTheme.colorScheme.primary
            )
        } else {
            TextButton(onClick = onClick) {
                Text("Aktifkan")
            }
        }
    }
}
