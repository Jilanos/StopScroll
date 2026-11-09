package com.stopscroll.app.service

import com.stopscroll.app.engine.AppRule
import com.stopscroll.app.engine.EngineEffect
import com.stopscroll.app.engine.FakeClock
import com.stopscroll.app.engine.SessionEngine
import org.junit.Assert.*
import org.junit.Test

class WarningControllerTest {
    private val target = "com.example.target"
    private val clock = FakeClock()
    private val engine = SessionEngine(clock).apply {
        setRules(listOf(AppRule(target, "Target", 1_000L, 60_000L)))
    }
    private var visible = false
    private var shows = 0
    private val warning = WarningController({ visible = true; shows++ }, { visible = false })

    private fun update(foreground: String?, effects: List<EngineEffect>) =
        warning.update(foreground, engine::canShowWarning, effects)

    @Test
    fun `visible warning hides immediately on target exit`() {
        update(target, engine.onForeground(target))
        assertTrue(visible)
        update("com.stopscroll.app", engine.onForeground("com.stopscroll.app"))
        assertFalse(visible)
    }

    @Test
    fun `one second allowance hides warning before HOME even while target still owns the window`() {
        update(target, engine.onForeground(target))
        clock.advance(1_000L)
        val effects = engine.onTick()
        update(target, effects)
        assertFalse(visible)
        assertEquals(1, effects.filterIsInstance<EngineEffect.GoHome>().size)
    }

    @Test
    fun `delayed effect cannot show over another app`() {
        val effects = engine.onForeground(target)
        update("com.example.other", effects)
        assertFalse(visible)
        assertEquals(0, shows)
    }

    @Test
    fun `delayed effect cannot show after allowance deadline or rule disable`() {
        val effects = engine.onForeground(target)
        clock.advance(1_000L)
        update(target, effects)
        assertEquals(0, shows)
        engine.setRules(emptyList())
        update(target, effects)
        assertFalse(visible)
    }

    @Test
    fun `transient window preserves warning but locking and disconnect hide it`() {
        update(target, engine.onForeground(target))
        update(null, emptyList())
        assertTrue(visible)
        update(null, engine.onScreenLocked())
        assertFalse(visible)
        warning.clear()
        assertFalse(visible)
    }

    @Test
    fun `disconnect clears a still visible warning synchronously`() {
        update(target, engine.onForeground(target))
        assertTrue(visible)
        warning.clear()
        assertFalse(visible)
        assertEquals(1, shows)
    }
}
