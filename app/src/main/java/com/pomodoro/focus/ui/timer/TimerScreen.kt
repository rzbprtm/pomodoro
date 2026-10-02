package com.pomodoro.focus.ui.timer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pomodoro.focus.data.local.entity.TaskEntity
import com.pomodoro.focus.ui.components.ForestCircularTimer
import com.pomodoro.focus.ui.theme.ForestAccentRed

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

    val availableTasks = todoTasks + unfinishedTasks

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Circular Timer
        ForestCircularTimer(
            remainingMs = state.remainingMs,
            totalMs = state.totalMs,
            phase = state.phase,
            size = 260.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Selected task chip
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
            Spacer(modifier = Modifier.height(8.dp))
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
                    Text("Mulai Fokus")
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
                // Rest phase - timer auto runs, show info
                Text(
                    text = "Istirahat sejenak...",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (!state.isRunning) {
                    Button(
                        onClick = { viewModel.startRest() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mulai Istirahat")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Task selection list (only in IDLE)
        AnimatedVisibility(visible = state.phase == TimerPhase.IDLE) {
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
                    modifier = Modifier.heightIn(max = 300.dp)
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
