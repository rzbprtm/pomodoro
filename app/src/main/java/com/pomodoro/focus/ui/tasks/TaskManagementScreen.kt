package com.pomodoro.focus.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pomodoro.focus.data.local.entity.TaskEntity
import com.pomodoro.focus.ui.theme.ForestAccentGold
import com.pomodoro.focus.ui.theme.ForestAccentRed
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TaskManagementScreen(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val todoTasks by viewModel.todoTasks.collectAsState()
    val unfinishedTasks by viewModel.unfinishedTasks.collectAsState()
    val achievedTasks by viewModel.achievedTasks.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddTask by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }

    val tabs = listOf("Todo", "Unfinished", "Achieved")

    Column(modifier = modifier.fillMaxSize()) {
        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        val count = when (index) {
                            0 -> todoTasks.size
                            1 -> unfinishedTasks.size
                            2 -> achievedTasks.size
                            else -> 0
                        }
                        Text("$title ($count)")
                    }
                )
            }
        }

        // Task list
        Box(modifier = Modifier.weight(1f)) {
            val tasks = when (selectedTab) {
                0 -> todoTasks
                1 -> unfinishedTasks
                2 -> achievedTasks
                else -> emptyList()
            }

            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (selectedTab) {
                            0 -> "Belum ada target. Tambah yang baru!"
                            1 -> "Tidak ada target tertunda."
                            2 -> "Belum ada target tercapai."
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onAchieve = { viewModel.markAchieved(task) },
                            onResetTodo = { viewModel.resetToTodo(task) },
                            onDelete = { viewModel.deleteTask(task) }
                        )
                    }
                }
            }
        }

        // FAB for adding task (only in Todo tab)
        if (selectedTab == 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                FloatingActionButton(
                    onClick = { showAddTask = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah target")
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: TaskEntity,
    onAchieve: () -> Unit,
    onResetTodo: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id")) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.status == "ACHIEVED") TextDecoration.LineThrough else TextDecoration.None
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Focus time spent
                if (task.totalFocusTimeMs > 0) {
                    val mins = task.totalFocusTimeMs / 1000 / 60
                    Text(
                        text = "Fokus: ${mins} menit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Completed timestamp
                task.completedAt?.let {
                    Text(
                        text = "Selesai: ${dateFormat.format(Date(it))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Status badge
                val (badgeText, badgeColor) = when (task.status) {
                    "TODO" -> "Todo" to MaterialTheme.colorScheme.primary
                    "UNFINISHED" -> "Tertunda" to ForestAccentGold
                    "ACHIEVED" -> "Tercapai" to MaterialTheme.colorScheme.primary
                    else -> "" to MaterialTheme.colorScheme.primary
                }
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = badgeColor
                )
            }

            // Actions
            when (task.status) {
                "TODO" -> {
                    IconButton(onClick = onAchieve) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Selesai",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Hapus",
                            tint = ForestAccentRed
                        )
                    }
                }
                "UNFINISHED" -> {
                    IconButton(onClick = onResetTodo) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Lanjutkan",
                            tint = ForestAccentGold
                        )
                    }
                    IconButton(onClick = onAchieve) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Selesai",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                "ACHIEVED" -> {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Hapus",
                            tint = ForestAccentRed
                        )
                    }
                }
            }
        }
    }
}
