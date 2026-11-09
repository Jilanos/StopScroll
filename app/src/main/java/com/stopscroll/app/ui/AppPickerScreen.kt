package com.stopscroll.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.stopscroll.app.R
import com.stopscroll.app.ui.theme.Spacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Apps people most often want to keep an eye on, shown first when installed. */
private val SUGGESTED_PACKAGES = listOf(
    "com.instagram.android",
    "com.zhiliaoapp.musically",
    "com.ss.android.ugc.trill",
    "com.google.android.youtube",
    "com.reddit.frontpage",
    "com.twitter.android",
    "com.facebook.katana",
    "com.snapchat.android",
    "com.instagram.barcelona",
    "com.pinterest",
    "com.linkedin.android",
    "tv.twitch.android.app",
    "com.netflix.mediaclient",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AppPickerScreen(
    configured: Set<String>,
    onBack: () -> Unit,
    onPick: (InstalledApp) -> Unit,
) {
    val context = LocalContext.current
    val apps by produceState<List<InstalledApp>?>(null) {
        value = withContext(Dispatchers.IO) { loadLaunchableApps(context) }
    }
    var query by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.pick_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        val all = apps
        if (all == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        val filtered = remember(all, query) {
            val needle = query.trim()
            if (needle.isEmpty()) all else all.filter { it.label.contains(needle, ignoreCase = true) }
        }
        val suggested = remember(all, query) {
            if (query.isNotBlank()) {
                emptyList()
            } else {
                SUGGESTED_PACKAGES.mapNotNull { pkg -> all.firstOrNull { it.packageName == pkg } }
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + Spacing.l,
            ),
        ) {
            item(key = "search") {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(stringResource(R.string.search_apps)) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.l, vertical = Spacing.s),
                )
            }
            if (suggested.isNotEmpty()) {
                header("suggested-header", R.string.suggested)
                appItems("s:", suggested, configured, onPick)
                header("all-header", R.string.all_apps)
            }
            if (filtered.isEmpty()) {
                item(key = "none") {
                    Text(
                        text = stringResource(R.string.no_app_found),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(Spacing.xl),
                    )
                }
            }
            appItems("a:", filtered, configured, onPick)
        }
    }
}

private fun LazyListScope.header(key: String, label: Int) {
    item(key = key) {
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = Spacing.xl, end = Spacing.xl, top = Spacing.l, bottom = Spacing.xs),
        )
    }
}

private fun LazyListScope.appItems(
    keyPrefix: String,
    apps: List<InstalledApp>,
    configured: Set<String>,
    onPick: (InstalledApp) -> Unit,
) {
    items(apps, key = { keyPrefix + it.packageName }) { app ->
        val added = app.packageName in configured
        ListItem(
            headlineContent = { Text(app.label, style = MaterialTheme.typography.bodyLarge) },
            leadingContent = { AppIcon(app.packageName, size = 40.dp) },
            trailingContent = {
                if (added) {
                    Pill(
                        text = stringResource(R.string.already_added),
                        container = MaterialTheme.colorScheme.secondaryContainer,
                        content = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                } else {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            },
            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.clickable { onPick(app) }.padding(horizontal = Spacing.s),
        )
    }
}
