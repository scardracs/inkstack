package org.inkstack.core.crash

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.inkstack.R
import org.inkstack.core.exceptions.CaughtException
import org.inkstack.core.ui.BaseComposeActivity
import org.inkstack.core.util.ext.getSerializableExtraCompat
import org.inkstack.core.util.ext.printStackTraceDebug

/**
 * Consent UI before posting a crash/error report to GitHub Issues.
 */
@AndroidEntryPoint
class CrashReportActivity : BaseComposeActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val pending = CrashReportStore.peek(this)
		val liveError = intent.getSerializableExtraCompat<Throwable>(EXTRA_THROWABLE)
		val kind = runCatching {
			GitHubIssuesReporter.Kind.valueOf(
				intent.getStringExtra(EXTRA_KIND) ?: GitHubIssuesReporter.Kind.IN_APP.name,
			)
		}.getOrDefault(GitHubIssuesReporter.Kind.IN_APP)
		val report = when {
			pending != null -> GitHubIssuesReporter.Report.from(pending)
			liveError != null -> GitHubIssuesReporter.Report.from(liveError, kind)
			else -> null
		}
		if (report == null) {
			finish()
			return
		}
		setComposeContent {
			CrashReportDialog(
				report = report,
				onFinished = {
					CrashReportStore.clear(this)
					finish()
				},
			)
		}
	}

	companion object {
		const val EXTRA_THROWABLE = "crash.throwable"
		const val EXTRA_KIND = "crash.kind"

		fun intent(context: Context, error: Throwable, kind: GitHubIssuesReporter.Kind): Intent =
			Intent(context, CrashReportActivity::class.java)
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
				.putExtra(EXTRA_THROWABLE, CaughtException(error))
				.putExtra(EXTRA_KIND, kind.name)

		fun intentForPending(context: Context): Intent =
			Intent(context, CrashReportActivity::class.java)
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
	}
}

@Composable
private fun CrashReportDialog(
	report: GitHubIssuesReporter.Report,
	onFinished: () -> Unit,
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	var sending by remember { mutableStateOf(false) }
	var errorText by remember { mutableStateOf<String?>(null) }

	BackHandler(enabled = !sending, onBack = onFinished)

	AlertDialog(
		onDismissRequest = { if (!sending) onFinished() },
		title = { Text(stringResource(R.string.error_occurred)) },
		text = {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.verticalScroll(rememberScrollState()),
				verticalArrangement = Arrangement.spacedBy(12.dp),
			) {
				Text(stringResource(R.string.crash_text))
				if (!GitHubIssuesReporter.isEnabled) {
					Text(
						text = stringResource(R.string.crash_reporting_unavailable),
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.error,
					)
				}
				if (sending) {
					CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
				}
				errorText?.let {
					Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
				}
				Text(
					text = report.stackTrace.take(1_500),
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
				)
			}
		},
		confirmButton = {
			TextButton(
				enabled = !sending && GitHubIssuesReporter.isEnabled,
				onClick = {
					sending = true
					errorText = null
					scope.launch {
						val result = GitHubIssuesReporter.submit(context, report)
						result.onSuccess {
							onFinished()
						}.onFailure {
							it.printStackTraceDebug()
							sending = false
							errorText = it.message ?: context.getString(R.string.error_occurred)
						}
					}
				},
			) {
				Text(stringResource(R.string.send))
			}
		},
		dismissButton = {
			TextButton(enabled = !sending, onClick = onFinished) {
				Text(stringResource(R.string.no_thanks))
			}
		},
	)
}
