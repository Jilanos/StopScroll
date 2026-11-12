package com.stopscroll.app.engine

import java.time.Instant
import java.time.ZoneId

/** Local calendar day of [wallMillis] in [zone], as an epoch day. */
fun localEpochDay(wallMillis: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
    Instant.ofEpochMilli(wallMillis).atZone(zone).toLocalDate().toEpochDay()

enum class TimerPhase {
    /** Full allowance, no pause running. */
    READY,

    /** The app is open: the allowance runs, the pause is stopped. */
    IN_USE,

    /** The app is closed with part of the allowance used: the pause runs. */
    PAUSING,

    /** The allowance ran out: the app is blocked until the pause ends. */
    BLOCKED,

    /** The rule is switched off. */
    DISABLED,
}

/** What the main screen shows for one app at a given instant. */
data class TimerStatus(
    val phase: TimerPhase,
    val usedTodayMillis: Long,
    val allowanceRemainingMillis: Long,
    val allowanceTotalMillis: Long,
    val pauseElapsedMillis: Long,
    val pauseTotalMillis: Long,
) {
    val allowanceFraction: Float get() = fraction(allowanceRemainingMillis, allowanceTotalMillis)

    val pauseFraction: Float get() = fraction(pauseElapsedMillis, pauseTotalMillis)

    val pauseRemainingMillis: Long get() = (pauseTotalMillis - pauseElapsedMillis).coerceAtLeast(0L)

    private fun fraction(part: Long, total: Long): Float =
        if (total <= 0L) 0f else (part.toDouble() / total).toFloat().coerceIn(0f, 1f)
}

/**
 * Reads a persisted state the way the engine would at [wallNow], so the screen shows a pause
 * as reached or a block as over even before the service's next tick.
 */
fun timerStatus(rule: AppRule, state: PackageRuntimeState?, wallNow: Long, today: Long): TimerStatus {
    val used = state?.usedOn(today) ?: 0L
    val pauseTotal = state?.cooldownMillis ?: rule.cooldownMillis
    val ready = TimerStatus(TimerPhase.READY, used, rule.usageMillis, rule.usageMillis, 0L, rule.cooldownMillis)
    if (!rule.enabled) return ready.copy(phase = TimerPhase.DISABLED)
    if (state == null) return ready

    val cooldownEnd = state.cooldownEndsAtWallMillis
    if (cooldownEnd != null) {
        if (wallNow >= cooldownEnd) return ready
        val left = (cooldownEnd - wallNow).coerceAtMost(pauseTotal)
        return TimerStatus(TimerPhase.BLOCKED, used, 0L, rule.usageMillis, pauseTotal - left, pauseTotal)
    }

    val remaining = state.remainingUsageMillis?.coerceAtLeast(0L) ?: return ready
    val allowanceTotal = maxOf(rule.usageMillis, remaining)
    val pausedSince = state.pausedSinceWallMillis
        ?: return TimerStatus(TimerPhase.IN_USE, used, remaining, allowanceTotal, 0L, pauseTotal)

    val elapsed = (wallNow - pausedSince).coerceAtLeast(0L)
    if (elapsed >= pauseTotal) return ready
    return TimerStatus(TimerPhase.PAUSING, used, remaining, allowanceTotal, elapsed, pauseTotal)
}
