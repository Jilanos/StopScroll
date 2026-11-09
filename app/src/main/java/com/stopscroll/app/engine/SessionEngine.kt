package com.stopscroll.app.engine

/**
 * Pure state machine for retained foreground allowances and per-package cooldowns.
 *
 * Not thread safe: callers must serialize every entry point.
 */
class SessionEngine(private val clock: Clock) {

    private val rules = LinkedHashMap<String, AppRule>()
    private val states = LinkedHashMap<String, PackageRuntimeState>()

    private var runningPackage: String? = null
    private var runningSinceElapsed: Long = 0L
    private var foregroundPackage: String? = null

    fun setRules(newRules: List<AppRule>) {
        rules.clear()
        newRules.forEach { rules[it.packageName] = it }
        for (packageName in states.keys.toList()) {
            val rule = rules[packageName]
            if (rule == null || !rule.enabled) {
                states.remove(packageName)
                if (runningPackage == packageName) runningPackage = null
            }
        }
    }

    fun restore(snapshot: Map<String, PackageRuntimeState>) {
        states.clear()
        states.putAll(snapshot)
        runningPackage = null
        foregroundPackage = null
    }

    /** Exact paused view of the current state, safe to persist at any moment. */
    fun snapshot(): Map<String, PackageRuntimeState> {
        val now = clock.elapsedRealtime()
        return states.mapValues { (packageName, state) ->
            val remaining = state.remainingUsageMillis
            if (packageName == runningPackage && remaining != null) {
                state.copy(remainingUsageMillis = (remaining - (now - runningSinceElapsed)).coerceAtLeast(0L))
            } else {
                state
            }
        }
    }

    fun stateOf(packageName: String): PackageRuntimeState? = snapshot()[packageName]

    fun canShowWarning(packageName: String): Boolean =
        runningPackage == packageName && foregroundPackage == packageName &&
            (stateOf(packageName)?.remainingUsageMillis ?: 0L) > 0L

    fun onTick(): List<EngineEffect> = advance()

    fun onScreenLocked(): List<EngineEffect> = onForeground(null)

    /**
     * @param packageName the confirmed ordinary foreground owner, or null when no monitored
     * app owns the foreground (launcher, other app, locked screen).
     */
    fun onForeground(packageName: String?): List<EngineEffect> {
        foregroundPackage = packageName
        val effects = advance().toMutableList()

        if (runningPackage != null && runningPackage != packageName) {
            runningPackage = null
        }
        if (packageName == null) return effects

        val rule = rules[packageName]?.takeIf { it.enabled } ?: return effects
        if (runningPackage == packageName) return effects

        val state = states[packageName] ?: PackageRuntimeState()
        val cooldownEnd = state.cooldownEndsAtWallMillis
        if (cooldownEnd != null) {
            effects += EngineEffect.GoHome(packageName, HomeReason.COOLDOWN_REENTRY)
            return effects
        }

        val remaining = state.remainingUsageMillis ?: rule.usageMillis
        val alreadyWarned = state.remainingUsageMillis != null && state.warned
        val warnNow = !alreadyWarned && remaining <= WARNING_LEAD_MILLIS
        states[packageName] = PackageRuntimeState(
            remainingUsageMillis = remaining,
            warned = alreadyWarned || warnNow,
            cooldownEndsAtWallMillis = null,
            cooldownMillis = state.cooldownMillis,
        )
        runningPackage = packageName
        runningSinceElapsed = clock.elapsedRealtime()
        if (warnNow) effects += EngineEffect.ShowWarning(packageName, remaining)
        return effects
    }

    /**
     * Doubled block duration the user would accept in exchange for an extension, or null when
     * no extension can be granted (no cooldown running for this package, or a legacy state).
     */
    fun extensionOffer(packageName: String): Long? {
        expireCooldowns()
        if (rules[packageName]?.enabled != true) return null
        val state = states[packageName] ?: return null
        if (state.cooldownEndsAtWallMillis == null) return null
        val blockMillis = state.cooldownMillis ?: return null
        return doubledDurationMillis(blockMillis, clock.wallTime())
    }

    /**
     * Cancels the running cooldown, grants [EXTENSION_MILLIS] of allowance and doubles the block
     * duration that the next expiry applies. Each grant doubles again, capped by
     * [maxDurationMillis]. The doubled duration is reset once a block runs to its end.
     */
    fun grantExtension(packageName: String): List<EngineEffect> {
        val effects = advance().toMutableList()
        val doubled = extensionOffer(packageName) ?: return effects
        states[packageName] = PackageRuntimeState(
            remainingUsageMillis = EXTENSION_MILLIS,
            warned = true,
            cooldownEndsAtWallMillis = null,
            cooldownMillis = doubled,
        )
        effects += EngineEffect.LaunchApp(packageName)
        return effects
    }

    private fun advance(): List<EngineEffect> {
        expireCooldowns()
        val packageName = runningPackage ?: return emptyList()
        val rule = rules[packageName]
        val state = states[packageName]
        val remainingBefore = state?.remainingUsageMillis
        if (rule == null || state == null || remainingBefore == null) {
            runningPackage = null
            return emptyList()
        }

        val now = clock.elapsedRealtime()
        val remaining = remainingBefore - (now - runningSinceElapsed)
        runningSinceElapsed = now

        if (remaining <= 0L) {
            val blockMillis = state.cooldownMillis ?: rule.cooldownMillis
            states[packageName] = PackageRuntimeState(
                remainingUsageMillis = null,
                warned = false,
                cooldownEndsAtWallMillis = cooldownDeadline(clock.wallTime(), blockMillis),
                cooldownMillis = blockMillis,
            )
            runningPackage = null
            return if (foregroundPackage == packageName) {
                listOf(EngineEffect.GoHome(packageName, HomeReason.ALLOWANCE_EXPIRED))
            } else {
                emptyList()
            }
        }

        val warnNow = !state.warned && remaining <= WARNING_LEAD_MILLIS
        states[packageName] = state.copy(remainingUsageMillis = remaining, warned = state.warned || warnNow)
        return if (warnNow) listOf(EngineEffect.ShowWarning(packageName, remaining)) else emptyList()
    }

    private fun expireCooldowns() {
        val now = clock.wallTime()
        for ((packageName, state) in states.entries.toList()) {
            val end = state.cooldownEndsAtWallMillis ?: continue
            if (now >= end) {
                states[packageName] = state.copy(cooldownEndsAtWallMillis = null, cooldownMillis = null)
            }
        }
    }

    companion object {
        const val WARNING_LEAD_MILLIS = 30_000L
        const val EXTENSION_MILLIS = 30_000L
    }
}
