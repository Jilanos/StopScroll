package com.stopscroll.app.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.edit

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * Display preferences of the app itself (theme, onboarding seen). They live apart from the
 * enforcement state so they can never affect rules or sessions.
 */
class ThemePreference(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    var mode: ThemeMode
        get() = prefs.getString(KEY_THEME, null)
            ?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } }
            ?: ThemeMode.SYSTEM
        set(value) = prefs.edit { putString(KEY_THEME, value.name) }

    var onboardingDone: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_DONE, false)
        set(value) = prefs.edit { putBoolean(KEY_ONBOARDING_DONE, value) }

    /** Current theme mode, updated live when another screen changes it. */
    @Composable
    fun rememberMode(): ThemeMode {
        var current by remember { mutableStateOf(mode) }
        DisposableEffect(Unit) {
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == KEY_THEME) current = mode
            }
            prefs.registerOnSharedPreferenceChangeListener(listener)
            onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
        }
        return current
    }

    private companion object {
        const val FILE = "display"
        const val KEY_THEME = "theme"
        const val KEY_ONBOARDING_DONE = "onboarding_done"
    }
}
