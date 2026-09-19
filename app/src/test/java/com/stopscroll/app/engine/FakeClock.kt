package com.stopscroll.app.engine

class FakeClock(
    private var elapsed: Long = 10_000L,
    private var wall: Long = 1_700_000_000_000L,
) : Clock {

    override fun elapsedRealtime(): Long = elapsed

    override fun wallTime(): Long = wall

    fun advance(millis: Long) {
        elapsed += millis
        wall += millis
    }

    /** Simulates a reboot: monotonic time restarts, wall time keeps running. */
    fun reboot(downtimeMillis: Long) {
        elapsed = 0L
        wall += downtimeMillis
    }
}
