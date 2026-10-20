package org.inkstack.favourites.ui.categories.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import org.inkstack.R
import org.inkstack.core.ui.compose.InkstackScaffold
import org.inkstack.core.util.ext.getDisplayMessage
import org.inkstack.core.util.ext.sortedByOrdinal
import org.inkstack.favourites.ui.categories.edit.FavouritesCategoryEditViewModel
import org.inkstack.list.domain.ListSortOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouritesCategoryEditScreen(
	onBack: () -> Unit,
	onSaved: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: FavouritesCategoryEditViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val category by viewModel.category.collectAsState()
	val isLoading by viewModel.isLoading.collectAsState()
	val isTrackerEnabled by viewModel.isTrackerEnabled.collectAsState()
	val sortOrders = remember { ListSortOrder.FAVORITES.sortedByOrdinal() }

	var name by remember { mutableStateOf("") }
	var sortOrder by remember { mutableStateOf(ListSortOrder.NEWEST) }
	var tracker by remember { mutableStateOf(true) }
	var shelf by remember { mutableStateOf(true) }
	var sortExpanded by remember { mutableStateOf(false) }
	var error by remember { mutableStateOf<String?>(null) }
	var initialized by remember { mutableStateOf(false) }

	LaunchedEffect(category) {
		if (!initialized && category != null) {
			name = category?.title.orEmpty()
			sortOrder = category?.order ?: ListSortOrder.NEWEST
			tracker = category?.isTrackingEnabled != false
			shelf = category?.isVisibleInLibrary != false
			initialized = true
		} else if (!initialized && category == null) {
			// create mode still waits for loading to finish
			initialized = !isLoading
		}
	}

	LaunchedEffect(viewModel) {
		viewModel.onSaved.collectLatest { event ->
			event?.consume { onSaved() }
		}
	}
	LaunchedEffect(viewModel) {
		viewModel.onError.collectLatest { event ->
			event?.consume { e ->
				error = e.getDisplayMessage(context.resources)
			}
		}
	}

	val titleRes = if (category == null) R.string.create_category else R.string.edit_category
	val canSave = name.isNotBlank() && !isLoading

	InkstackScaffold(
		modifier = modifier,
		topBar = {
			TopAppBar(
				title = { Text(stringResource(titleRes)) },
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
					}
				},
				actions = {
					Button(
						onClick = {
							error = null
							viewModel.save(name.trim(), sortOrder, tracker, shelf)
						},
						enabled = canSave,
						modifier = Modifier.padding(end = 8.dp),
					) {
						Text(stringResource(R.string.save))
					}
				},
				windowInsets = WindowInsets.statusBars,
				colors = TopAppBarDefaults.topAppBarColors(),
			)
		},
	) { padding ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding)
				.verticalScroll(rememberScrollState())
				.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp),
		) {
			OutlinedTextField(
				value = name,
				onValueChange = {
					name = it
					error = null
				},
				modifier = Modifier.fillMaxWidth(),
				label = { Text(stringResource(R.string.name)) },
				enabled = !isLoading,
				singleLine = true,
			)
			ExposedDropdownMenuBox(
				expanded = sortExpanded,
				onExpandedChange = { if (!isLoading) sortExpanded = it },
			) {
				OutlinedTextField(
					value = stringResource(sortOrder.titleResId),
					onValueChange = {},
					readOnly = true,
					modifier = Modifier
						.fillMaxWidth()
						.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
					label = { Text(stringResource(R.string.sort_order)) },
					trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortExpanded) },
					enabled = !isLoading,
				)
				ExposedDropdownMenu(
					expanded = sortExpanded,
					onDismissRequest = { sortExpanded = false },
				) {
					sortOrders.forEach { order ->
						DropdownMenuItem(
							text = { Text(stringResource(order.titleResId)) },
							onClick = {
								sortOrder = order
								sortExpanded = false
							},
						)
					}
				}
			}
			if (isTrackerEnabled) {
				SwitchRow(
					title = stringResource(R.string.check_for_new_chapters),
					checked = tracker,
					enabled = !isLoading,
					onCheckedChange = { tracker = it },
				)
			}
			SwitchRow(
				title = stringResource(R.string.show_on_shelf),
				checked = shelf,
				enabled = !isLoading,
				onCheckedChange = { shelf = it },
			)
			error?.let {
				Text(
					text = it,
					color = androidx.compose.material3.MaterialTheme.colorScheme.error,
				)
			}
		}
	}
}

@Composable
private fun SwitchRow(
	title: String,
	checked: Boolean,
	enabled: Boolean,
	onCheckedChange: (Boolean) -> Unit,
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically,
	) {
		Text(title, modifier = Modifier.weight(1f))
		Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
	}
}
