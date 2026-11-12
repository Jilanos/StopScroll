package com.stopscroll.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.stopscroll.app.R
import com.stopscroll.app.engine.TimerPhase
import com.stopscroll.app.engine.TimerStatus

/** Today's usage, then the allowance (détente) and pause timers of one app. */
@Composable
internal fun RuleTimers(status: TimerStatus, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(R.string.timers_today, formatDuration(status.usedTodayMillis)),
            style = MaterialTheme.typography.labelLarge,
            color = colors.onSurfaceVariant,
        )
        if (status.phase == TimerPhase.DISABLED) return@Column

        Meter(
            label = stringResource(R.string.timer_allowance),
            value = if (status.phase == TimerPhase.BLOCKED) {
                stringResource(R.string.allowance_spent)
            } else {
                stringResource(
                    R.string.allowance_left,
                    formatCountdown(status.allowanceRemainingMillis),
                    formatDuration(status.allowanceTotalMillis),
                )
            },
            fraction = status.allowanceFraction,
            color = colors.secondary,
            track = colors.secondaryContainer,
        )
        Meter(
            label = stringResource(R.string.timer_pause),
            value = when (status.phase) {
                TimerPhase.PAUSING -> stringResource(
                    R.string.pause_left,
                    formatCountdown(status.pauseRemainingMillis),
                    formatDuration(status.pauseTotalMillis),
                )
                TimerPhase.BLOCKED -> stringResource(R.string.pause_blocked, formatCountdown(status.pauseRemainingMillis))
                TimerPhase.IN_USE -> stringResource(R.string.pause_stopped)
                else -> stringResource(R.string.pause_none, formatDuration(status.pauseTotalMillis))
            },
            fraction = status.pauseFraction,
            color = if (status.phase == TimerPhase.BLOCKED) colors.error else colors.tertiary,
            track = colors.tertiaryContainer,
        )
    }
}

@Composable
private fun Meter(label: String, value: String, fraction: Float, color: Color, track: Color) {
    val progress by animateFloatAsState(targetValue = fraction, label = "meter")
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = color,
            trackColor = track,
            strokeCap = StrokeCap.Round,
        )
    }
}

/** Rounds up so a countdown never reads "0 s" while time is left. */
private fun formatCountdown(millis: Long): String = formatDuration((millis + 999) / 1_000 * 1_000)
