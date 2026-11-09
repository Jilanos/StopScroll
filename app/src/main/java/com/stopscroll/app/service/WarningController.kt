package com.stopscroll.app.service

import com.stopscroll.app.engine.EngineEffect

/** Called synchronously with engine commits, on the UI thread in the Android adapter. */
internal class WarningController(
    private val show: (Long) -> Unit,
    private val hide: () -> Unit,
) {
    private var owner: String? = null

    fun update(foreground: String?, eligible: (String) -> Boolean, effects: List<EngineEffect>) {
        owner?.let {
            if ((foreground != null && foreground != it) || !eligible(it)) clear()
        }
        effects.filterIsInstance<EngineEffect.ShowWarning>().forEach {
            if (foreground == it.packageName && eligible(it.packageName)) {
                owner = it.packageName
                show(it.remainingMillis)
            }
        }
    }

    fun clear() {
        owner = null
        hide()
    }
}
