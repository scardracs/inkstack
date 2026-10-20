package org.inkstack.settings.storage.directories.compose

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import org.inkstack.R
import org.inkstack.core.ui.compose.InkstackScaffold
import org.inkstack.core.util.FileSize
import org.inkstack.core.util.ext.getDisplayMessage
import org.inkstack.settings.storage.directories.MangaDirectoriesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaDirectoriesScreen(
	onBack: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: MangaDirectoriesViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val items by viewModel.items.collectAsState()
	val isLoading by viewModel.isLoading.collectAsState()
	val snackbarHostState = remember { SnackbarHostState() }
	val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
		if (uri != null) {
			val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
			context.contentResolver.takePersistableUriPermission(uri, flags)
			viewModel.onCustomDirectoryPicked(uri)
		}
	}

	LaunchedEffect(viewModel) {
		viewModel.onError.collectLatest { event ->
			event?.consume { error ->
				snackbarHostState.showSnackbar(error.getDisplayMessage(context.resources))
			}
		}
	}

	InkstackScaffold(
		modifier = modifier,
		snackbarHostState = snackbarHostState,
		topBar = {
			TopAppBar(
				title = { Text(stringResource(R.string.directories)) },
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
					}
				},
				windowInsets = WindowInsets.statusBars,
				colors = TopAppBarDefaults.topAppBarColors(),
			)
		},
		floatingActionButton = {
			FloatingActionButton(
				onClick = {
					try {
						picker.launch(null)
					} catch (_: Exception) {
						Toast.makeText(context, R.string.operation_not_supported, Toast.LENGTH_SHORT).show()
					}
				},
			) {
				Icon(Icons.Default.Add, contentDescription = stringResource(R.string.pick_custom_directory))
			}
		},
	) { padding ->
		LazyColumn(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding),
		) {
			if (isLoading) {
				item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
			}
			items(items, key = { it.path.absolutePath }) { item ->
				ListItem(
					headlineContent = { Text(item.title) },
					supportingContent = {
						Text(
							buildString {
								append(item.path.absolutePath)
								if (item.isDefault) {
									append('\n')
									append(context.getString(R.string.download_default_directory))
								}
								if (!item.isAccessible) {
									append('\n')
									append(context.getString(R.string.no_write_permission_to_file))
								}
								if (item.isAppPrivate) {
									append('\n')
									append(context.getString(R.string.private_app_directory_warning))
								}
								append('\n')
								append(
									context.getString(
										R.string.available_pattern,
										FileSize.BYTES.format(context, item.available),
									),
								)
							},
						)
					},
					trailingContent = {
						if (!item.isAppPrivate) {
							IconButton(
								onClick = { viewModel.onRemoveClick(item.path) },
								enabled = !item.isDefault,
							) {
								Icon(Icons.Default.Delete, contentDescription = null)
							}
						}
					},
				)
			}
		}
	}
}
