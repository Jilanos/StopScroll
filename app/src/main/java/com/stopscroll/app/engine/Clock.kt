package com.stopscroll.app.engine

/**
 * Monotonic time drives allowance consumption; wall time drives cooldown deadlines and the
 * pause away from an app so they survive a reboot, at the documented cost of being sensitive
 * to manual clock changes.
 */
interface Clock {
    fun elapsedRealtime(): Long

    fun wallTime(): Long

    /** Local calendar day of [wallMillis], as an epoch day, used to group daily usage. */
    fun localDay(wallMillis: Long): Long
}
