package com.stopscroll.app.service

import android.accessibilityservice.AccessibilityService
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo
import com.stopscroll.app.data.StopScrollStore
import com.stopscroll.app.engine.EngineEffect
import com.stopscroll.app.engine.SessionEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class StopScrollAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(SupervisorJob() + kotlinx.coroutines.Dispatchers.Main.immediate)
    private val engine = SessionEngine(AndroidClock)
    private lateinit var store: StopScrollStore
    private lateinit var warningOverlay: WarningOverlay
    private lateinit var warnings: WarningController
    private lateinit var runtime: SessionRuntime

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
        warnings = WarningController(warningOverlay::show, warningOverlay::hide)
        runtime = SessionRuntime(engine, store::writeRuntime, ::applyEffects)
        registerReceiver(
            screenReceiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_USER_PRESENT)
            },
        )

        scope.launch {
            runtime.initialize(store::readSnapshot, ::resolveForegroundPackage)
            launch {
                store.rules.distinctUntilChanged().collect { rules ->
                    runtime.updateRules(rules, ::resolveForegroundPackage)
                }
            }
            while (isActive) {
                delay(TICK_MILLIS)
                runtime.cycle { engine.onTick() }
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        dispatch {
            val foreground = resolveForegroundPackage()
            if (foreground == null) emptyList() else engine.onForeground(foreground)
        }
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        runCatching { unregisterReceiver(screenReceiver) }
        scope.coroutineContext[kotlinx.coroutines.Job]?.cancel()
        if (::warnings.isInitialized) warnings.clear()
        super.onDestroy()
    }

    private fun dispatch(block: () -> List<EngineEffect>) {
        if (!::runtime.isInitialized) return
        scope.launch { runtime.cycle(block) }
    }

    private fun applyEffects(effects: List<EngineEffect>) {
        warnings.update(resolveForegroundPackage(), engine::canShowWarning, effects)
        effects.filterIsInstance<EngineEffect.GoHome>().forEach { goHome(it.packageName) }
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
        if (!getSystemService(PowerManager::class.java).isInteractive ||
            getSystemService(KeyguardManager::class.java).isKeyguardLocked
        ) return null
        val application = windows
            .filter { it.type == AccessibilityWindowInfo.TYPE_APPLICATION }
            .sortedByDescending { it.isActive }
            .firstOrNull() ?: return null
        val owner = application.root?.packageName?.toString() ?: return null
        return ordinaryForegroundPackage(owner)
    }

    private companion object {
        const val TAG = "StopScroll"
        const val TICK_MILLIS = 1_000L
    }
}
