package org.inkstack.list.ui.config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.R
import org.inkstack.core.prefs.ListMode
import org.inkstack.core.ui.compose.sheet.ComposeAdaptiveSheet

@AndroidEntryPoint
class ListConfigBottomSheet : ComposeAdaptiveSheet() {

	private val viewModel by viewModels<ListConfigViewModel>()

	@Composable
	override fun sheetTitle(): String = stringResource(R.string.list_options)

	@Composable
	override fun SheetContent(onDismissRequest: () -> Unit) {
		ListConfigSheetContent(viewModel = viewModel)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListConfigSheetContent(viewModel: ListConfigViewModel) {
	var listMode by remember { mutableStateOf(viewModel.listMode) }
	var gridSize by remember { mutableFloatStateOf(viewModel.gridSize.toFloat()) }
	var groupingEnabled by remember { mutableStateOf(viewModel.isGroupingEnabled) }
	var groupingAvailable by remember { mutableStateOf(viewModel.isGroupingAvailable) }
	val sortOrders = remember { viewModel.getSortOrders() }
	var selectedSortIndex by remember { mutableIntStateOf(-1) }
	var sortExpanded by remember { mutableStateOf(false) }

	LaunchedEffect(sortOrders) {
		if (sortOrders != null) {
			val selected = viewModel.getSelectedSortOrder()
			selectedSortIndex = sortOrders.indexOf(selected).coerceAtLeast(0)
		}
	}

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.verticalScroll(rememberScrollState())
			.padding(horizontal = 16.dp, vertical = 8.dp),
		verticalArrangement = Arrangement.spacedBy(12.dp),
	) {
		Text(
			text = stringResource(R.string.list_mode),
			style = MaterialTheme.typography.titleSmall,
		)
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.selectableGroup(),
			horizontalArrangement = Arrangement.spacedBy(8.dp),
		) {
			ListModeChip(
				selected = listMode == ListMode.LIST,
				label = stringResource(R.string.compact),
				icon = R.drawable.ic_list,
				modifier = Modifier.weight(1f),
				onClick = {
					listMode = ListMode.LIST
					viewModel.listMode = ListMode.LIST
				},
			)
			ListModeChip(
				selected = listMode == ListMode.DETAILED_LIST,
				label = stringResource(R.string.details),
				icon = R.drawable.ic_list_detailed,
				modifier = Modifier.weight(1f),
				onClick = {
					listMode = ListMode.DETAILED_LIST
					viewModel.listMode = ListMode.DETAILED_LIST
				},
			)
			ListModeChip(
				selected = listMode == ListMode.GRID,
				label = stringResource(R.string.grid),
				icon = R.drawable.ic_grid,
				modifier = Modifier.weight(1f),
				onClick = {
					listMode = ListMode.GRID
					viewModel.listMode = ListMode.GRID
				},
			)
		}

		if (listMode == ListMode.GRID) {
			Text(
				text = stringResource(R.string.grid_size),
				style = MaterialTheme.typography.titleSmall,
			)
			Slider(
				value = gridSize,
				onValueChange = {
					gridSize = it
					viewModel.gridSize = it.toInt()
				},
				valueRange = 50f..150f,
				modifier = Modifier.fillMaxWidth(),
			)
			Text(
				text = "${gridSize.toInt()}%",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
			)
		}

		if (sortOrders != null && selectedSortIndex in sortOrders.indices) {
			Text(
				text = stringResource(R.string.sort_order),
				style = MaterialTheme.typography.titleSmall,
			)
			ExposedDropdownMenuBox(
				expanded = sortExpanded,
				onExpandedChange = { sortExpanded = it },
			) {
				OutlinedTextField(
					value = stringResource(sortOrders[selectedSortIndex].titleResId),
					onValueChange = {},
					readOnly = true,
					modifier = Modifier
						.fillMaxWidth()
						.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
					trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortExpanded) },
				)
				ExposedDropdownMenu(
					expanded = sortExpanded,
					onDismissRequest = { sortExpanded = false },
				) {
					sortOrders.forEachIndexed { index, order ->
						DropdownMenuItem(
							text = { Text(stringResource(order.titleResId)) },
							onClick = {
								selectedSortIndex = index
								viewModel.setSortOrder(index)
								groupingAvailable = viewModel.isGroupingAvailable
								sortExpanded = false
							},
						)
					}
				}
			}
		}

		if (viewModel.isGroupingSupported) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.selectable(
						selected = groupingEnabled,
						enabled = groupingAvailable,
						role = Role.Switch,
						onClick = {
							if (!groupingAvailable) return@selectable
							groupingEnabled = !groupingEnabled
							viewModel.isGroupingEnabled = groupingEnabled
						},
					)
					.padding(vertical = 4.dp),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.SpaceBetween,
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(12.dp),
					modifier = Modifier.weight(1f),
				) {
					Icon(
						painter = painterResource(R.drawable.ic_list_group),
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onSurfaceVariant,
					)
					Text(
						text = stringResource(R.string.group),
						style = MaterialTheme.typography.bodyLarge,
					)
				}
				Switch(
					checked = groupingEnabled,
					onCheckedChange = {
						groupingEnabled = it
						viewModel.isGroupingEnabled = it
					},
					enabled = groupingAvailable,
				)
			}
		}
	}
}

@Composable
private fun ListModeChip(
	selected: Boolean,
	label: String,
	icon: Int,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	FilterChip(
		selected = selected,
		onClick = onClick,
		modifier = modifier,
		label = {
			Text(
				text = label,
				style = MaterialTheme.typography.labelMedium,
				maxLines = 1,
				softWrap = false,
			)
		},
		leadingIcon = {
			Icon(painter = painterResource(icon), contentDescription = null)
		},
	)
}
