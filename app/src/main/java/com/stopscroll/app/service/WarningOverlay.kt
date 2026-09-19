package com.stopscroll.app.service

import android.accessibilityservice.AccessibilityService
import android.graphics.Color
import android.graphics.PixelFormat
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

    fun show(remainingMillis: Long) {
        handler.post {
            hide()
            val seconds = (remainingMillis / 1000.0).roundToInt()
            val text = TextView(service).apply {
                text = service.resources.getQuantityString(R.plurals.warning_overlay, seconds, seconds)
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.argb(220, 0, 0, 0))
                textSize = 18f
                val padding = (16 * service.resources.displayMetrics.density).toInt()
                setPadding(padding, padding, padding, padding)
                gravity = Gravity.CENTER
            }
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT,
            ).apply { gravity = Gravity.TOP }

            runCatching { windowManager.addView(text, params) }
                .onSuccess {
                    view = text
                    handler.postDelayed(hideRunnable, VISIBLE_MILLIS)
                }
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
    }
}
