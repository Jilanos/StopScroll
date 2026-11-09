package com.stopscroll.app.service

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * In-process channel from the time-up screen to the running service, which owns the engine.
 * A request sent while the service is not running is dropped, so no extension is granted.
 */
object ExtensionRequests {
    private val flow = MutableSharedFlow<String>(extraBufferCapacity = 8)

    val requests: SharedFlow<String> = flow

    fun request(packageName: String) {
        flow.tryEmit(packageName)
    }
}
