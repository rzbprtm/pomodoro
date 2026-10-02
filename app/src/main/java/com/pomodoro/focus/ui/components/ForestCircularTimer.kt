package com.pomodoro.focus.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    strokeWidth: Dp = 10.dp,
    onClick: (() -> Unit)? = null
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

    // Infinite transition for animations
    val infiniteTransition = rememberInfiniteTransition(label = "timer_anim")

    // Breathing pulse scale during FOCUS
    val focusPulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (phase == TimerPhase.FOCUS) 1.035f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "focus_pulse"
    )

    // Breathing glow alpha during FOCUS
    val focusGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = if (phase == TimerPhase.FOCUS) 0.65f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "focus_glow"
    )

    // Bouncing offset for Rest Emoji
    val restBounceY by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = if (phase == TimerPhase.REST) 6f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rest_bounce"
    )

    val clickableModifier = if (phase == TimerPhase.IDLE && onClick != null) {
        Modifier
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = true, radius = size / 2),
                onClick = onClick
            )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .size(size)
            .scale(focusPulseScale)
            .then(clickableModifier),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = strokeWidth.toPx()
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            val topLeft = Offset(stroke / 2, stroke / 2)

            // Outer soft glow when focusing
            if (phase == TimerPhase.FOCUS) {
                drawCircle(
                    color = progressColor.copy(alpha = focusGlowAlpha * 0.15f),
                    radius = (this.size.width / 2)
                )
            }

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

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (phase) {
                TimerPhase.FOCUS -> {
                    // Tree focus icon with subtle scale
                    Text(
                        text = "\uD83C\uDF33",
                        fontSize = 24.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
                TimerPhase.REST -> {
                    // Chill animated emoji
                    Text(
                        text = "☕\uD83E\uDDD8",
                        fontSize = 26.sp,
                        modifier = Modifier
                            .offset(y = restBounceY.dp)
                            .padding(bottom = 2.dp)
                    )
                }
                TimerPhase.IDLE -> {
                    Text(
                        text = "\u2728",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            Text(
                text = timeText,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )

            val phaseLabel = when (phase) {
                TimerPhase.FOCUS -> "SEDANG FOKUS..."
                TimerPhase.REST -> "ISTIRAHAT"
                TimerPhase.IDLE -> "KETUK UNTUK ATUR"
            }

            Text(
                text = phaseLabel,
                style = MaterialTheme.typography.labelMedium,
                color = if (phase == TimerPhase.IDLE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.2.sp
            )
        }
    }
}
