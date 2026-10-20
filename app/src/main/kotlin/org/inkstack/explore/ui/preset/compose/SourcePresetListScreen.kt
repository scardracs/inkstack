package org.inkstack.explore.ui.preset.compose

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import org.inkstack.R
import org.inkstack.core.ui.compose.InkstackScaffold
import org.inkstack.core.util.ext.getDisplayMessage
import org.inkstack.core.util.ext.getDisplayName
import org.inkstack.core.util.ext.toLocale
import org.inkstack.explore.data.SourcePreset
import org.inkstack.explore.ui.preset.SourcePresetListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourcePresetListScreen(
	onBack: () -> Unit,
	onCreate: () -> Unit,
	onEdit: (Long) -> Unit,
	modifier: Modifier = Modifier,
	viewModel: SourcePresetListViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val presets by viewModel.presets.collectAsState()
	val snackbarHostState = remember { SnackbarHostState() }
	var activeId by remember { mutableLongStateOf(viewModel.activePresetId) }
	var pendingDelete by remember { mutableStateOf<SourcePreset?>(null) }

	LaunchedEffect(viewModel) {
		viewModel.onError.collectLatest { event ->
			event?.consume { error ->
				snackbarHostState.showSnackbar(error.getDisplayMessage(context.resources))
			}
		}
	}
	LaunchedEffect(viewModel) {
		viewModel.onPresetDeleted.collectLatest { event ->
			event?.consume {
				activeId = viewModel.activePresetId
				snackbarHostState.showSnackbar(context.getString(R.string.preset_deleted))
			}
		}
	}

	InkstackScaffold(
		modifier = modifier,
		snackbarHostState = snackbarHostState,
		topBar = {
			TopAppBar(
				title = { Text(stringResource(R.string.source_presets)) },
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
			FloatingActionButton(onClick = onCreate) {
				Icon(Icons.Default.Add, contentDescription = stringResource(R.string.create_preset))
			}
		},
	) { padding ->
		LazyColumn(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding),
		) {
			items(presets, key = { it.id }) { preset ->
				val count = viewModel.countSourcesForPreset(preset)
				val langNames = preset.languages.map { it.toLocale().getDisplayName(context) }
				val countText = pluralStringResource(R.plurals.sources_count, count, count)
				val subtitle = if (langNames.isNotEmpty()) {
					"${langNames.joinToString()} — $countText"
				} else {
					countText
				}
				ListItem(
					headlineContent = { Text(preset.title) },
					supportingContent = { Text(subtitle) },
					leadingContent = {
						RadioButton(
							selected = preset.id == activeId,
							onClick = {
								val newId = if (preset.id == activeId) 0L else preset.id
								viewModel.setActivePreset(newId)
								activeId = newId
							},
						)
					},
					trailingContent = {
						IconButton(onClick = { onEdit(preset.id) }) {
							Icon(Icons.Default.Edit, contentDescription = null)
						}
						IconButton(onClick = { pendingDelete = preset }) {
							Icon(Icons.Default.Delete, contentDescription = null)
						}
					},
					modifier = Modifier
						.fillMaxWidth()
						.clickable {
							val newId = if (preset.id == activeId) 0L else preset.id
							viewModel.setActivePreset(newId)
							activeId = newId
						},
				)
			}
		}
	}

	pendingDelete?.let { preset ->
		AlertDialog(
			onDismissRequest = { pendingDelete = null },
			title = { Text(preset.title) },
			confirmButton = {
				TextButton(
					onClick = {
						viewModel.deletePreset(preset.id)
						pendingDelete = null
					},
				) {
					Text(stringResource(R.string.remove))
				}
			},
			dismissButton = {
				TextButton(onClick = { pendingDelete = null }) {
					Text(stringResource(android.R.string.cancel))
				}
			},
		)
	}
}
