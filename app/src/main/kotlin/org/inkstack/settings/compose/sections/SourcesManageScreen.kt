package org.inkstack.settings.compose.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import org.inkstack.R
import org.inkstack.core.model.getTitle
import org.inkstack.core.nav.router
import org.inkstack.core.util.ext.findActivity
import org.inkstack.settings.sources.manage.SourcesManageViewModel
import org.inkstack.settings.sources.model.SourceConfigItem

@Composable
fun SourcesManageScreen(
	onBack: () -> Unit,
	onOpenSource: (String) -> Unit,
	modifier: Modifier = Modifier,
	viewModel: SourcesManageViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val snackbarHostState = remember { SnackbarHostState() }
	val activity = context.findActivity() as? FragmentActivity
	val content by viewModel.content.collectAsState()
	var query by remember { mutableStateOf("") }

	LaunchedEffect(viewModel) {
		viewModel.onActionDone.collectLatest { event ->
			event?.consume { action ->
				snackbarHostState.showSnackbar(context.getString(action.stringResId))
			}
		}
	}

	SettingsSectionScaffold(
		titleRes = R.string.manage_sources,
		onBack = onBack,
		modifier = modifier,
		snackbarHostState = snackbarHostState,
		actions = {
			TextButton(onClick = { activity?.router?.openSourcesCatalog() }) {
				Text(stringResource(R.string.sources_catalog))
			}
		},
	) {
		item {
			OutlinedTextField(
				value = query,
				onValueChange = {
					query = it
					viewModel.performSearch(it)
				},
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 16.dp, vertical = 8.dp),
				leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
				trailingIcon = {
					if (query.isNotEmpty()) {
						IconButton(
							onClick = {
								query = ""
								viewModel.performSearch("")
							},
						) {
							Icon(Icons.Default.Close, contentDescription = null)
						}
					}
				},
				singleLine = true,
				placeholder = { Text(stringResource(R.string.search)) },
			)
		}
		items(content.size, key = { index ->
			when (val item = content[index]) {
				is SourceConfigItem.SourceItem -> item.source.name
				is SourceConfigItem.Tip -> "tip:${item.key}"
				SourceConfigItem.EmptySearchResult -> "empty"
			}
		}) { index ->
			when (val item = content[index]) {
				is SourceConfigItem.SourceItem -> {
					ListItem(
						headlineContent = { Text(item.source.getTitle(context)) },
						supportingContent = {
							if (item.isPinned) Text(stringResource(R.string.pin))
						},
						leadingContent = {
							Switch(
								checked = item.isEnabled,
								onCheckedChange = { viewModel.setEnabled(item.source, it) },
								enabled = item.isDisableAvailable || item.isEnabled,
							)
						},
						trailingContent = {
							IconButton(onClick = { onOpenSource(item.source.name) }) {
								Icon(Icons.Default.Settings, contentDescription = null)
							}
						},
						modifier = Modifier
							.fillMaxWidth()
							.clickable { onOpenSource(item.source.name) },
					)
				}
				is SourceConfigItem.Tip -> {
					ListItem(
						headlineContent = { Text(stringResource(item.textResId)) },
						trailingContent = {
							IconButton(onClick = { viewModel.onTipClosed(item) }) {
								Icon(Icons.Default.Close, contentDescription = null)
							}
						},
					)
				}
				SourceConfigItem.EmptySearchResult -> {
					ListItem(
						headlineContent = { Text(stringResource(R.string.nothing_found)) },
					)
				}
			}
		}
	}
}
