package com.stopscroll.app.engine

import org.junit.Assert.*
import org.junit.Test

class DurationBoundsTest {
    @Test
    fun `input rejects non positive non numeric and overflowing seconds`() {
        listOf("", "abc", "1.5", "0", "-1", "9223372036854776", Long.MAX_VALUE.toString(), "99999999999999999999")
            .forEach { assertNull(it, durationMillis(it)) }
        assertEquals(45_000L, durationMillis("45"))
        val maximum = Long.MAX_VALUE / 1_000L
        assertEquals(maximum * 1_000L, durationMillis(maximum.toString()))
        assertNull(durationMillis((maximum + 1).toString()))
    }

    @Test
    fun `cooldown input leaves room for the current wall clock`() {
        val now = 1_700_000_000_000L
        val maximum = (Long.MAX_VALUE - now) / 1_000L
        assertEquals(maximum * 1_000L, durationMillis(maximum.toString(), now))
        assertNull(durationMillis((maximum + 1).toString(), now))
    }

    @Test
    fun `engine saturates a legacy huge cooldown instead of wrapping negative`() {
        val clock = FakeClock()
        val target = "com.example.target"
        val engine = SessionEngine(clock).apply {
            setRules(listOf(AppRule(target, "Target", 1_000L, Long.MAX_VALUE)))
        }
        engine.onForeground(target)
        clock.advance(1_000L)
        engine.onTick()
        assertEquals(Long.MAX_VALUE, engine.stateOf(target)?.cooldownEndsAtWallMillis)
        assertEquals(listOf(EngineEffect.GoHome(target, HomeReason.COOLDOWN_REENTRY)), engine.onForeground(target))
    }

    @Test
    fun `safe deadline handles exact boundary and normal addition`() {
        assertEquals(Long.MAX_VALUE, cooldownDeadline(Long.MAX_VALUE - 1_000L, 1_000L))
        assertEquals(Long.MAX_VALUE, cooldownDeadline(Long.MAX_VALUE - 999L, 1_000L))
        assertEquals(61_000L, cooldownDeadline(1_000L, 60_000L))
    }
}
