package com.stopscroll.app.service

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.stopscroll.app.R
import kotlin.math.roundToInt

/**
 * Short, non-touchable accessibility overlay. It deliberately adds no sound, vibration or
 * SYSTEM_ALERT_WINDOW permission.
 */
class WarningOverlay(private val service: AccessibilityService) {

    private val handler = Handler(Looper.getMainLooper())
    private val windowManager = service.getSystemService(WindowManager::class.java)
    private var view: View? = null

    private val hideRunnable = Runnable { hide() }

    // All calls come from the service's main dispatcher. No deferred show can survive exit.
    fun show(remainingMillis: Long) {
        hide()
        val seconds = (remainingMillis / 1000.0).roundToInt()
        val density = service.resources.displayMetrics.density
        fun dp(value: Int) = (value * density).roundToInt()
        val text = TextView(service).apply {
            text = service.resources.getQuantityString(R.plurals.warning_overlay, seconds, seconds)
            setTextColor(Color.WHITE)
            textSize = 16f
            background = GradientDrawable().apply {
                cornerRadius = dp(28).toFloat()
                setColor(service.getColor(R.color.warning_background))
            }
            elevation = dp(6).toFloat()
            val glyph = service.getDrawable(R.drawable.ic_stopscroll_glyph)?.apply { setBounds(0, 0, dp(22), dp(22)) }
            setCompoundDrawablesRelative(glyph, null, null, null)
            compoundDrawablePadding = dp(10)
            setPadding(dp(16), dp(12), dp(20), dp(12))
            gravity = Gravity.CENTER_VERTICAL
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = dp(TOP_OFFSET_DP)
        }

        runCatching { windowManager.addView(text, params) }
            .onSuccess {
                view = text
                handler.postDelayed(hideRunnable, VISIBLE_MILLIS)
            }
    }

    fun hide() {
        handler.removeCallbacks(hideRunnable)
        val current = view ?: return
        view = null
        runCatching { windowManager.removeView(current) }
    }

    private companion object {
        const val VISIBLE_MILLIS = 4_000L
        const val TOP_OFFSET_DP = 56
    }
}
