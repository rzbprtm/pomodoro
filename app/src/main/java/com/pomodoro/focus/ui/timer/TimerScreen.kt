package com.pomodoro.focus.ui.timer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pomodoro.focus.data.local.entity.TaskEntity
import com.pomodoro.focus.ui.components.ForestCircularTimer
import com.pomodoro.focus.ui.theme.ForestAccentGold
import com.pomodoro.focus.ui.theme.ForestAccentRed
import com.pomodoro.focus.ui.theme.ForestPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    viewModel: TimerViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val todoTasks by viewModel.todoTasks.collectAsState()
    val unfinishedTasks by viewModel.unfinishedTasks.collectAsState()
    var showAddTask by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var showEmergencyDialog by remember { mutableStateOf(false) }
    var showAdjustDialog by remember { mutableStateOf(false) }

    val availableTasks = todoTasks + unfinishedTasks

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Circular Timer (Clickable in IDLE to adjust duration)
        ForestCircularTimer(
            remainingMs = state.remainingMs,
            totalMs = state.totalMs,
            phase = state.phase,
            size = 260.dp,
            onClick = {
                if (state.phase == TimerPhase.IDLE) {
                    showAdjustDialog = true
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Selected task chip (or focus status)
        state.selectedTask?.let { task ->
            AssistChip(
                onClick = { },
                label = {
                    Text(
                        text = "Target: ${task.title}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Rest or Focus dynamic status banner
        when (state.phase) {
            TimerPhase.REST -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Chill dulu bro! \u2615\uD83C\uDF34",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ForestAccentGold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Kamu udah kerja keras tadi. Tarik nafas, minum air, atau santai sejenak.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            TimerPhase.FOCUS -> {
                Text(
                    text = "\uD83C\uDF33 Jangan keluar aplikasi. Pohon fokusmu sedang tumbuh!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            TimerPhase.IDLE -> {
                Text(
                    text = "Fokus: ${state.focusMinutes}m \u2022 Istirahat: ${state.restMinutes}m",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // Action buttons
        when (state.phase) {
            TimerPhase.IDLE -> {
                Button(
                    onClick = { viewModel.startFocus() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mulai Fokus", style = MaterialTheme.typography.labelLarge)
                }
            }
            TimerPhase.FOCUS -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Complete Target button (only if task selected)
                    if (state.selectedTask != null) {
                        Button(
                            onClick = { viewModel.completeTarget() },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Selesai", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    // Emergency Exit
                    OutlinedButton(
                        onClick = { showEmergencyDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ForestAccentRed
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Batal", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            TimerPhase.REST -> {
                if (!state.isRunning) {
                    Button(
                        onClick = { viewModel.startRest() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mulai Istirahat (${state.restMinutes}m)", color = MaterialTheme.colorScheme.background)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Task selection list (only in IDLE)
        AnimatedVisibility(
            visible = state.phase == TimerPhase.IDLE,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pilih Target",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    IconButton(onClick = { showAddTask = true }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Tambah target",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 260.dp)
                ) {
                    items(availableTasks, key = { it.id }) { task ->
                        TaskSelectionCard(
                            task = task,
                            isSelected = state.selectedTask?.id == task.id,
                            onClick = {
                                viewModel.selectTask(
                                    if (state.selectedTask?.id == task.id) null else task
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    // Quick Duration Adjust Dialog
    if (showAdjustDialog) {
        var tempFocus by remember { mutableIntStateOf(state.focusMinutes) }
        var tempRest by remember { mutableIntStateOf(state.restMinutes) }

        AlertDialog(
            onDismissRequest = { showAdjustDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = ForestPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Atur Waktu Pomodoro")
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Quick Presets
                    Text(
                        text = "Preset Cepat:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetChip(
                            label = "50 / 10",
                            selected = tempFocus == 50 && tempRest == 10,
                            onClick = { tempFocus = 50; tempRest = 10 }
                        )
                        PresetChip(
                            label = "25 / 5",
                            selected = tempFocus == 25 && tempRest == 5,
                            onClick = { tempFocus = 25; tempRest = 5 }
                        )
                        PresetChip(
                            label = "60 / 15",
                            selected = tempFocus == 60 && tempRest == 15,
                            onClick = { tempFocus = 60; tempRest = 15 }
                        )
                        PresetChip(
                            label = "90 / 20",
                            selected = tempFocus == 90 && tempRest == 20,
                            onClick = { tempFocus = 90; tempRest = 20 }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Focus slider
                    Text(
                        text = "Waktu Fokus: $tempFocus Menit",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Slider(
                        value = tempFocus.toFloat(),
                        onValueChange = { tempFocus = it.toInt() },
                        valueRange = 5f..120f,
                        steps = 22
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Rest slider
                    Text(
                        text = "Waktu Istirahat: $tempRest Menit",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Slider(
                        value = tempRest.toFloat(),
                        onValueChange = { tempRest = it.toInt() },
                        valueRange = 1f..30f,
                        steps = 28
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.setFocusMinutes(tempFocus)
                    viewModel.setRestMinutes(tempRest)
                    showAdjustDialog = false
                }) {
                    Text("Terapkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdjustDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Add task dialog
    if (showAddTask) {
        AlertDialog(
            onDismissRequest = { showAddTask = false; newTaskTitle = "" },
            title = { Text("Tambah Target Baru") },
            text = {
                OutlinedTextField(
                    value = newTaskTitle,
                    onValueChange = { newTaskTitle = it },
                    placeholder = { Text("Nama target...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newTaskTitle.isNotBlank()) {
                        viewModel.addTask(newTaskTitle.trim())
                        newTaskTitle = ""
                        showAddTask = false
                    }
                }) { Text("Tambah") }
            },
            dismissButton = {
                TextButton(onClick = { showAddTask = false; newTaskTitle = "" }) {
                    Text("Batal")
                }
            }
        )
    }

    // Emergency exit confirmation
    if (showEmergencyDialog) {
        AlertDialog(
            onDismissRequest = { showEmergencyDialog = false },
            title = { Text("Batalkan Sesi?") },
            text = {
                Text("Sesi akan dibatalkan dan target akan disimpan ke Unfinished. Streakmu bisa terpengaruh.")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.emergencyExit()
                    showEmergencyDialog = false
                }) {
                    Text("Ya, Batalkan", color = ForestAccentRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmergencyDialog = false }) {
                    Text("Kembali")
                }
            }
        )
    }
}

@Composable
fun PresetChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskSelectionCard(
    task: TaskEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = isSelected, onClick = onClick)
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (task.status == "UNFINISHED") {
                    Text(
                        text = "Belum selesai",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}
