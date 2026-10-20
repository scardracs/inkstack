package org.inkstack.settings.about.compose

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import org.inkstack.R
import org.inkstack.core.ui.compose.InkstackScaffold
import org.inkstack.core.util.FileSize
import org.inkstack.core.util.ext.getDisplayMessage
import org.inkstack.core.util.ext.printStackTraceDebug
import org.inkstack.settings.about.AppUpdateViewModel

@Composable
fun AppUpdateScreen(
	onCancel: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: AppUpdateViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val version by viewModel.nextVersion.collectAsState()
	val isLoading by viewModel.isLoading.collectAsState()
	val downloadProgress by viewModel.downloadProgress.collectAsState()
	val downloadState by viewModel.downloadState.collectAsState()
	var error by remember { mutableStateOf<String?>(null) }

	DisposableEffect(viewModel) {
		val receiver = object : BroadcastReceiver() {
			override fun onReceive(ctx: Context, intent: Intent) {
				if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
					viewModel.onDownloadComplete(intent)
				}
			}
		}
		ContextCompat.registerReceiver(
			context,
			receiver,
			IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
			ContextCompat.RECEIVER_EXPORTED,
		)
		onDispose { context.unregisterReceiver(receiver) }
	}

	LaunchedEffect(viewModel) {
		viewModel.onError.collectLatest { event ->
			event?.consume { e -> error = e.getDisplayMessage(context.resources) }
		}
	}
	LaunchedEffect(viewModel) {
		viewModel.onDownloadDone.collectLatest { event ->
			event?.consume { intent ->
				try {
					context.startActivity(intent)
				} catch (e: ActivityNotFoundException) {
					e.printStackTraceDebug()
				}
			}
		}
	}

	val stateError = when (downloadState) {
		DownloadManager.STATUS_FAILED -> stringResource(R.string.error_occurred)
		DownloadManager.STATUS_PAUSED -> stringResource(R.string.downloads_paused)
		else -> null
	}

	InkstackScaffold(modifier = modifier) { padding ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding)
				.padding(24.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			Icon(
				painter = painterResource(R.drawable.ic_app_update),
				contentDescription = null,
				tint = MaterialTheme.colorScheme.primary,
			)
			Text(
				stringResource(R.string.app_update_available),
				style = MaterialTheme.typography.headlineSmall,
				textAlign = TextAlign.Center,
				modifier = Modifier.padding(top = 16.dp),
			)
			if (isLoading) {
				LinearProgressIndicator(
					progress = { downloadProgress.coerceIn(0f, 1f) },
					modifier = Modifier
						.fillMaxWidth()
						.padding(top = 16.dp),
				)
			}
			(error ?: stateError)?.let {
				Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
			}
			Column(
				modifier = Modifier
					.weight(1f)
					.fillMaxWidth()
					.verticalScroll(rememberScrollState())
					.padding(top = 16.dp),
			) {
				if (version == null) {
					Text(stringResource(R.string.loading_))
				} else {
					Text(stringResource(R.string.new_version_s, version!!.name))
					Text(stringResource(R.string.size_s, FileSize.BYTES.format(context, version!!.apkSize)))
					Spacer(Modifier.padding(8.dp))
					Text(version!!.description)
				}
			}
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
			) {
				OutlinedButton(onClick = onCancel) {
					Text(stringResource(android.R.string.cancel))
				}
				Button(
					onClick = {
						viewModel.installIntent.value?.let { intent ->
							try {
								context.startActivity(intent)
							} catch (e: Exception) {
								error = e.getDisplayMessage(context.resources)
							}
						} ?: viewModel.startDownload()
					},
					enabled = version != null && !isLoading,
				) {
					Text(stringResource(R.string.update))
				}
			}
		}
	}
}
