package com.stopscroll.app.service

import com.stopscroll.app.data.StoredSnapshot
import com.stopscroll.app.engine.AppRule
import com.stopscroll.app.engine.EngineEffect
import com.stopscroll.app.engine.PackageRuntimeState
import com.stopscroll.app.engine.SessionEngine
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Serializes restoration, persistence and effects; no caller can publish an empty startup state. */
internal class SessionRuntime(
    private val engine: SessionEngine,
    private val persist: suspend (Map<String, PackageRuntimeState>) -> Unit,
    private val applyEffects: (List<EngineEffect>) -> Unit,
) {
    private val ready = CompletableDeferred<Unit>()
    private val mutex = Mutex()

    suspend fun initialize(load: suspend () -> StoredSnapshot, foreground: () -> String?) {
        mutex.withLock {
            val saved = load()
            engine.restore(saved.runtime)
            engine.setRules(saved.rules)
            commit(engine.onForeground(foreground()))
            ready.complete(Unit)
        }
    }

    suspend fun cycle(action: () -> List<EngineEffect>) {
        ready.await()
        mutex.withLock { commit(action()) }
    }

    suspend fun updateRules(rules: List<AppRule>, foreground: () -> String?) = cycle {
        engine.setRules(rules)
        foreground()?.let(engine::onForeground) ?: engine.onTick()
    }

    private suspend fun commit(effects: List<EngineEffect>) {
        persist(engine.snapshot())
        applyEffects(effects)
    }
}
