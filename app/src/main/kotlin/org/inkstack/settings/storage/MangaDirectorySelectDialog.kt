package org.inkstack.settings.storage

import android.content.Intent
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import org.inkstack.R
import org.inkstack.core.os.OpenDocumentTreeHelper
import org.inkstack.core.ui.compose.ComposeAlertDialogFragment
import org.inkstack.core.util.ext.getDisplayMessage
import org.inkstack.core.util.ext.tryLaunch

@AndroidEntryPoint
class MangaDirectorySelectDialog : ComposeAlertDialogFragment() {

	private val viewModel: MangaDirectorySelectViewModel by viewModels()
	private val pickFileTreeLauncher = OpenDocumentTreeHelper(
		activityResultCaller = this,
		flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
			or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
			or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION,
	) {
		if (it != null) viewModel.onCustomDirectoryPicked(it)
	}

	@Composable
	override fun DialogContent(onDismissRequest: () -> Unit) {
		val context = LocalContext.current
		val items by viewModel.items.collectAsState()

		LaunchedEffect(viewModel) {
			viewModel.onDismissDialog.collectLatest { event ->
				event?.consume { onDismissRequest() }
			}
		}
		LaunchedEffect(viewModel) {
			viewModel.onPickDirectory.collectLatest { event ->
				event?.consume {
					if (!pickFileTreeLauncher.tryLaunch(null)) {
						Toast.makeText(context, R.string.operation_not_supported, Toast.LENGTH_SHORT).show()
					}
				}
			}
		}
		LaunchedEffect(viewModel) {
			viewModel.onError.collectLatest { event ->
				event?.consume { error ->
					Toast.makeText(context, error.getDisplayMessage(context.resources), Toast.LENGTH_SHORT).show()
				}
			}
		}

		AlertDialog(
			onDismissRequest = onDismissRequest,
			title = { Text(stringResource(R.string.manga_save_location)) },
			text = {
				LazyColumn(
					modifier = Modifier
						.fillMaxWidth()
						.heightIn(max = 420.dp),
				) {
					items(items, key = { it.file?.path ?: it.titleRes }) { item ->
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.clickable { viewModel.onItemClick(item) }
								.padding(vertical = 6.dp),
							verticalAlignment = Alignment.CenterVertically,
						) {
							RadioButton(
								selected = item.isChecked,
								onClick = { viewModel.onItemClick(item) },
							)
							Column(modifier = Modifier.padding(start = 4.dp)) {
								Text(
									text = item.title ?: stringResource(item.titleRes),
									style = MaterialTheme.typography.bodyLarge,
								)
								item.file?.absolutePath?.let { path ->
									Text(
										text = path,
										style = MaterialTheme.typography.bodySmall,
										color = MaterialTheme.colorScheme.onSurfaceVariant,
									)
								}
							}
						}
					}
				}
			},
			confirmButton = {},
			dismissButton = {
				TextButton(onClick = onDismissRequest) {
					Text(stringResource(android.R.string.cancel))
				}
			},
		)
	}
}
