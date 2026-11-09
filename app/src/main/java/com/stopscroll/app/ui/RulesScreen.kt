package com.stopscroll.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.stopscroll.app.R
import com.stopscroll.app.engine.AppRule
import com.stopscroll.app.ui.theme.Spacing
import com.stopscroll.app.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RulesScreen(
    rules: List<AppRule>,
    serviceEnabled: Boolean,
    snackbar: SnackbarHostState,
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    onShowHowItWorks: () -> Unit,
    onOpenSettings: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (AppRule) -> Unit,
    onToggle: (AppRule, Boolean) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BrandMark(size = 32.dp)
                        Spacer(Modifier.width(Spacing.m))
                        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
                    }
                },
                actions = { OverflowMenu(themeMode, onThemeChange, onShowHowItWorks) },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(stringResource(R.string.add_app)) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                onClick = onAdd,
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Spacing.l,
                end = Spacing.l,
                top = padding.calculateTopPadding() + Spacing.xs,
                bottom = padding.calculateBottomPadding() + 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            item(key = "tagline") {
                Text(
                    text = stringResource(R.string.tagline),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Spacing.xs),
                )
            }
            item(key = "status") {
                if (serviceEnabled) ServiceActiveCard() else SetupCard(onOpenSettings)
            }
            item(key = "header") {
                SectionHeader(rules)
            }
            if (rules.isEmpty()) {
                item(key = "empty") { EmptyState(onAdd) }
            } else {
                items(rules, key = { it.packageName }) { rule ->
                    RuleCard(rule = rule, onClick = { onEdit(rule) }, onToggle = { onToggle(rule, it) })
                }
            }
        }
    }
}

@Composable
private fun OverflowMenu(themeMode: ThemeMode, onThemeChange: (ThemeMode) -> Unit, onShowHowItWorks: () -> Unit) {
    var open by remember { mutableStateOf(false) }
    IconButton(onClick = { open = true }) {
        Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.menu))
    }
    DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.how_title)) },
            leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null) },
            onClick = {
                open = false
                onShowHowItWorks()
            },
        )
        HorizontalDivider()
        Text(
            text = stringResource(R.string.theme),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = Spacing.l, vertical = Spacing.s),
        )
        listOf(
            ThemeMode.SYSTEM to R.string.theme_system,
            ThemeMode.LIGHT to R.string.theme_light,
            ThemeMode.DARK to R.string.theme_dark,
        ).forEach { (mode, label) ->
            DropdownMenuItem(
                text = { Text(stringResource(label)) },
                trailingIcon = {
                    if (mode == themeMode) Icon(Icons.Filled.Check, contentDescription = null)
                },
                onClick = {
                    open = false
                    onThemeChange(mode)
                },
            )
        }
    }
}

@Composable
private fun ServiceActiveCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Row(Modifier.padding(Spacing.l), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(Spacing.m))
            Column {
                Text(stringResource(R.string.service_on), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.service_on_hint), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun SetupCard(onOpenSettings: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        ),
    ) {
        Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            Text(stringResource(R.string.service_off), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.service_off_hint), style = MaterialTheme.typography.bodyMedium)
            Step(1, null, stringResource(R.string.setup_step1))
            Step(2, null, stringResource(R.string.setup_step2))
            Step(3, null, stringResource(R.string.setup_step3))
            Button(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.open_accessibility_settings))
            }
        }
    }
}

@Composable
private fun SectionHeader(rules: List<AppRule>) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = Spacing.xs, end = Spacing.xs, top = Spacing.s),
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = stringResource(R.string.your_apps),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        if (rules.isNotEmpty()) {
            val active = rules.count { it.enabled }
            Text(
                text = pluralStringResource(R.plurals.apps_active, active, active),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RuleCard(rule: AppRule, onClick: () -> Unit, onToggle: (Boolean) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLowest),
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.6f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.l, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val dim = if (rule.enabled) 1f else 0.45f
            AppIcon(rule.packageName, size = 48.dp, modifier = Modifier.alpha(dim))
            Spacer(Modifier.width(Spacing.l))
            Column(Modifier.weight(1f).alpha(if (rule.enabled) 1f else 0.7f)) {
                Text(rule.label, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (rule.enabled) {
                        Pill(
                            text = stringResource(R.string.allowed_short, formatDuration(rule.usageMillis)),
                            container = colors.secondaryContainer,
                            content = colors.onSecondaryContainer,
                        )
                        Pill(
                            text = stringResource(R.string.block_short, formatDuration(rule.cooldownMillis)),
                            container = colors.tertiaryContainer,
                            content = colors.onTertiaryContainer,
                        )
                    } else {
                        Pill(
                            text = stringResource(R.string.rule_paused),
                            container = colors.surfaceContainerHighest,
                            content = colors.onSurfaceVariant,
                        )
                    }
                }
            }
            Spacer(Modifier.width(Spacing.s))
            Switch(checked = rule.enabled, onCheckedChange = onToggle)
        }
    }
}

@Composable
private fun EmptyState(onAdd: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xxl, horizontal = Spacing.l),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.m),
    ) {
        BrandMark(
            size = 88.dp,
            container = MaterialTheme.colorScheme.secondaryContainer,
            content = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Text(
            text = stringResource(R.string.empty_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onAdd) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(Spacing.s))
            Text(stringResource(R.string.add_app))
        }
    }
}
