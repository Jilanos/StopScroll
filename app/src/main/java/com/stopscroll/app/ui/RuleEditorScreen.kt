package com.stopscroll.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.stopscroll.app.R
import com.stopscroll.app.engine.AppRule
import com.stopscroll.app.engine.durationMillis
import com.stopscroll.app.ui.theme.Spacing

private val USAGE_PRESETS = listOf(60L, 5 * 60L, 10 * 60L, 15 * 60L, 30 * 60L)
private val BLOCK_PRESETS = listOf(5 * 60L, 10 * 60L, 20 * 60L, 30 * 60L, 60 * 60L)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RuleEditorScreen(
    rule: AppRule,
    isNew: Boolean,
    onBack: () -> Unit,
    onSave: (AppRule) -> Unit,
    onDelete: () -> Unit,
) {
    var usage by remember(rule) { mutableStateOf((rule.usageMillis / 1000).toString()) }
    var cooldown by remember(rule) { mutableStateOf((rule.cooldownMillis / 1000).toString()) }
    val usageMillis = durationMillis(usage)
    val cooldownMillis = durationMillis(cooldown, System.currentTimeMillis())
    val valid = usageMillis != null && cooldownMillis != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
        bottomBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = Spacing.l, vertical = Spacing.m),
            ) {
                Button(
                    enabled = valid,
                    onClick = {
                        val savedUsage = durationMillis(usage)
                        val savedCooldown = durationMillis(cooldown, System.currentTimeMillis())
                        if (savedUsage != null && savedCooldown != null) {
                            onSave(rule.copy(usageMillis = savedUsage, cooldownMillis = savedCooldown))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) { Text(stringResource(R.string.save)) }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.l),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIcon(rule.packageName, size = 64.dp)
                Spacer(Modifier.width(Spacing.l))
                Text(rule.label, style = MaterialTheme.typography.headlineMedium)
            }
            DurationSection(
                title = stringResource(R.string.allowed_title),
                hint = stringResource(R.string.allowed_hint),
                presets = USAGE_PRESETS,
                value = usage,
                parsedMillis = usageMillis,
                onValueChange = { usage = it },
            )
            DurationSection(
                title = stringResource(R.string.block_title),
                hint = stringResource(R.string.block_hint),
                presets = BLOCK_PRESETS,
                value = cooldown,
                parsedMillis = cooldownMillis,
                onValueChange = { cooldown = it },
            )
            if (!isNew) {
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) { Text(stringResource(R.string.delete)) }
            }
            Spacer(Modifier.height(Spacing.s))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DurationSection(
    title: String,
    hint: String,
    presets: List<Long>,
    value: String,
    parsedMillis: Long?,
    onValueChange: (String) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                parsedMillis?.let {
                    Text(
                        text = formatDuration(it),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Text(hint, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                presets.forEach { seconds ->
                    FilterChip(
                        selected = value.trim() == seconds.toString(),
                        onClick = { onValueChange(seconds.toString()) },
                        label = { Text(formatDuration(seconds * 1000)) },
                    )
                }
            }
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(stringResource(R.string.seconds_field)) },
                suffix = { Text("s") },
                isError = parsedMillis == null,
                supportingText = if (parsedMillis == null) {
                    { Text(stringResource(R.string.invalid_duration)) }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
