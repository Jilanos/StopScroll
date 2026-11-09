package com.stopscroll.app.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stopscroll.app.R
import com.stopscroll.app.data.StopScrollStore
import com.stopscroll.app.engine.AppRule
import com.stopscroll.app.ui.theme.ThemePreference
import com.stopscroll.app.ui.theme.setThemedContent
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: RulesViewModel by viewModels { RulesViewModel.factory(StopScrollStore(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val display = ThemePreference(this)
        setThemedContent {
            StopScrollApp(
                viewModel = viewModel,
                display = display,
                onOpenAccessibilitySettings = {
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                },
            )
        }
    }
}

private sealed interface Screen {
    data object Onboarding : Screen
    data object Rules : Screen
    data object Picker : Screen
    data class Editor(val rule: AppRule, val isNew: Boolean) : Screen
}

@Composable
private fun StopScrollApp(
    viewModel: RulesViewModel,
    display: ThemePreference,
    onOpenAccessibilitySettings: () -> Unit,
) {
    val rules by viewModel.rules.collectAsStateWithLifecycle()
    val serviceEnabled = rememberAccessibilityServiceEnabled()
    var screen by remember {
        mutableStateOf(if (display.onboardingDone) Screen.Rules else Screen.Onboarding)
    }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val removedText = stringResource(R.string.rule_removed, "%s")
    val undoText = stringResource(R.string.undo)

    if (screen != Screen.Rules) BackHandler { screen = Screen.Rules }

    when (val current = screen) {
        Screen.Onboarding -> OnboardingScreen(
            onStart = {
                display.onboardingDone = true
                screen = Screen.Rules
            },
        )

        Screen.Rules -> RulesScreen(
            rules = rules,
            serviceEnabled = serviceEnabled,
            snackbar = snackbar,
            themeMode = display.rememberMode(),
            onThemeChange = { display.mode = it },
            onShowHowItWorks = { screen = Screen.Onboarding },
            onOpenSettings = onOpenAccessibilitySettings,
            onAdd = { screen = Screen.Picker },
            onEdit = { screen = Screen.Editor(it, isNew = false) },
            onToggle = { rule, enabled -> viewModel.setEnabled(rule.packageName, enabled) },
        )

        Screen.Picker -> AppPickerScreen(
            configured = rules.mapTo(HashSet()) { it.packageName },
            onBack = { screen = Screen.Rules },
            onPick = { app ->
                val existing = rules.firstOrNull { it.packageName == app.packageName }
                screen = Screen.Editor(
                    rule = existing
                        ?: AppRule(app.packageName, app.label, DEFAULT_USAGE_MILLIS, DEFAULT_COOLDOWN_MILLIS),
                    isNew = existing == null,
                )
            },
        )

        is Screen.Editor -> RuleEditorScreen(
            rule = current.rule,
            isNew = current.isNew,
            onBack = { screen = Screen.Rules },
            onSave = {
                viewModel.upsert(it)
                screen = Screen.Rules
            },
            onDelete = {
                val removed = current.rule
                viewModel.remove(removed.packageName)
                screen = Screen.Rules
                scope.launch {
                    snackbar.currentSnackbarData?.dismiss()
                    val result = snackbar.showSnackbar(
                        message = removedText.replace("%s", removed.label),
                        actionLabel = undoText,
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) viewModel.upsert(removed)
                }
            },
        )
    }
}

private const val DEFAULT_USAGE_MILLIS = 5 * 60 * 1000L
private const val DEFAULT_COOLDOWN_MILLIS = 10 * 60 * 1000L
