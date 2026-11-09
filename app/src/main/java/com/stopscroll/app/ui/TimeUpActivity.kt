package com.stopscroll.app.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.stopscroll.app.R
import com.stopscroll.app.service.ExtensionRequests

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

        enableEdgeToEdge()
        setContent {
            MaterialTheme {
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
    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AppIcon(packageName, size = 72.dp)
            Text(label, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
            Text(stringResource(R.string.time_up_title), style = MaterialTheme.typography.headlineMedium)
            blockMillis?.let {
                Text(stringResource(R.string.time_up_block, formatDuration(it)), textAlign = TextAlign.Center)
            }
            offerMillis?.let {
                OutlinedButton(
                    onClick = {
                        requested = true
                        onExtend()
                    },
                    enabled = !requested,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.time_up_extend)) }
                Text(
                    stringResource(R.string.time_up_extend_hint, formatDuration(it)),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
            }
            Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.time_up_ok))
            }
        }
    }
}
