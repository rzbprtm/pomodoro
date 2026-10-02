package com.pomodoro.focus.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pomodoro.focus.ui.theme.ForestPrimary
import com.pomodoro.focus.ui.theme.ForestSurfaceVariant
import com.pomodoro.focus.ui.timer.TimerPhase

@Composable
fun ForestCircularTimer(
    remainingMs: Long,
    totalMs: Long,
    phase: TimerPhase,
    modifier: Modifier = Modifier,
    size: Dp = 260.dp,
    strokeWidth: Dp = 10.dp
) {
    val progress = if (totalMs > 0) remainingMs.toFloat() / totalMs else 0f
    val minutes = (remainingMs / 1000 / 60).toInt()
    val seconds = ((remainingMs / 1000) % 60).toInt()
    val timeText = "%02d:%02d".format(minutes, seconds)

    val trackColor = ForestSurfaceVariant
    val progressColor = when (phase) {
        TimerPhase.FOCUS -> ForestPrimary
        TimerPhase.REST -> MaterialTheme.colorScheme.tertiary
        TimerPhase.IDLE -> ForestPrimary
    }

    val phaseLabel = when (phase) {
        TimerPhase.FOCUS -> "FOKUS"
        TimerPhase.REST -> "ISTIRAHAT"
        TimerPhase.IDLE -> "SIAP"
    }

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = strokeWidth.toPx()
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            val topLeft = Offset(stroke / 2, stroke / 2)

            // Background track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Progress arc
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = timeText,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = phaseLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
