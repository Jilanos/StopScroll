package com.stopscroll.app.engine

/**
 * Monotonic time drives allowance consumption; wall time drives cooldown deadlines so they
 * survive a reboot, at the documented cost of being sensitive to manual clock changes.
 */
interface Clock {
    fun elapsedRealtime(): Long

    fun wallTime(): Long
}
