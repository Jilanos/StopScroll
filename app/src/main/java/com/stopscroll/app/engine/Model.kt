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
 */
@Serializable
data class PackageRuntimeState(
    val remainingUsageMillis: Long? = null,
    val warned: Boolean = false,
    val cooldownEndsAtWallMillis: Long? = null,
)

enum class HomeReason {
    ALLOWANCE_EXPIRED,
    COOLDOWN_REENTRY,
}

sealed interface EngineEffect {
    data class ShowWarning(val packageName: String, val remainingMillis: Long) : EngineEffect

    data class GoHome(val packageName: String, val reason: HomeReason) : EngineEffect
}
