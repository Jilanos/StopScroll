package com.stopscroll.app.engine

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

private const val TARGET = "com.instagram.android"
private const val MINUTE = 60_000L
private const val NOW = 1_700_000_000_000L
private const val TODAY = 19_675L

class TimerStatusTest {

    private val rule = AppRule(TARGET, "Instagram", usageMillis = 5 * MINUTE, cooldownMillis = 10 * MINUTE)

    @Test
    fun `an app never opened is ready with a full allowance`() {
        val status = timerStatus(rule, null, NOW, TODAY)

        assertEquals(TimerStatus(TimerPhase.READY, 0L, 5 * MINUTE, 5 * MINUTE, 0L, 10 * MINUTE), status)
        assertEquals(1f, status.allowanceFraction)
        assertEquals(0f, status.pauseFraction)
    }

    @Test
    fun `a closed app shows its frozen allowance and the running pause`() {
        val state = PackageRuntimeState(
            remainingUsageMillis = 3 * MINUTE,
            pausedSinceWallMillis = NOW - 4 * MINUTE,
            usageDay = TODAY,
            usedTodayMillis = 7 * MINUTE,
        )
        val status = timerStatus(rule, state, NOW, TODAY)

        assertEquals(TimerPhase.PAUSING, status.phase)
        assertEquals(7 * MINUTE, status.usedTodayMillis)
        assertEquals(0.6f, status.allowanceFraction, 1e-6f)
        assertEquals(0.4f, status.pauseFraction, 1e-6f)
        assertEquals(6 * MINUTE, status.pauseRemainingMillis)
    }

    @Test
    fun `a pause already reached reads as a full allowance before the next tick`() {
        val state = PackageRuntimeState(remainingUsageMillis = MINUTE, pausedSinceWallMillis = NOW - 10 * MINUTE)

        assertEquals(TimerPhase.READY, timerStatus(rule, state, NOW, TODAY).phase)
        assertEquals(5 * MINUTE, timerStatus(rule, state, NOW, TODAY).allowanceRemainingMillis)
    }

    @Test
    fun `an open app has its pause stopped`() {
        val status = timerStatus(rule, PackageRuntimeState(remainingUsageMillis = 2 * MINUTE), NOW, TODAY)

        assertEquals(TimerPhase.IN_USE, status.phase)
        assertEquals(0L, status.pauseElapsedMillis)
        assertEquals(2 * MINUTE, status.allowanceRemainingMillis)
    }

    @Test
    fun `a running block shows an empty allowance and the block progress`() {
        val state = PackageRuntimeState(cooldownEndsAtWallMillis = NOW + 15 * MINUTE, cooldownMillis = 20 * MINUTE)
        val status = timerStatus(rule, state, NOW, TODAY)

        assertEquals(TimerPhase.BLOCKED, status.phase)
        assertEquals(0f, status.allowanceFraction)
        assertEquals(20 * MINUTE, status.pauseTotalMillis)
        assertEquals(15 * MINUTE, status.pauseRemainingMillis)
        assertEquals(TimerPhase.READY, timerStatus(rule, state, NOW + 15 * MINUTE, TODAY).phase)
    }

    @Test
    fun `yesterday's usage is not shown today and a disabled rule keeps today's`() {
        val state = PackageRuntimeState(usageDay = TODAY - 1, usedTodayMillis = 9 * MINUTE)
        assertEquals(0L, timerStatus(rule, state, NOW, TODAY).usedTodayMillis)

        val disabled = timerStatus(rule.copy(enabled = false), state.copy(usageDay = TODAY), NOW, TODAY)
        assertEquals(TimerPhase.DISABLED, disabled.phase)
        assertEquals(9 * MINUTE, disabled.usedTodayMillis)
    }

    @Test
    fun `the local day follows the time zone`() {
        val lateEvening = LocalDate.of(2026, 11, 12).atTime(23, 30).toInstant(ZoneOffset.UTC).toEpochMilli()
        assertEquals(LocalDate.of(2026, 11, 12).toEpochDay(), localEpochDay(lateEvening, ZoneOffset.UTC))
        assertEquals(LocalDate.of(2026, 11, 13).toEpochDay(), localEpochDay(lateEvening, ZoneId.of("Europe/Paris")))
    }
}
