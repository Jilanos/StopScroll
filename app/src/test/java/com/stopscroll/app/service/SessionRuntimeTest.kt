package com.stopscroll.app.service

import com.stopscroll.app.data.StoredSnapshot
import com.stopscroll.app.engine.AppRule
import com.stopscroll.app.engine.EngineEffect
import com.stopscroll.app.engine.FakeClock
import com.stopscroll.app.engine.HomeReason
import com.stopscroll.app.engine.PackageRuntimeState
import com.stopscroll.app.engine.SessionEngine
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionRuntimeTest {
    private val target = "com.example.target"
    private val ownPackage = "com.stopscroll.app"
    private val rule = AppRule(target, "Target", 60_000L, 60_000L)
    private val clock = FakeClock()
    private val engine = SessionEngine(clock)
    private val writes = mutableListOf<Map<String, PackageRuntimeState>>()
    private val effects = mutableListOf<EngineEffect>()
    private val runtime = SessionRuntime(engine, { writes += it }, { effects += it })

    @Test
    fun `own activity pauses and resumes the exact remaining allowance`() = runTest {
        runtime.initialize({ StoredSnapshot(listOf(rule), emptyMap()) }, { target })
        clock.advance(10_000L)
        runtime.cycle { engine.onForeground(ordinaryForegroundPackage(ownPackage)) }
        clock.advance(120_000L)
        runtime.cycle { engine.onTick() }
        assertEquals(50_000L, engine.stateOf(target)?.remainingUsageMillis)
        runtime.cycle { engine.onForeground(target) }
        clock.advance(5_000L)
        runtime.cycle { engine.onTick() }
        assertEquals(45_000L, engine.stateOf(target)?.remainingUsageMillis)
    }

    @Test
    fun `transient system UI does not become an ordinary foreground owner`() {
        assertNull(ordinaryForegroundPackage("com.android.systemui"))
        assertNull(ordinaryForegroundPackage(null))
        assertEquals(ownPackage, ordinaryForegroundPackage(ownPackage))
    }

    @Test
    fun `early events wait for rules and runtime and cannot overwrite saved state`() = runTest {
        val loaded = CompletableDeferred<StoredSnapshot>()
        var handled = false
        launch { runtime.cycle { handled = true; engine.onForeground(target) } }
        launch { runtime.initialize({ loaded.await() }, { target }) }
        runCurrent()
        assertFalse(handled)
        assertTrue(writes.isEmpty())
        loaded.complete(StoredSnapshot(listOf(rule), mapOf(target to PackageRuntimeState(42_000L))))
        runCurrent()
        assertTrue(handled)
        assertEquals(2, writes.size)
        assertTrue(writes.all { it[target]?.remainingUsageMillis == 42_000L })
    }

    @Test
    fun `startup enforces a restored cooldown without another window event`() = runTest {
        val deadline = clock.wallTime() + 60_000L
        runtime.initialize({
            StoredSnapshot(listOf(rule), mapOf(target to PackageRuntimeState(cooldownEndsAtWallMillis = deadline)))
        }, { target })
        assertEquals(listOf(EngineEffect.GoHome(target, HomeReason.COOLDOWN_REENTRY)), effects)
        assertEquals(deadline, writes.single()[target]?.cooldownEndsAtWallMillis)
    }

    @Test
    fun `startup starts charging a currently open target without another event`() = runTest {
        runtime.initialize({ StoredSnapshot(listOf(rule), emptyMap()) }, { target })
        clock.advance(10_000L)
        runtime.cycle { engine.onTick() }
        assertEquals(50_000L, engine.stateOf(target)?.remainingUsageMillis)
    }

    @Test
    fun `foreground is resolved after loading rather than retaining an early event`() = runTest {
        val loaded = CompletableDeferred<StoredSnapshot>()
        var foreground = target
        launch { runtime.initialize({ loaded.await() }, { foreground }) }
        launch { runtime.cycle { engine.onForeground(foreground) } }
        runCurrent()
        foreground = ownPackage
        loaded.complete(StoredSnapshot(listOf(rule), mapOf(target to PackageRuntimeState(42_000L))))
        runCurrent()
        clock.advance(120_000L)
        runtime.cycle { engine.onTick() }
        assertEquals(42_000L, engine.stateOf(target)?.remainingUsageMillis)
        assertTrue(effects.isEmpty())
    }

    @Test
    fun `new rule immediately reconciles an already open target`() = runTest {
        runtime.initialize({ StoredSnapshot(emptyList(), emptyMap()) }, { target })
        runtime.updateRules(listOf(rule), { target })
        clock.advance(10_000L)
        runtime.cycle { engine.onTick() }
        assertEquals(50_000L, engine.stateOf(target)?.remainingUsageMillis)
    }
}
