package com.stopscroll.app.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.stopscroll.app.R
import com.stopscroll.app.service.ExtensionRequests
import com.stopscroll.app.ui.theme.Spacing
import com.stopscroll.app.ui.theme.setThemedContent

/**
 * Shown by the service over a target app whose allowance just expired. "OK" and back go HOME;
 * "+30 s" asks the service for an extension, which relaunches the target once granted.
 */
class TimeUpActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val packageName = intent.getStringExtra(EXTRA_PACKAGE)
        if (packageName == null) {
            finish()
            return
        }
        val blockMillis = intent.getLongExtra(EXTRA_BLOCK_MILLIS, NONE).takeIf { it > 0L }
        val offerMillis = intent.getLongExtra(EXTRA_OFFER_MILLIS, NONE).takeIf { it > 0L }
        val label = appLabel(packageName)

        setThemedContent {
            Surface(color = MaterialTheme.colorScheme.background) {
                BackHandler(onBack = ::goHome)
                TimeUpScreen(
                    packageName = packageName,
                    label = label,
                    blockMillis = blockMillis,
                    offerMillis = offerMillis,
                    onExtend = { ExtensionRequests.request(packageName) },
                    onClose = ::goHome,
                )
            }
        }
    }

    private fun appLabel(packageName: String): String = try {
        packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)).toString()
    } catch (_: PackageManager.NameNotFoundException) {
        packageName
    }

    private fun goHome() {
        startActivity(
            Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        finish()
    }

    companion object {
        private const val EXTRA_PACKAGE = "package"
        private const val EXTRA_BLOCK_MILLIS = "block_millis"
        private const val EXTRA_OFFER_MILLIS = "offer_millis"
        private const val NONE = -1L

        /** [offerMillis] is the doubled block duration of an available extension, or null for none. */
        fun intent(context: Context, packageName: String, blockMillis: Long?, offerMillis: Long?): Intent =
            Intent(context, TimeUpActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .putExtra(EXTRA_PACKAGE, packageName)
                .putExtra(EXTRA_BLOCK_MILLIS, blockMillis ?: NONE)
                .putExtra(EXTRA_OFFER_MILLIS, offerMillis ?: NONE)
    }
}

@Composable
private fun TimeUpScreen(
    packageName: String,
    label: String,
    blockMillis: Long?,
    offerMillis: Long?,
    onExtend: () -> Unit,
    onClose: () -> Unit,
) {
    var requested by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(colors.primaryContainer, colors.background)))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = Spacing.xl, vertical = Spacing.l),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.l, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(colors.surfaceContainerLowest),
                    contentAlignment = Alignment.Center,
                ) {
                    AppIcon(packageName, size = 76.dp)
                }
                BrandMark(
                    size = 44.dp,
                    modifier = Modifier.align(Alignment.BottomEnd).offset(x = 4.dp, y = 4.dp),
                )
            }
            Spacer(Modifier.height(Spacing.s))
            Text(
                text = stringResource(R.string.time_up_title),
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center,
            )
            blockMillis?.let {
                Text(
                    text = stringResource(R.string.time_up_block, label, formatDuration(it)),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
            }
            Text(
                text = stringResource(R.string.time_up_kind),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            offerMillis?.let {
                Text(
                    text = stringResource(R.string.time_up_extend_hint, formatDuration(it)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                FilledTonalButton(
                    onClick = {
                        requested = true
                        onExtend()
                    },
                    enabled = !requested,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) { Text(stringResource(R.string.time_up_extend)) }
            }
            Button(onClick = onClose, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text(stringResource(R.string.time_up_ok))
            }
        }
    }
}
