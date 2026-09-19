package com.stopscroll.app.service

import android.os.SystemClock
import com.stopscroll.app.engine.Clock

object AndroidClock : Clock {
    override fun elapsedRealtime(): Long = SystemClock.elapsedRealtime()

    override fun wallTime(): Long = System.currentTimeMillis()
}
