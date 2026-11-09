package com.stopscroll.app.ui

internal fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    return "%d min %02d s".format(totalSeconds / 60, totalSeconds % 60)
}
