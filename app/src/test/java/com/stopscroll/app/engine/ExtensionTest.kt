package com.stopscroll.app.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private const val TARGET = "com.instagram.android"
private const val LAUNCHER = "com.android.launcher"

class ExtensionTest {

    private val clock = FakeClock()
    private val rule = AppRule(TARGET, "Instagram", usageMillis = 45_000, cooldownMillis = 60_000)

    private fun engine(vararg rules: AppRule = arrayOf(rule)) = SessionEngine(clock).apply { setRules(rules.toList()) }

    private fun SessionEngine.run(millis: Long): List<EngineEffect> {
        val effects = mutableListOf<EngineEffect>()
        repeat((millis / 1_000).toInt()) {
            clock.advance(1_000)
            effects += onTick()
        }
        return effects
    }

    private fun SessionEngine.expire(usageMillis: Long = 45_000): List<EngineEffect> {
        onForeground(TARGET)
        return run(usageMillis)
    }

    @Test
    fun `expiry records the block duration and offers its double`() {
        val engine = engine()
        engine.expire()

        assertEquals(60_000L, engine.stateOf(TARGET)?.cooldownMillis)
        assertEquals(120_000L, engine.extensionOffer(TARGET))
    }

    @Test
    fun `an extension cancels the block grants thirty seconds and relaunches the app`() {
        val engine = engine()
        engine.expire()
        engine.onForeground("com.stopscroll.app")

        val effects = engine.grantExtension(TARGET)

        assertEquals(listOf(EngineEffect.LaunchApp(TARGET)), effects)
        val state = engine.stateOf(TARGET)
        assertNull(state?.cooldownEndsAtWallMillis)
        assertEquals(SessionEngine.EXTENSION_MILLIS, state?.remainingUsageMillis)
        assertEquals(120_000L, state?.cooldownMillis)
        assertNull(engine.extensionOffer(TARGET))
    }

    @Test
    fun `the extension is consumed like an allowance and the next block is doubled`() {
        val engine = engine()
        engine.expire()
        engine.grantExtension(TARGET)

        val reopen = engine.onForeground(TARGET)
        assertTrue("no second warning for the extension", reopen.isEmpty())
        engine.run(10_000)
        engine.onForeground(LAUNCHER)
        engine.run(60_000)
        assertEquals(20_000L, engine.stateOf(TARGET)?.remainingUsageMillis)

        engine.onForeground(TARGET)
        val expiry = engine.run(20_000)

        assertEquals(listOf(EngineEffect.GoHome(TARGET, HomeReason.ALLOWANCE_EXPIRED)), expiry)
        assertEquals(clock.wallTime() + 120_000, engine.stateOf(TARGET)?.cooldownEndsAtWallMillis)
        assertEquals(240_000L, engine.extensionOffer(TARGET))
    }

    @Test
    fun `each extension doubles the pending block again`() {
        val engine = engine()
        engine.expire()
        engine.grantExtension(TARGET)
        engine.expire(30_000)
        engine.grantExtension(TARGET)

        assertEquals(240_000L, engine.stateOf(TARGET)?.cooldownMillis)
        engine.expire(30_000)
        assertEquals(clock.wallTime() + 240_000, engine.stateOf(TARGET)?.cooldownEndsAtWallMillis)
    }

    @Test
    fun `a block that runs to its end resets the next one to the rule duration`() {
        val engine = engine()
        engine.expire()
        engine.grantExtension(TARGET)
        engine.expire(30_000)

        clock.advance(120_000)
        engine.onForeground(LAUNCHER)
        assertNull(engine.stateOf(TARGET)?.cooldownMillis)

        engine.expire()
        assertEquals(clock.wallTime() + 60_000, engine.stateOf(TARGET)?.cooldownEndsAtWallMillis)
        assertEquals(60_000L, engine.stateOf(TARGET)?.cooldownMillis)
    }

    @Test
    fun `no extension is granted without a running block`() {
        val engine = engine(rule, AppRule("com.example.off", "Off", 45_000, 60_000, enabled = false))
        assertTrue(engine.grantExtension(TARGET).isEmpty())

        engine.onForeground(TARGET)
        engine.run(10_000)
        assertTrue(engine.grantExtension(TARGET).isEmpty())
        assertEquals(35_000L, engine.stateOf(TARGET)?.remainingUsageMillis)

        engine.run(35_000)
        clock.advance(60_000)
        assertNull(engine.extensionOffer(TARGET))
        assertTrue(engine.grantExtension(TARGET).isEmpty())

        assertTrue(engine.grantExtension("com.example.off").isEmpty())
        assertTrue(engine.grantExtension("com.example.unknown").isEmpty())
    }

    @Test
    fun `a block restored from an older version offers no extension`() {
        val engine = engine()
        engine.restore(mapOf(TARGET to PackageRuntimeState(cooldownEndsAtWallMillis = clock.wallTime() + 60_000)))

        assertNull(engine.extensionOffer(TARGET))
        assertTrue(engine.grantExtension(TARGET).isEmpty())
        assertEquals(
            listOf(EngineEffect.GoHome(TARGET, HomeReason.COOLDOWN_REENTRY)),
            engine.onForeground(TARGET),
        )
    }

    @Test
    fun `a granted extension and its doubled block survive a restart`() {
        val first = engine()
        first.expire()
        first.grantExtension(TARGET)
        first.onForeground(TARGET)
        first.run(10_000)
        val snapshot = first.snapshot()

        clock.reboot(downtimeMillis = 5_000)
        val restarted = engine()
        restarted.restore(snapshot)
        restarted.onForeground(TARGET)
        assertEquals(20_000L, restarted.stateOf(TARGET)?.remainingUsageMillis)

        restarted.run(20_000)
        assertEquals(clock.wallTime() + 120_000, restarted.stateOf(TARGET)?.cooldownEndsAtWallMillis)
    }

    @Test
    fun `disabling the rule drops a pending doubled block`() {
        val engine = engine()
        engine.expire()
        engine.grantExtension(TARGET)

        engine.setRules(listOf(rule.copy(enabled = false)))
        engine.setRules(listOf(rule))
        engine.expire()

        assertEquals(clock.wallTime() + 60_000, engine.stateOf(TARGET)?.cooldownEndsAtWallMillis)
    }
}
