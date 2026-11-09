package com.stopscroll.app.data

import com.stopscroll.app.engine.AppRule
import com.stopscroll.app.engine.PackageRuntimeState

data class StoredSnapshot(
    val rules: List<AppRule>,
    val runtime: Map<String, PackageRuntimeState>,
)
