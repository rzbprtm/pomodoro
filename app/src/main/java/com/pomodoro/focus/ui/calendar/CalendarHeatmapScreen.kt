package com.pomodoro.focus.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pomodoro.focus.ui.components.StreakBadge
import com.pomodoro.focus.ui.theme.*
import com.pomodoro.focus.util.StreakCalculator
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarHeatmapScreen(
    viewModel: CalendarViewModel,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val recordMap = remember(state.records) {
        state.records.associateBy { it.dateKey }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Streak badge
        StreakBadge(
            streak = state.streak,
            message = state.streakMessage
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendDot(
                color = if (isDarkTheme) HeatmapEmpty else HeatmapEmptyLight,
                label = "Kosong"
            )
            Spacer(modifier = Modifier.width(16.dp))
            LegendDot(
                color = if (isDarkTheme) HeatmapPartial else HeatmapPartialLight,
                label = "Sesi selesai"
            )
            Spacer(modifier = Modifier.width(16.dp))
            LegendDot(
                color = if (isDarkTheme) HeatmapPerfect else HeatmapPerfectLight,
                label = "Sesi + Target"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Month navigation
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.previousMonth() }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Bulan sebelumnya")
                    }
                    Text(
                        text = state.currentMonth.month.getDisplayName(
                            TextStyle.FULL, Locale("id")
                        ) + " " + state.currentMonth.year,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(onClick = { viewModel.nextMonth() }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Bulan berikutnya")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Day headers
                Row(modifier = Modifier.fillMaxWidth()) {
                    val dayNames = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
                    dayNames.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Calendar grid
                val month = state.currentMonth
                val firstDay = month.atDay(1)
                val daysInMonth = month.lengthOfMonth()
                val startDayOfWeek = (firstDay.dayOfWeek.value - 1) // Mon=0

                val totalCells = startDayOfWeek + daysInMonth
                val rows = (totalCells + 6) / 7

                for (row in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0..6) {
                            val cellIndex = row * 7 + col
                            val dayNum = cellIndex - startDayOfWeek + 1

                            if (dayNum in 1..daysInMonth) {
                                val date = month.atDay(dayNum)
                                val dateKey = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                                val record = recordMap[dateKey]
                                val tier = StreakCalculator.dayTier(record)
                                val isToday = date == LocalDate.now()

                                val bgColor = when (tier) {
                                    2 -> if (isDarkTheme) HeatmapPerfect else HeatmapPerfectLight
                                    1 -> if (isDarkTheme) HeatmapPartial else HeatmapPartialLight
                                    else -> if (isDarkTheme) HeatmapEmpty else HeatmapEmptyLight
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(bgColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayNum.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            tier >= 1 && isDarkTheme -> ForestBackground
                                            tier >= 1 -> LightBackground
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LegendDot(
    color: androidx.compose.ui.graphics.Color,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
