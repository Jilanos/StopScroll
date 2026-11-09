package com.stopscroll.app.ui

/**
 * Short French duration such as "45 s", "5 min", "1 min 30 s", "2 h 15 min" or "3 j 4 h". Parts are
 * joined with no-break spaces so a duration never wraps across lines.
 */
internal fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val days = totalSeconds / 86_400
    val hours = totalSeconds % 86_400 / 3_600
    val minutes = totalSeconds % 3_600 / 60
    val seconds = totalSeconds % 60
    return when {
        days > 0 -> join(days to "j", hours to "h")
        hours > 0 -> join(hours to "h", minutes to "min")
        minutes > 0 -> join(minutes to "min", seconds to "s")
        else -> "$seconds${NBSP}s"
    }
}

private fun join(major: Pair<Long, String>, minor: Pair<Long, String>): String =
    if (minor.first == 0L) {
        "${major.first}$NBSP${major.second}"
    } else {
        "${major.first}$NBSP${major.second}$NBSP${minor.first}$NBSP${minor.second}"
    }

private const val NBSP = '\u00A0'
