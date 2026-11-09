package com.stopscroll.app.service

/** Application windows include our settings activity; accessibility overlays are filtered by type. */
internal fun ordinaryForegroundPackage(owner: String?): String? =
    owner?.takeUnless { it == "com.android.systemui" }
