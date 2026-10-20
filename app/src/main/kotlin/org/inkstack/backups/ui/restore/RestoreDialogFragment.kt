package org.inkstack.backups.ui.restore

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import org.inkstack.R
import org.inkstack.core.nav.router
import org.inkstack.core.ui.compose.ComposeAlertDialogFragment
import org.inkstack.core.util.ext.getDisplayMessage
import java.text.DateFormat
import java.text.SimpleDateFormat

@AndroidEntryPoint
class RestoreDialogFragment : ComposeAlertDialogFragment() {

	private val viewModel: RestoreViewModel by viewModels()

	@Composable
	override fun DialogContent(onDismissRequest: () -> Unit) {
		RestoreDialogContent(
			viewModel = viewModel,
			onDismissRequest = onDismissRequest,
			onRestore = {
				val started = RestoreService.start(
					requireContext(),
					viewModel.uri ?: return@RestoreDialogContent false,
					viewModel.getCheckedSections(),
				)
				if (started) {
					Toast.makeText(requireContext(), R.string.backup_restored_background, Toast.LENGTH_SHORT).show()
					router.closeWelcomeSheet()
					onDismissRequest()
				} else {
					Toast.makeText(requireContext(), R.string.operation_not_supported, Toast.LENGTH_SHORT).show()
				}
				started
			},
		)
	}
}

@Composable
private fun RestoreDialogContent(
	viewModel: RestoreViewModel,
	onDismissRequest: () -> Unit,
	onRestore: () -> Boolean,
) {
	val context = LocalContext.current
	val isLoading by viewModel.isLoading.collectAsState()
	val entries by viewModel.availableEntries.collectAsState()
	val backupDate by viewModel.backupDate.collectAsState()
	var errorMessage by remember { mutableStateOf<String?>(null) }

	LaunchedEffect(viewModel) {
		viewModel.onError.collectLatest { event ->
			event?.consume { error ->
				errorMessage = error.getDisplayMessage(context.resources)
			}
		}
	}

	val subtitle = when {
		!isLoading -> backupDate?.let { date ->
			stringResource(
				R.string.backup_date_,
				SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(date),
			)
		}
		entries.isNotEmpty() -> stringResource(R.string.processing_)
		else -> stringResource(R.string.loading_)
	}

	AlertDialog(
		onDismissRequest = {},
		title = { Text(stringResource(R.string.restore_backup)) },
		text = {
			Column(modifier = Modifier.fillMaxWidth()) {
				if (isLoading) {
					LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
				} else {
					LazyColumn(
						modifier = Modifier
							.fillMaxWidth()
							.heightIn(max = 360.dp),
					) {
						items(entries, key = { it.section }) { item ->
							Row(
								modifier = Modifier
									.fillMaxWidth()
									.clickable(enabled = item.isEnabled) { viewModel.onItemClick(item) }
									.padding(vertical = 4.dp),
								verticalAlignment = Alignment.CenterVertically,
							) {
								Checkbox(
									checked = item.isChecked,
									onCheckedChange = { viewModel.onItemClick(item) },
									enabled = item.isEnabled,
								)
								Text(
									text = stringResource(item.titleResId),
									style = MaterialTheme.typography.bodyLarge,
								)
							}
						}
					}
				}
				if (!subtitle.isNullOrEmpty()) {
					Text(
						text = subtitle,
						style = MaterialTheme.typography.labelMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						modifier = Modifier.padding(top = 8.dp),
					)
				}
			}
		},
		confirmButton = {
			TextButton(
				onClick = { onRestore() },
				enabled = !isLoading && entries.any { it.isChecked },
			) {
				Text(stringResource(R.string.restore))
			}
		},
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
