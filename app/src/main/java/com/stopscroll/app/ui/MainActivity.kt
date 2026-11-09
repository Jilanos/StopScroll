package com.stopscroll.app.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stopscroll.app.R
import com.stopscroll.app.data.StopScrollStore
import com.stopscroll.app.engine.AppRule
import com.stopscroll.app.engine.durationMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private val viewModel: RulesViewModel by viewModels { RulesViewModel.factory(StopScrollStore(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                MainScreen(
                    viewModel = viewModel,
                    onOpenAccessibilitySettings = {
                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreen(viewModel: RulesViewModel, onOpenAccessibilitySettings: () -> Unit) {
    val context = LocalContext.current
    val rules by viewModel.rules.collectAsStateWithLifecycle()
    var picking by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<AppRule?>(null) }
    val serviceEnabled = rememberAccessibilityServiceEnabled()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(stringResource(R.string.add_app)) },
                icon = {},
                onClick = { picking = true },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            ServiceStatusCard(serviceEnabled, onOpenAccessibilitySettings)
            Spacer(Modifier.height(16.dp))
            if (rules.isEmpty()) {
                Text(stringResource(R.string.no_rule))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(rules, key = { it.packageName }) { rule ->
                        RuleCard(
                            rule = rule,
                            onToggle = { viewModel.setEnabled(rule.packageName, it) },
                            onEdit = { editing = rule },
                            onDelete = { viewModel.remove(rule.packageName) },
                        )
                    }
                }
            }
        }
    }

    if (picking) {
        AppPickerDialog(
            onDismiss = { picking = false },
            onPick = { app ->
                picking = false
                editing = rules.firstOrNull { it.packageName == app.packageName }
                    ?: AppRule(app.packageName, app.label, DEFAULT_USAGE_MILLIS, DEFAULT_COOLDOWN_MILLIS)
            },
        )
    }

    editing?.let { rule ->
        DurationDialog(
            rule = rule,
            onDismiss = { editing = null },
            onConfirm = {
                viewModel.upsert(it)
                editing = null
            },
        )
    }
}

@Composable
private fun ServiceStatusCard(enabled: Boolean, onOpenSettings: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            },
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = stringResource(if (enabled) R.string.service_on else R.string.service_off),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(stringResource(if (enabled) R.string.service_on_hint else R.string.service_off_hint))
            TextButton(onClick = onOpenSettings) { Text(stringResource(R.string.open_accessibility_settings)) }
        }
    }
}

@Composable
private fun RuleCard(rule: AppRule, onToggle: (Boolean) -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIcon(rule.packageName)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(rule.label, style = MaterialTheme.typography.titleMedium)
                    Text(rule.packageName, style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = rule.enabled, onCheckedChange = onToggle)
            }
            Text(stringResource(R.string.allowed_duration, formatDuration(rule.usageMillis)))
            Text(stringResource(R.string.block_duration, formatDuration(rule.cooldownMillis)))
            Row {
                TextButton(onClick = onEdit) { Text(stringResource(R.string.edit)) }
                TextButton(onClick = onDelete) { Text(stringResource(R.string.delete)) }
            }
        }
    }
}

@Composable
private fun AppPickerDialog(onDismiss: () -> Unit, onPick: (InstalledApp) -> Unit) {
    val context = LocalContext.current
    val apps by produceState(emptyList<InstalledApp>()) {
        value = withContext(Dispatchers.IO) { loadLaunchableApps(context) }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
        title = { Text(stringResource(R.string.add_app)) },
        text = {
            LazyColumn {
                items(apps, key = { it.packageName }) { app ->
                    TextButton(onClick = { onPick(app) }, modifier = Modifier.fillMaxWidth()) {
                        AppIcon(app.packageName, size = 32.dp)
                        Spacer(Modifier.width(12.dp))
                        Text(app.label, modifier = Modifier.weight(1f))
                    }
                }
            }
        },
    )
}

@Composable
private fun DurationDialog(rule: AppRule, onDismiss: () -> Unit, onConfirm: (AppRule) -> Unit) {
    var usage by remember { mutableStateOf((rule.usageMillis / 1000).toString()) }
    var cooldown by remember { mutableStateOf((rule.cooldownMillis / 1000).toString()) }
    val usageMillis = durationMillis(usage)
    val cooldownMillis = durationMillis(cooldown, System.currentTimeMillis())
    val valid = usageMillis != null && cooldownMillis != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(rule.label) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = usage,
                    onValueChange = { usage = it },
                    label = { Text(stringResource(R.string.usage_seconds)) },
                    isError = usageMillis == null,
                    singleLine = true,
                )
                OutlinedTextField(
                    value = cooldown,
                    onValueChange = { cooldown = it },
                    label = { Text(stringResource(R.string.cooldown_seconds)) },
                    isError = cooldownMillis == null,
                    singleLine = true,
                )
                if (!valid) Text(stringResource(R.string.invalid_duration))
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    val savedUsage = durationMillis(usage)
                    val savedCooldown = durationMillis(cooldown, System.currentTimeMillis())
                    if (savedUsage != null && savedCooldown != null) {
                        onConfirm(
                            rule.copy(
                                usageMillis = savedUsage,
                                cooldownMillis = savedCooldown,
                            ),
                        )
                    }
                },
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

private const val DEFAULT_USAGE_MILLIS = 5 * 60 * 1000L
private const val DEFAULT_COOLDOWN_MILLIS = 10 * 60 * 1000L
