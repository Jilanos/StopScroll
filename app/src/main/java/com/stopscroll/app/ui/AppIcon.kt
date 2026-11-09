package com.stopscroll.app.ui

import android.content.Context
import android.content.pm.PackageManager
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val iconCache = LruCache<String, ImageBitmap>(128)

/** Launcher icon of an installed app, or null when the package is missing. Call off the main thread. */
internal fun loadAppIcon(context: Context, packageName: String, sizePx: Int): ImageBitmap? {
    val key = "$packageName@$sizePx"
    iconCache.get(key)?.let { return it }
    val drawable = try {
        context.packageManager.getApplicationIcon(packageName)
    } catch (_: PackageManager.NameNotFoundException) {
        return null
    }
    val bitmap = drawable.toBitmap(sizePx, sizePx).asImageBitmap()
    iconCache.put(key, bitmap)
    return bitmap
}

/** Reserves the icon slot, shows a cached icon at once and decodes a missing one on an IO thread. */
@Composable
fun AppIcon(packageName: String, modifier: Modifier = Modifier, size: Dp = 40.dp) {
    val context = LocalContext.current.applicationContext
    val sizePx = with(LocalDensity.current) { size.roundToPx() }
    var icon by remember(packageName, sizePx) { mutableStateOf(iconCache.get("$packageName@$sizePx")) }
    LaunchedEffect(packageName, sizePx) {
        if (icon == null) icon = withContext(Dispatchers.IO) { loadAppIcon(context, packageName, sizePx) }
    }
    Box(modifier.size(size)) {
        icon?.let { Image(bitmap = it, contentDescription = null, modifier = Modifier.size(size)) }
    }
}
