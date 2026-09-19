package com.stopscroll.app.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private const val INSTAGRAM = "com.instagram.android"
private const val OTHER_APP = "com.example.mail"
private const val LAUNCHER = "com.android.launcher"

private const val MINUTE = 60_000L

class SessionEngineTest {

    private val clock = FakeClock()

    private fun engine(vararg rules: AppRule): SessionEngine =
        SessionEngine(clock).apply { setRules(rules.toList()) }

    private fun rule(
        packageName: String = INSTAGRAM,
        usageMillis: Long = 5 * MINUTE,
        cooldownMillis: Long = 10 * MINUTE,
        enabled: Boolean = true,
    ) = AppRule(packageName, packageName, usageMillis, cooldownMillis, enabled)

    /** Runs one-second ticks for the given duration and returns every effect produced. */
    private fun SessionEngine.run(millis: Long): List<EngineEffect> {
        val effects = mutableListOf<EngineEffect>()
        repeat((millis / 1_000).toInt()) {
            clock.advance(1_000)
            effects += onTick()
        }
        return effects
    }

    @Test
    fun `duplicate foreground events do not restart the allowance`() {
        val engine = engine(rule())
        engine.onForeground(INSTAGRAM)
        engine.run(2 * MINUTE)
        repeat(5) { engine.onForeground(INSTAGRAM) }

        assertEquals(3 * MINUTE, engine.stateOf(INSTAGRAM)?.remainingUsageMillis)
    }

    @Test
    fun `a non selected app never acquires a session`() {
        val engine = engine(rule())
        val effects = engine.onForeground(OTHER_APP) + engine.run(10 * MINUTE)

        assertTrue(effects.isEmpty())
        assertNull(engine.stateOf(OTHER_APP))
    }

    @Test
    fun `leaving pauses consumption and reopening resumes the remaining allowance`() {
        val engine = engine(rule())
        engine.onForeground(INSTAGRAM)
        engine.run(2 * MINUTE)

        engine.onForeground(LAUNCHER)
        engine.run(10 * MINUTE)
        engine.onForeground(INSTAGRAM)

        assertEquals(3 * MINUTE, engine.stateOf(INSTAGRAM)?.remainingUsageMillis)
    }

    @Test
    fun `screen lock pauses consumption`() {
        val engine = engine(rule())
        engine.onForeground(INSTAGRAM)
        engine.run(MINUTE)

        engine.onScreenLocked()
        engine.run(30 * MINUTE)

        assertEquals(4 * MINUTE, engine.stateOf(INSTAGRAM)?.remainingUsageMillis)
    }

    @Test
    fun `a long session warns once at thirty seconds remaining`() {
        val engine = engine(rule(usageMillis = 45_000))
        assertTrue(engine.onForeground(INSTAGRAM).isEmpty())

        val beforeWarning = engine.run(14_000)
        assertTrue(beforeWarning.isEmpty())

        val warnings = engine.run(2_000).filterIsInstance<EngineEffect.ShowWarning>()
        assertEquals(1, warnings.size)
        assertEquals(30_000L, warnings.single().remainingMillis)
        assertEquals(29_000L, engine.stateOf(INSTAGRAM)?.remainingUsageMillis)
    }

    @Test
    fun `a short session warns immediately on opening`() {
        val engine = engine(rule(usageMillis = 20_000))
        val effects = engine.onForeground(INSTAGRAM)

        assertEquals(listOf(EngineEffect.ShowWarning(INSTAGRAM, 20_000)), effects)
        assertEquals(20_000L, engine.stateOf(INSTAGRAM)?.remainingUsageMillis)
    }

    @Test
    fun `reopening after a warning does not warn again`() {
        val engine = engine(rule(usageMillis = 45_000))
        engine.onForeground(INSTAGRAM)
        engine.run(20_000)

        engine.onForeground(LAUNCHER)
        val onReturn = engine.onForeground(INSTAGRAM) + engine.run(10_000)

        assertTrue(onReturn.filterIsInstance<EngineEffect.ShowWarning>().isEmpty())
    }

    @Test
    fun `expiry sends home and opens a cooldown`() {
        val engine = engine(rule(usageMillis = 45_000, cooldownMillis = 60_000))
        engine.onForeground(INSTAGRAM)
        val effects = engine.run(45_000)

        assertEquals(
            listOf(EngineEffect.GoHome(INSTAGRAM, HomeReason.ALLOWANCE_EXPIRED)),
            effects.filterIsInstance<EngineEffect.GoHome>(),
        )
        assertNull(engine.stateOf(INSTAGRAM)?.remainingUsageMillis)
        assertEquals(clock.wallTime() + 60_000, engine.stateOf(INSTAGRAM)?.cooldownEndsAtWallMillis)
    }

    @Test
    fun `expiry observed while another app is foreground commits cooldown without ejecting it`() {
        val engine = engine(rule(usageMillis = 45_000, cooldownMillis = 60_000))
        engine.onForeground(INSTAGRAM)
        engine.run(40_000)

        clock.advance(10_000)
        val onSwitch = engine.onForeground(OTHER_APP)
        assertTrue(onSwitch.isEmpty())

        val onReentry = engine.onForeground(INSTAGRAM)
        assertEquals(listOf(EngineEffect.GoHome(INSTAGRAM, HomeReason.COOLDOWN_REENTRY)), onReentry)
    }

    @Test
    fun `repeated attempts during cooldown do not extend its deadline`() {
        val engine = engine(rule(usageMillis = 45_000, cooldownMillis = 60_000))
        engine.onForeground(INSTAGRAM)
        engine.run(45_000)
        val deadline = engine.stateOf(INSTAGRAM)?.cooldownEndsAtWallMillis

        repeat(5) {
            clock.advance(5_000)
            val effects = engine.onForeground(INSTAGRAM)
            assertEquals(listOf(EngineEffect.GoHome(INSTAGRAM, HomeReason.COOLDOWN_REENTRY)), effects)
            engine.onForeground(LAUNCHER)
        }

        assertEquals(deadline, engine.stateOf(INSTAGRAM)?.cooldownEndsAtWallMillis)
    }

    @Test
    fun `a new full allowance starts once the cooldown expires`() {
        val engine = engine(rule(usageMillis = 45_000, cooldownMillis = 60_000))
        engine.onForeground(INSTAGRAM)
        engine.run(45_000)

        clock.advance(60_000)
        val effects = engine.onForeground(INSTAGRAM)

        assertTrue(effects.isEmpty())
        assertEquals(45_000L, engine.stateOf(INSTAGRAM)?.remainingUsageMillis)
        assertNull(engine.stateOf(INSTAGRAM)?.cooldownEndsAtWallMillis)
    }

    @Test
    fun `independent packages keep separate allowances and cooldowns`() {
        val engine = engine(
            rule(packageName = INSTAGRAM, usageMillis = 45_000, cooldownMillis = 60_000),
            rule(packageName = OTHER_APP, usageMillis = 120_000, cooldownMillis = 30_000),
        )

        engine.onForeground(INSTAGRAM)
        engine.run(45_000)
        engine.onForeground(OTHER_APP)
        engine.run(10_000)

        assertEquals(110_000L, engine.stateOf(OTHER_APP)?.remainingUsageMillis)
        assertNull(engine.stateOf(OTHER_APP)?.cooldownEndsAtWallMillis)
        assertNull(engine.stateOf(INSTAGRAM)?.remainingUsageMillis)
        assertTrue(engine.stateOf(INSTAGRAM)?.cooldownEndsAtWallMillis != null)
    }

    @Test
    fun `restart restores the remaining allowance without charging disconnected time`() {
        val first = engine(rule())
        first.onForeground(INSTAGRAM)
        first.run(2 * MINUTE)
        val snapshot = first.snapshot()

        clock.advance(30 * MINUTE)
        val restarted = engine(rule())
        restarted.restore(snapshot)
        restarted.onForeground(INSTAGRAM)

        assertEquals(3 * MINUTE, restarted.stateOf(INSTAGRAM)?.remainingUsageMillis)
    }

    @Test
    fun `warning state survives a restart`() {
        val first = engine(rule(usageMillis = 45_000))
        first.onForeground(INSTAGRAM)
        first.run(20_000)
        val snapshot = first.snapshot()

        val restarted = engine(rule(usageMillis = 45_000))
        restarted.restore(snapshot)
        val effects = restarted.onForeground(INSTAGRAM) + restarted.run(10_000)

        assertTrue(effects.filterIsInstance<EngineEffect.ShowWarning>().isEmpty())
        assertEquals(15_000L, restarted.stateOf(INSTAGRAM)?.remainingUsageMillis)
    }

    @Test
    fun `cooldown keeps running across a reboot and expires by elapsed wall time`() {
        val first = engine(rule(usageMillis = 45_000, cooldownMillis = 60_000))
        first.onForeground(INSTAGRAM)
        first.run(45_000)
        val snapshot = first.snapshot()

        clock.reboot(downtimeMillis = 30_000)
        val restarted = engine(rule(usageMillis = 45_000, cooldownMillis = 60_000))
        restarted.restore(snapshot)
        assertEquals(
            listOf(EngineEffect.GoHome(INSTAGRAM, HomeReason.COOLDOWN_REENTRY)),
            restarted.onForeground(INSTAGRAM),
        )

        clock.advance(31_000)
        restarted.onForeground(LAUNCHER)
        assertTrue(restarted.onForeground(INSTAGRAM).isEmpty())
        assertEquals(45_000L, restarted.stateOf(INSTAGRAM)?.remainingUsageMillis)
    }

    @Test
    fun `disabling a rule clears its allowance and cooldown`() {
        val engine = engine(rule(usageMillis = 45_000, cooldownMillis = 60_000))
        engine.onForeground(INSTAGRAM)
        engine.run(45_000)

        engine.setRules(listOf(rule(usageMillis = 45_000, cooldownMillis = 60_000, enabled = false)))
        assertNull(engine.stateOf(INSTAGRAM))

        assertTrue(engine.onForeground(INSTAGRAM).isEmpty())
    }

    @Test
    fun `a duration change applies to the next session and leaves the cooldown untouched`() {
        val engine = engine(rule(usageMillis = 45_000, cooldownMillis = 60_000))
        engine.onForeground(INSTAGRAM)
        engine.run(20_000)

        engine.setRules(listOf(rule(usageMillis = 120_000, cooldownMillis = 90_000)))
        assertEquals(25_000L, engine.stateOf(INSTAGRAM)?.remainingUsageMillis)

        val expiry = engine.run(25_000)
        assertEquals(
            listOf(EngineEffect.GoHome(INSTAGRAM, HomeReason.ALLOWANCE_EXPIRED)),
            expiry.filterIsInstance<EngineEffect.GoHome>(),
        )
        assertEquals(clock.wallTime() + 90_000, engine.stateOf(INSTAGRAM)?.cooldownEndsAtWallMillis)

        clock.advance(90_000)
        engine.onForeground(INSTAGRAM)
        assertEquals(120_000L, engine.stateOf(INSTAGRAM)?.remainingUsageMillis)
    }
}
