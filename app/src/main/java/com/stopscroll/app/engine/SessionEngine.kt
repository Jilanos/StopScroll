package com.stopscroll.app.engine

/**
 * Pure state machine for per-package allowance and pause timers and cooldowns.
 *
 * Two timers per package:
 * - the allowance (temps de détente) runs only while the app is in the foreground;
 * - the pause runs only while the app is closed, from the moment the user leaves it with part
 *   of the allowance used. Reopening before the pause is reached resets the pause and resumes
 *   the remaining allowance. Staying away for the block duration refills the allowance and
 *   counts as a completed block (a pending doubled duration is cleared).
 *
 * Exhausting the allowance starts a cooldown (block) during which entries are sent HOME.
 *
 * Not thread safe: callers must serialize every entry point.
 */
class SessionEngine(private val clock: Clock) {

    private val rules = LinkedHashMap<String, AppRule>()
    private val states = LinkedHashMap<String, PackageRuntimeState>()

    private var runningPackage: String? = null
    private var runningSinceElapsed: Long = 0L
    private var foregroundPackage: String? = null

    /**
     * A removed rule drops its state. A disabled rule loses its allowance, pause and cooldown
     * but keeps today's usage.
     */
    fun setRules(newRules: List<AppRule>) {
        rules.clear()
        newRules.forEach { rules[it.packageName] = it }
        for (packageName in states.keys.toList()) {
            val rule = rules[packageName]
            if (rule != null && rule.enabled) continue
            val state = if (packageName == runningPackage) chargeRunning() else states.getValue(packageName)
            if (packageName == runningPackage) runningPackage = null
            val usageOnly = PackageRuntimeState(usageDay = state.usageDay, usedTodayMillis = state.usedTodayMillis)
            if (rule == null || usageOnly.usedTodayMillis == 0L) {
                states.remove(packageName)
            } else {
                states[packageName] = usageOnly
            }
        }
    }

    /**
     * A state saved while its app was open has no pause start: the pause starts now, so time
     * the service did not observe is neither charged to the allowance nor credited to the pause.
     * This also covers states saved by versions without the pause timer.
     */
    fun restore(snapshot: Map<String, PackageRuntimeState>) {
        val now = clock.wallTime()
        states.clear()
        snapshot.forEach { (packageName, state) ->
            states[packageName] = if (state.isPartlyUsed() && state.pausedSinceWallMillis == null) {
                state.copy(pausedSinceWallMillis = now)
            } else {
                state
            }
        }
        runningPackage = null
        foregroundPackage = null
    }

    /** Exact paused view of the current state, safe to persist at any moment. */
    fun snapshot(): Map<String, PackageRuntimeState> {
        val now = clock.elapsedRealtime()
        return states.mapValues { (packageName, state) ->
            if (packageName == runningPackage && state.remainingUsageMillis != null) {
                val charged = charge(state, now - runningSinceElapsed)
                charged.copy(remainingUsageMillis = charged.remainingUsageMillis?.coerceAtLeast(0L))
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

        if (runningPackage != null && runningPackage != packageName) stopRunning()
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
        states[packageName] = state.copy(
            remainingUsageMillis = remaining,
            warned = alreadyWarned || warnNow,
            cooldownEndsAtWallMillis = null,
            pausedSinceWallMillis = null,
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
        expireTimers()
        if (rules[packageName]?.enabled != true) return null
        val state = states[packageName] ?: return null
        if (state.cooldownEndsAtWallMillis == null) return null
        val blockMillis = state.cooldownMillis ?: return null
        return doubledDurationMillis(blockMillis, clock.wallTime())
    }

    /**
     * Cancels the running cooldown, grants [EXTENSION_MILLIS] of allowance and doubles the block
     * duration that the next expiry applies. Each grant doubles again, capped by
     * [maxDurationMillis]. The doubled duration is reset once a block runs to its end, or once
     * the user stays away from the app for that doubled duration.
     */
    fun grantExtension(packageName: String): List<EngineEffect> {
        val effects = advance().toMutableList()
        val doubled = extensionOffer(packageName) ?: return effects
        states[packageName] = states.getValue(packageName).copy(
            remainingUsageMillis = EXTENSION_MILLIS,
            warned = true,
            cooldownEndsAtWallMillis = null,
            cooldownMillis = doubled,
            pausedSinceWallMillis = clock.wallTime(),
        )
        effects += EngineEffect.LaunchApp(packageName)
        return effects
    }

    private fun advance(): List<EngineEffect> {
        expireTimers()
        val packageName = runningPackage ?: return emptyList()
        val rule = rules[packageName]
        val state = states[packageName]
        if (rule == null || state?.remainingUsageMillis == null) {
            runningPackage = null
            return emptyList()
        }

        val charged = chargeRunning()
        val remaining = charged.remainingUsageMillis ?: 0L

        if (remaining <= 0L) {
            val blockMillis = state.cooldownMillis ?: rule.cooldownMillis
            states[packageName] = charged.copy(
                remainingUsageMillis = null,
                warned = false,
                cooldownEndsAtWallMillis = cooldownDeadline(clock.wallTime(), blockMillis),
                cooldownMillis = blockMillis,
                pausedSinceWallMillis = null,
            )
            runningPackage = null
            return if (foregroundPackage == packageName) {
                listOf(EngineEffect.GoHome(packageName, HomeReason.ALLOWANCE_EXPIRED))
            } else {
                emptyList()
            }
        }

        val warnNow = !state.warned && remaining <= WARNING_LEAD_MILLIS
        states[packageName] = charged.copy(warned = state.warned || warnNow)
        return if (warnNow) listOf(EngineEffect.ShowWarning(packageName, remaining)) else emptyList()
    }

    /** Charges the running package up to now and stores the result. */
    private fun chargeRunning(): PackageRuntimeState {
        val packageName = checkNotNull(runningPackage)
        val now = clock.elapsedRealtime()
        val charged = charge(states.getValue(packageName), now - runningSinceElapsed)
        runningSinceElapsed = now
        states[packageName] = charged
        return charged
    }

    /** Leaving the app freezes the allowance and starts the pause. */
    private fun stopRunning() {
        val packageName = runningPackage ?: return
        runningPackage = null
        val state = states[packageName] ?: return
        if (state.isPartlyUsed()) states[packageName] = state.copy(pausedSinceWallMillis = clock.wallTime())
    }

    /**
     * Subtracts [elapsedMillis] from the allowance and adds the consumed part to today's usage.
     * The remaining allowance may go negative; the caller treats that as expiry.
     */
    private fun charge(state: PackageRuntimeState, elapsedMillis: Long): PackageRuntimeState {
        val remaining = state.remainingUsageMillis ?: return state
        val elapsed = elapsedMillis.coerceAtLeast(0L)
        val consumed = elapsed.coerceAtMost(remaining.coerceAtLeast(0L))
        val day = clock.localDay(clock.wallTime())
        return state.copy(
            remainingUsageMillis = remaining - elapsed,
            usageDay = day,
            usedTodayMillis = state.usedOn(day) + consumed,
        )
    }

    private fun expireTimers() {
        val now = clock.wallTime()
        for ((packageName, state) in states.entries.toList()) {
            val end = state.cooldownEndsAtWallMillis
            if (end != null) {
                if (now >= end) {
                    states[packageName] = state.copy(cooldownEndsAtWallMillis = null, cooldownMillis = null)
                }
                continue
            }
            val pausedSince = state.pausedSinceWallMillis ?: continue
            if (packageName == runningPackage) continue
            val rule = rules[packageName]?.takeIf { it.enabled } ?: continue
            if (now < pausedSince) {
                // The wall clock moved backwards: restart the pause rather than stretch it.
                states[packageName] = state.copy(pausedSinceWallMillis = now)
                continue
            }
            if (now - pausedSince >= (state.cooldownMillis ?: rule.cooldownMillis)) {
                states[packageName] = state.copy(
                    remainingUsageMillis = null,
                    warned = false,
                    cooldownMillis = null,
                    pausedSinceWallMillis = null,
                )
            }
        }
    }

    private fun PackageRuntimeState.isPartlyUsed(): Boolean =
        remainingUsageMillis != null && cooldownEndsAtWallMillis == null

    companion object {
        const val WARNING_LEAD_MILLIS = 30_000L
        const val EXTENSION_MILLIS = 30_000L
    }
}
