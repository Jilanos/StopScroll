package com.stopscroll.app.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private const val TARGET = "com.instagram.android"
private const val OTHER = "com.example.mail"
private const val LAUNCHER = "com.android.launcher"
private const val MINUTE = 60_000L

/** The allowance (détente) and pause timers of one app, driven by a fake clock. */
class PauseTimerTest {

    private val clock = FakeClock()
    private val rule = AppRule(TARGET, "Instagram", usageMillis = 5 * MINUTE, cooldownMillis = 10 * MINUTE)

    private fun engine(vararg rules: AppRule = arrayOf(rule)) = SessionEngine(clock).apply { setRules(rules.toList()) }

    private fun SessionEngine.run(millis: Long): List<EngineEffect> {
        val effects = mutableListOf<EngineEffect>()
        repeat((millis / 1_000).toInt()) {
            clock.advance(1_000)
            effects += onTick()
        }
        return effects
    }

    private fun SessionEngine.state() = stateOf(TARGET)

    // Transition 1: app open, the allowance runs and the pause is stopped.

    @Test
    fun `while the app is open the allowance runs and no pause accrues`() {
        val engine = engine(rule.copy(usageMillis = 30 * MINUTE, cooldownMillis = MINUTE))
        engine.onForeground(TARGET)
        engine.run(5 * MINUTE)

        assertEquals(25 * MINUTE, engine.state()?.remainingUsageMillis)
        assertNull("pause stopped while open", engine.state()?.pausedSinceWallMillis)
    }

    // Transition 2: app closed, the allowance stops and the pause runs.

    @Test
    fun `leaving freezes the allowance and starts the pause`() {
        val engine = engine()
        engine.onForeground(TARGET)
        engine.run(2 * MINUTE)

        val leftAt = clock.wallTime()
        engine.onForeground(LAUNCHER)
        engine.run(9 * MINUTE)

        assertEquals(3 * MINUTE, engine.state()?.remainingUsageMillis)
        assertEquals(leftAt, engine.state()?.pausedSinceWallMillis)
    }

    @Test
    fun `locking the screen and opening another app also start the pause`() {
        val engine = engine(rule, rule.copy(packageName = OTHER))
        engine.onForeground(TARGET)
        engine.run(MINUTE)
        engine.onScreenLocked()
        assertEquals(clock.wallTime(), engine.state()?.pausedSinceWallMillis)

        engine.onForeground(TARGET)
        engine.run(MINUTE)
        engine.onForeground(OTHER)
        assertEquals(clock.wallTime(), engine.state()?.pausedSinceWallMillis)
        assertEquals(3 * MINUTE, engine.state()?.remainingUsageMillis)
    }

    // Transition 3: reopened before the end of the pause, the pause resets and the allowance resumes.

    @Test
    fun `reopening before the pause is reached resets the pause and resumes the allowance`() {
        val engine = engine()
        engine.onForeground(TARGET)
        engine.run(2 * MINUTE)
        engine.onForeground(LAUNCHER)
        engine.run(10 * MINUTE - 1_000)

        assertTrue(engine.onForeground(TARGET).isEmpty())
        assertNull(engine.state()?.pausedSinceWallMillis)
        assertEquals(3 * MINUTE, engine.state()?.remainingUsageMillis)

        engine.run(MINUTE)
        assertEquals(2 * MINUTE, engine.state()?.remainingUsageMillis)
    }

    @Test
    fun `a reset pause must run again in full from the next exit`() {
        val engine = engine()
        engine.onForeground(TARGET)
        engine.run(2 * MINUTE)
        engine.onForeground(LAUNCHER)
        engine.run(9 * MINUTE)
        engine.onForeground(TARGET)
        engine.onForeground(LAUNCHER)
        engine.run(9 * MINUTE)

        assertEquals(3 * MINUTE, engine.state()?.remainingUsageMillis)
        engine.run(MINUTE)
        assertNull("refilled after a full pause", engine.state()?.remainingUsageMillis)
    }

    // Transition 4: pause reached, the allowance is refilled.

    @Test
    fun `reaching the pause refills the allowance without reopening the app`() {
        val engine = engine()
        engine.onForeground(TARGET)
        engine.run(2 * MINUTE)
        engine.onForeground(LAUNCHER)
        engine.run(10 * MINUTE)

        val state = engine.state()
        assertNull(state?.remainingUsageMillis)
        assertNull(state?.pausedSinceWallMillis)
        assertNull(state?.cooldownEndsAtWallMillis)

        assertTrue(engine.onForeground(TARGET).isEmpty())
        assertEquals(5 * MINUTE, engine.state()?.remainingUsageMillis)
    }

    @Test
    fun `reopening after the pause starts a full allowance`() {
        val engine = engine()
        engine.onForeground(TARGET)
        engine.run(4 * MINUTE)
        engine.onForeground(LAUNCHER)
        clock.advance(10 * MINUTE)

        engine.onForeground(TARGET)
        assertEquals(5 * MINUTE, engine.state()?.remainingUsageMillis)
    }

    @Test
    fun `a refilled allowance warns again`() {
        val engine = engine(rule.copy(usageMillis = 45_000, cooldownMillis = MINUTE))
        engine.onForeground(TARGET)
        assertEquals(1, engine.run(20_000).filterIsInstance<EngineEffect.ShowWarning>().size)
        engine.onForeground(LAUNCHER)
        engine.run(MINUTE)

        engine.onForeground(TARGET)
        assertEquals(1, engine.run(20_000).filterIsInstance<EngineEffect.ShowWarning>().size)
    }

    @Test
    fun `after an extension the pause lasts the doubled block and then clears it`() {
        val engine = engine(rule.copy(usageMillis = 45_000, cooldownMillis = MINUTE))
        engine.onForeground(TARGET)
        engine.run(45_000)
        engine.onForeground("com.stopscroll.app")
        engine.grantExtension(TARGET)
        engine.onForeground(TARGET)
        engine.run(10_000)
        engine.onForeground(LAUNCHER)

        engine.run(MINUTE)
        assertEquals("the configured pause is not enough", 20_000L, engine.state()?.remainingUsageMillis)
        engine.run(MINUTE)
        assertNull(engine.state()?.remainingUsageMillis)
        assertNull(engine.state()?.cooldownMillis)
    }

    @Test
    fun `an untouched extension refills once the doubled pause is over`() {
        val engine = engine(rule.copy(usageMillis = 45_000, cooldownMillis = MINUTE))
        engine.onForeground(TARGET)
        engine.run(45_000)
        engine.onForeground("com.stopscroll.app")
        engine.grantExtension(TARGET)

        engine.run(2 * MINUTE)
        assertNull(engine.state()?.remainingUsageMillis)
        assertNull(engine.state()?.cooldownMillis)
    }

    @Test
    fun `the pause does not apply during a block`() {
        val engine = engine(rule.copy(usageMillis = 45_000, cooldownMillis = MINUTE))
        engine.onForeground(TARGET)
        engine.run(45_000)
        engine.onForeground(LAUNCHER)

        assertNull(engine.state()?.pausedSinceWallMillis)
        assertTrue(engine.state()?.cooldownEndsAtWallMillis != null)
    }

    // Persistence and restore.

    @Test
    fun `the pause keeps running across a reboot`() {
        val first = engine()
        first.onForeground(TARGET)
        first.run(2 * MINUTE)
        first.onForeground(LAUNCHER)
        first.run(4 * MINUTE)
        val snapshot = first.snapshot()

        clock.reboot(downtimeMillis = 6 * MINUTE)
        val restarted = engine()
        restarted.restore(snapshot)
        restarted.onForeground(TARGET)

        assertEquals(5 * MINUTE, restarted.state()?.remainingUsageMillis)
    }

    @Test
    fun `a state saved while the app was open starts its pause at restore`() {
        val first = engine()
        first.onForeground(TARGET)
        first.run(2 * MINUTE)
        val snapshot = first.snapshot()
        assertNull(snapshot[TARGET]?.pausedSinceWallMillis)

        clock.advance(30 * MINUTE)
        val restarted = engine()
        restarted.restore(snapshot)
        assertEquals(clock.wallTime(), restarted.state()?.pausedSinceWallMillis)
        assertEquals(3 * MINUTE, restarted.state()?.remainingUsageMillis)

        restarted.run(10 * MINUTE)
        assertNull(restarted.state()?.remainingUsageMillis)
    }

    @Test
    fun `a state saved by the previous version resumes its allowance and starts its pause at restore`() {
        val engine = engine()
        engine.restore(mapOf(TARGET to PackageRuntimeState(remainingUsageMillis = 42_000L, warned = true)))

        assertEquals(clock.wallTime(), engine.state()?.pausedSinceWallMillis)
        assertEquals(0L, engine.state()?.usedOn(clock.localDay(clock.wallTime())))
        assertTrue(engine.onForeground(TARGET).isEmpty())
        assertEquals(42_000L, engine.state()?.remainingUsageMillis)
    }

    @Test
    fun `a wall clock moved backwards restarts the pause instead of stretching it`() {
        val engine = engine()
        engine.onForeground(TARGET)
        engine.run(MINUTE)
        engine.onForeground(LAUNCHER)
        val restored = engine.snapshot()

        val shifted = SessionEngine(FakeClock(elapsed = 0L, wall = clock.wallTime() - 60 * MINUTE)).apply {
            setRules(listOf(rule))
            restore(restored)
            onTick()
        }
        assertFalse(shifted.stateOf(TARGET)?.pausedSinceWallMillis == restored[TARGET]?.pausedSinceWallMillis)
        assertEquals(4 * MINUTE, shifted.stateOf(TARGET)?.remainingUsageMillis)
    }

    // Daily usage.

    @Test
    fun `today's usage adds up across sessions, extensions included`() {
        val engine = engine(rule.copy(usageMillis = 45_000, cooldownMillis = MINUTE))
        val today = clock.localDay(clock.wallTime())
        engine.onForeground(TARGET)
        engine.run(20_000)
        engine.onForeground(LAUNCHER)
        engine.onForeground(TARGET)
        engine.run(30_000)
        engine.onForeground("com.stopscroll.app")
        engine.grantExtension(TARGET)
        engine.onForeground(TARGET)
        engine.run(10_000)

        assertEquals(55_000L, engine.state()?.usedOn(today))
    }

    @Test
    fun `usage is capped at the allowance actually consumed`() {
        val engine = engine(rule.copy(usageMillis = 2_500, cooldownMillis = MINUTE))
        engine.onForeground(TARGET)
        engine.run(3_000)

        assertEquals(2_500L, engine.state()?.usedOn(clock.localDay(clock.wallTime())))
    }

    @Test
    fun `usage starts again from zero on a new day and survives a restart`() {
        val engine = engine(rule.copy(usageMillis = 30 * MINUTE, cooldownMillis = MINUTE))
        val toMidnight = FakeClock.DAY_MILLIS - Math.floorMod(clock.wallTime(), FakeClock.DAY_MILLIS)
        clock.advance(toMidnight - 2 * MINUTE)
        val firstDay = clock.localDay(clock.wallTime())
        engine.onForeground(TARGET)
        engine.run(5 * MINUTE)

        val secondDay = clock.localDay(clock.wallTime())
        assertEquals(firstDay + 1, secondDay)
        // Each tick is charged to the day it ends on, so the tick ending at midnight counts for the new day.
        val usedSecondDay = engine.state()?.usedOn(secondDay) ?: 0L
        assertTrue(usedSecondDay in 3 * MINUTE..3 * MINUTE + 1_000)
        assertEquals(0L, engine.state()?.usedOn(firstDay))

        val restarted = engine(rule.copy(usageMillis = 30 * MINUTE, cooldownMillis = MINUTE))
        restarted.restore(engine.snapshot())
        assertEquals(usedSecondDay, restarted.state()?.usedOn(secondDay))
    }
}
