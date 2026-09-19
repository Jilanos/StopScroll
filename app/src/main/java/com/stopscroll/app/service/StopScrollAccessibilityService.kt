package com.stopscroll.app.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo
import com.stopscroll.app.data.StopScrollStore
import com.stopscroll.app.engine.EngineEffect
import com.stopscroll.app.engine.SessionEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class StopScrollAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(SupervisorJob() + kotlinx.coroutines.Dispatchers.Default)
    private val mutex = Mutex()
    private val engine = SessionEngine(AndroidClock)
    private lateinit var store: StopScrollStore
    private lateinit var warningOverlay: WarningOverlay

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> dispatch { engine.onScreenLocked() }
                Intent.ACTION_USER_PRESENT -> dispatch { engine.onForeground(resolveForegroundPackage()) }
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        store = StopScrollStore(this)
        warningOverlay = WarningOverlay(this)
        registerReceiver(
            screenReceiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_USER_PRESENT)
            },
        )

        scope.launch {
            mutex.withLock { engine.restore(store.readRuntime()) }
            launch {
                store.rules.collectLatest { rules ->
                    mutex.withLock {
                        engine.setRules(rules)
                        store.writeRuntime(engine.snapshot())
                    }
                }
            }
            while (isActive) {
                delay(TICK_MILLIS)
                runCycle { engine.onTick() }
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val foreground = resolveForegroundPackage() ?: return
        dispatch { engine.onForeground(foreground) }
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        runCatching { unregisterReceiver(screenReceiver) }
        warningOverlay.hide()
        scope.coroutineContext[kotlinx.coroutines.Job]?.cancel()
        super.onDestroy()
    }

    private fun dispatch(block: () -> List<EngineEffect>) {
        scope.launch { runCycle(block) }
    }

    private suspend fun runCycle(block: () -> List<EngineEffect>) {
        val effects = mutex.withLock {
            val produced = block()
            store.writeRuntime(engine.snapshot())
            produced
        }
        effects.forEach(::apply)
    }

    private fun apply(effect: EngineEffect) {
        when (effect) {
            is EngineEffect.ShowWarning -> warningOverlay.show(effect.remainingMillis)
            is EngineEffect.GoHome -> goHome(effect.packageName)
        }
    }

    /** Ownership is re-checked here so an unrelated foreground app is never ejected. */
    private fun goHome(packageName: String) {
        if (resolveForegroundPackage() != packageName) return
        val performed = performGlobalAction(GLOBAL_ACTION_HOME)
        if (!performed) Log.w(TAG, "GLOBAL_ACTION_HOME refused for $packageName")
    }

    /**
     * Returns the ordinary application that owns the foreground, or null when the current
     * windows are transient (keyboard, system UI, our own overlay) and the session must be
     * left untouched.
     */
    private fun resolveForegroundPackage(): String? {
        val application = windows
            .filter { it.type == AccessibilityWindowInfo.TYPE_APPLICATION }
            .sortedByDescending { it.isActive }
            .firstOrNull() ?: return null
        val owner = application.root?.packageName?.toString() ?: return null
        if (owner == packageName || owner in TRANSIENT_PACKAGES) return null
        return owner
    }

    private companion object {
        const val TAG = "StopScroll"
        const val TICK_MILLIS = 1_000L
        val TRANSIENT_PACKAGES = setOf("com.android.systemui")
    }
}
