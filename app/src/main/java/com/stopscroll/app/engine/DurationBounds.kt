package com.stopscroll.app.engine

/** Largest duration whose deadline still fits in a wall-clock Long at [wallTimeMillis]. */
fun maxDurationMillis(wallTimeMillis: Long = 0L): Long =
    (Long.MAX_VALUE - wallTimeMillis.coerceAtLeast(0L)) / 1_000L * 1_000L

/** Reject rather than wrap or silently change a duration entered by the user. */
fun durationMillis(text: String, wallTimeMillis: Long = 0L): Long? {
    val seconds = text.toLongOrNull() ?: return null
    val maximumSeconds = maxDurationMillis(wallTimeMillis) / 1_000L
    return if (seconds in 1..maximumSeconds) seconds * 1_000L else null
}

/** Doubles a block duration, capped at the same bound as user input so a deadline never overflows. */
fun doubledDurationMillis(durationMillis: Long, wallTimeMillis: Long = 0L): Long {
    val maximum = maxDurationMillis(wallTimeMillis)
    val duration = durationMillis.coerceAtLeast(0L)
    return if (duration > maximum / 2) maximum else duration * 2
}

/** Also protects rules already persisted before input validation was introduced. */
internal fun cooldownDeadline(wallTimeMillis: Long, durationMillis: Long): Long {
    val duration = durationMillis.coerceAtLeast(0L)
    return if (wallTimeMillis > Long.MAX_VALUE - duration) Long.MAX_VALUE else wallTimeMillis + duration
}
