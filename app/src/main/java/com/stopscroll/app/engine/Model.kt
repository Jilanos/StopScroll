package com.stopscroll.app.engine

import kotlinx.serialization.Serializable

@Serializable
data class AppRule(
    val packageName: String,
    val label: String,
    val usageMillis: Long,
    val cooldownMillis: Long,
    val enabled: Boolean = true,
)

/**
 * Persisted per-package runtime state. Consumption is never persisted as "running": every
 * snapshot is taken as if the allowance were paused, so an unobserved interval can never be
 * charged to the user.
 *
 * Fields added after the first release carry defaults so states saved by an older version
 * still decode. Such a state has no [pausedSinceWallMillis]; the engine starts its pause at
 * restore instead of crediting the unobserved interval.
 */
@Serializable
data class PackageRuntimeState(
    val remainingUsageMillis: Long? = null,
    val warned: Boolean = false,
    val cooldownEndsAtWallMillis: Long? = null,
    /**
     * Block duration of the current cycle. During a cooldown it is the running block duration;
     * after a granted extension it is the doubled duration the next expiry will apply. Null
     * means the rule's configured duration.
     */
    val cooldownMillis: Long? = null,
    /**
     * Wall time at which the user left the app with part of the allowance used. Staying away
     * for the block duration refills the allowance. Null while the app is open, while the
     * allowance is full and during a block.
     */
    val pausedSinceWallMillis: Long? = null,
    /** Local epoch day [usedTodayMillis] belongs to. */
    val usageDay: Long? = null,
    /** Allowance consumed on [usageDay], extensions included. */
    val usedTodayMillis: Long = 0L,
) {
    /** Usage on the given local epoch day; zero once the stored day is over. */
    fun usedOn(day: Long): Long = if (usageDay == day) usedTodayMillis else 0L
}

enum class HomeReason {
    ALLOWANCE_EXPIRED,
    COOLDOWN_REENTRY,
}

sealed interface EngineEffect {
    data class ShowWarning(val packageName: String, val remainingMillis: Long) : EngineEffect

    data class GoHome(val packageName: String, val reason: HomeReason) : EngineEffect

    /** An extension was granted: bring the target app back to the foreground. */
    data class LaunchApp(val packageName: String) : EngineEffect
}
