package org.inkstack.backups.ui.backup

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import org.inkstack.R
import org.inkstack.core.ui.compose.ComposeAlertDialogFragment
import org.inkstack.core.util.ext.getDisplayMessage
import org.inkstack.core.util.progress.Progress

@AndroidEntryPoint
class BackupDialogFragment : ComposeAlertDialogFragment() {

	private val viewModel by viewModels<BackupViewModel>()

	@Composable
	override fun DialogContent(onDismissRequest: () -> Unit) {
		BackupDialogContent(viewModel = viewModel, onDismissRequest = onDismissRequest)
	}
}

@Composable
private fun BackupDialogContent(
	viewModel: BackupViewModel,
	onDismissRequest: () -> Unit,
) {
	val context = LocalContext.current
	val progress by viewModel.progress.collectAsState()
	var errorMessage by remember { mutableStateOf<String?>(null) }

	LaunchedEffect(viewModel) {
		viewModel.onBackupDone.collectLatest { event ->
			event?.consume { _: Uri ->
				Toast.makeText(context, R.string.backup_saved, Toast.LENGTH_SHORT).show()
				onDismissRequest()
			}
		}
	}
	LaunchedEffect(viewModel) {
		viewModel.onError.collectLatest { event ->
			event?.consume { error ->
				errorMessage = error.getDisplayMessage(context.resources)
			}
		}
	}

	AlertDialog(
		onDismissRequest = {},
		title = { Text(stringResource(R.string.create_backup)) },
		text = {
			Column(modifier = Modifier.fillMaxWidth()) {
				Text(
					text = stringResource(R.string.processing_),
					style = MaterialTheme.typography.bodyMedium,
					modifier = Modifier.padding(bottom = 8.dp),
				)
				ProgressBar(progress)
			}
		},
		confirmButton = {},
		dismissButton = {
			TextButton(onClick = onDismissRequest) {
				Text(stringResource(android.R.string.cancel))
			}
		},
	)

	errorMessage?.let { msg ->
		AlertDialog(
			onDismissRequest = {
				errorMessage = null
				onDismissRequest()
			},
			title = { Text(stringResource(R.string.error)) },
			text = { Text(msg) },
			confirmButton = {
				TextButton(
					onClick = {
						errorMessage = null
						onDismissRequest()
					},
				) {
					Text(stringResource(R.string.close))
				}
			},
		)
	}
}

@Composable
private fun ProgressBar(progress: Progress) {
	if (progress.isIndeterminate) {
		LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
	} else {
		LinearProgressIndicator(
			progress = { progress.percent },
			modifier = Modifier.fillMaxWidth(),
		)
	}
}
