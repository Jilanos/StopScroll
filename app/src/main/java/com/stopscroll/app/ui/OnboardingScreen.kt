package com.stopscroll.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.stopscroll.app.R
import com.stopscroll.app.ui.theme.Spacing

/** Welcome and "how it works" explainer. Shown on first launch and from the menu. */
@Composable
internal fun OnboardingScreen(onStart: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.xl),
                verticalArrangement = Arrangement.spacedBy(Spacing.l),
            ) {
                Spacer(Modifier.height(Spacing.xl))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BrandMark(size = 56.dp)
                    Spacer(Modifier.width(Spacing.m))
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
                }
                Text(stringResource(R.string.onboarding_title), style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = stringResource(R.string.onboarding_body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.how_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = Spacing.s),
                )
                Step(1, stringResource(R.string.how_step1_title), stringResource(R.string.how_step1_body))
                Step(2, stringResource(R.string.how_step2_title), stringResource(R.string.how_step2_body))
                Step(3, stringResource(R.string.how_step3_title), stringResource(R.string.how_step3_body))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                ) {
                    Row(Modifier.padding(Spacing.l)) {
                        Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(Spacing.m))
                        Column {
                            Text(stringResource(R.string.privacy_title), style = MaterialTheme.typography.titleSmall)
                            Text(stringResource(R.string.privacy_body), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                Spacer(Modifier.height(Spacing.s))
            }
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.xl, vertical = Spacing.l)
                    .height(52.dp),
            ) { Text(stringResource(R.string.onboarding_start)) }
        }
    }
}
