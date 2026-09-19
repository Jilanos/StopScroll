package com.stopscroll.app.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

data class InstalledApp(val packageName: String, val label: String)

/**
 * Launchable apps only, excluding StopScroll itself and the current launcher so enforcement
 * can never trap the user in a HOME loop.
 */
fun loadLaunchableApps(context: Context): List<InstalledApp> {
    val packageManager = context.packageManager
    val launcherPackage = packageManager
        .resolveActivity(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),
            PackageManager.MATCH_DEFAULT_ONLY,
        )
        ?.activityInfo
        ?.packageName

    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return packageManager.queryIntentActivities(intent, 0)
        .asSequence()
        .map { it.activityInfo.applicationInfo }
        .filter { it.packageName != context.packageName && it.packageName != launcherPackage }
        .distinctBy { it.packageName }
        .map { InstalledApp(it.packageName, packageManager.getApplicationLabel(it).toString()) }
        .sortedBy { it.label.lowercase() }
        .toList()
}
