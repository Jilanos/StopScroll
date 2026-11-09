package com.stopscroll.app.engine

/** Reject rather than wrap or silently change a duration entered by the user. */
fun durationMillis(text: String, wallTimeMillis: Long = 0L): Long? {
    val seconds = text.toLongOrNull() ?: return null
    val maximumSeconds = (Long.MAX_VALUE - wallTimeMillis.coerceAtLeast(0L)) / 1_000L
    return if (seconds in 1..maximumSeconds) seconds * 1_000L else null
}

/** Also protects rules already persisted before input validation was introduced. */
internal fun cooldownDeadline(wallTimeMillis: Long, durationMillis: Long): Long {
    val duration = durationMillis.coerceAtLeast(0L)
    return if (wallTimeMillis > Long.MAX_VALUE - duration) Long.MAX_VALUE else wallTimeMillis + duration
}
