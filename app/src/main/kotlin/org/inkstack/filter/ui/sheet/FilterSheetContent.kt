package org.inkstack.filter.ui.sheet

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import org.inkstack.R
import org.inkstack.core.model.titleResId
import org.inkstack.core.ui.model.titleRes
import org.inkstack.core.util.AlphanumComparator
import org.inkstack.core.util.ext.getDisplayMessage
import org.inkstack.core.util.ext.getDisplayName
import org.inkstack.filter.data.PersistableFilter
import org.inkstack.filter.data.PersistableFilter.Companion.MAX_TITLE_LENGTH
import org.inkstack.filter.ui.FilterCoordinator
import org.inkstack.filter.ui.model.FilterProperty
import org.inkstack.parsers.model.YEAR_UNKNOWN
import org.inkstack.parsers.util.mapToSet
import org.inkstack.parsers.util.toIntUp
import java.util.TreeSet

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterSheetContent(
	filter: FilterCoordinator,
	showsDialog: Boolean,
	onShowTagsCatalog: (excludeMode: Boolean) -> Unit,
	onDismissRequest: () -> Unit,
) {
	val context = LocalContext.current
	val sortOrder by filter.sortOrder.collectAsState()
	val locale by filter.locale.collectAsState()
	val originalLocale by filter.originalLocale.collectAsState()
	val tags by filter.tags.collectAsState()
	val tagsExcluded by filter.tagsExcluded.collectAsState()
	val authors by filter.authors.collectAsState()
	val states by filter.states.collectAsState()
	val contentTypes by filter.contentTypes.collectAsState()
	val contentRating by filter.contentRating.collectAsState()
	val demographics by filter.demographics.collectAsState()
	val year by filter.year.collectAsState()
	val yearRange by filter.yearRange.collectAsState()
	val savedFilters by filter.savedFilters.collectAsState()
	val snapshot by filter.observe().collectAsState(initial = filter.snapshot())

	var saveDialogOpen by remember { mutableStateOf(false) }
	var renameTarget by remember { mutableStateOf<PersistableFilter?>(null) }
	var overwriteName by remember { mutableStateOf<String?>(null) }
	var presetMenu by remember { mutableStateOf<PersistableFilter?>(null) }

	val canSave = snapshot.listFilter.isNotEmpty() && savedFilters.selectedItems.isEmpty()

	Column(modifier = Modifier.fillMaxWidth()) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 12.dp, vertical = 8.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp),
		) {
			DropdownSection(
				title = stringResource(R.string.sort_order),
				property = sortOrder,
				label = { stringResource(it.titleRes) },
				onSelect = { filter.setSortOrder(it) },
			)
			if (!savedFilters.isEmpty()) {
				Text(stringResource(R.string.saved_filters), style = MaterialTheme.typography.titleSmall)
				FlowRow(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					verticalArrangement = Arrangement.spacedBy(8.dp),
				) {
					savedFilters.availableItems.forEach { preset ->
						val checked = preset in savedFilters.selectedItems
						FilterChip(
							selected = checked,
							onClick = { filter.setAdjusted(preset.filter) },
							label = { Text(preset.name) },
							trailingIcon = {
								Icon(
									imageVector = Icons.Filled.MoreVert,
									contentDescription = stringResource(R.string.more_options),
									modifier = Modifier
										.size(18.dp)
										.clickable { presetMenu = preset },
								)
							},
						)
					}
				}
			}
			DropdownSection(
				title = stringResource(R.string.language),
				property = locale,
				label = { localeItem -> localeItem.getDisplayName(context) },
				onSelect = { filter.setLocale(it) },
			)
			DropdownSection(
				title = stringResource(R.string.original_language),
				property = originalLocale,
				label = { localeItem -> localeItem.getDisplayName(context) },
				onSelect = { filter.setOriginalLocale(it) },
			)
			ChipSection(
				title = stringResource(
					if (filter.capabilities.isMultipleTagsSupported) R.string.genres else R.string.genre,
				),
				property = tags,
				label = { it.title },
				error = tags.error?.getDisplayMessage(context.resources),
				onToggle = { tag, checked -> filter.toggleTag(tag, !checked) },
				onMore = { onShowTagsCatalog(false) },
			)
			ChipSection(
				title = stringResource(R.string.genres_exclude),
				property = tagsExcluded,
				label = { it.title },
				onToggle = { tag, checked -> filter.toggleTagExclude(tag, !checked) },
				onMore = { onShowTagsCatalog(true) },
			)
			ChipSection(
				title = stringResource(R.string.author),
				property = authors,
				label = { it },
				onToggle = { author, checked ->
					if (checked) filter.setAuthor(null) else filter.setAuthor(author)
				},
			)
			ChipSection(
				title = stringResource(R.string.state),
				property = states,
				label = { stringResource(it.titleResId) },
				onToggle = { state, checked -> filter.toggleState(state, !checked) },
			)
			ChipSection(
				title = stringResource(R.string.type),
				property = contentTypes,
				label = { stringResource(it.titleResId) },
				onToggle = { type, checked -> filter.toggleContentType(type, !checked) },
			)
			ChipSection(
				title = stringResource(R.string.content_rating),
				property = contentRating,
				label = { stringResource(it.titleResId) },
				onToggle = { rating, checked -> filter.toggleContentRating(rating, !checked) },
			)
			ChipSection(
				title = stringResource(R.string.demographics),
				property = demographics,
				label = { stringResource(it.titleResId) },
				onToggle = { demo, checked -> filter.toggleDemographic(demo, !checked) },
			)
			YearSliderSection(year = year, onChange = filter::setYear)
			YearRangeSliderSection(yearRange = yearRange, onChange = filter::setYearRange)
		}
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 12.dp, vertical = 8.dp),
			horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
			verticalAlignment = Alignment.CenterVertically,
		) {
			OutlinedButton(
				onClick = { saveDialogOpen = true },
				enabled = canSave,
			) {
				Text(stringResource(R.string.save))
			}
			if (showsDialog) {
				Button(onClick = onDismissRequest) {
					Text(stringResource(R.string.done))
				}
			}
		}
	}

	presetMenu?.let { preset ->
		AlertDialog(
			onDismissRequest = { presetMenu = null },
			title = { Text(preset.name) },
			text = null,
			confirmButton = {
				TextButton(
					onClick = {
						filter.deleteSavedFilter(preset.id)
						presetMenu = null
					},
				) { Text(stringResource(R.string.delete)) }
			},
			dismissButton = {
				TextButton(
					onClick = {
						renameTarget = preset
						presetMenu = null
					},
				) { Text(stringResource(R.string.rename)) }
			},
		)
	}

	if (saveDialogOpen) {
		SaveFilterNameDialog(
			initial = "",
			existingNames = savedFilters.availableItems.mapTo(TreeSet(AlphanumComparator()), PersistableFilter::name),
			onDismiss = { saveDialogOpen = false },
			onSave = { name ->
				saveDialogOpen = false
				if (name in savedFilters.availableItems.mapToSet { it.name }) {
					overwriteName = name
				} else {
					filter.saveCurrentFilter(name)
				}
			},
		)
	}
	renameTarget?.let { preset ->
		SaveFilterNameDialog(
			initial = preset.name,
			existingNames = savedFilters.availableItems.mapToSet { it.name },
			onDismiss = { renameTarget = null },
			onSave = { name ->
				if (name.isEmpty() || name in savedFilters.availableItems.mapToSet { it.name }) {
					Toast.makeText(context, R.string.invalid_value_message, Toast.LENGTH_SHORT).show()
				} else {
					filter.renameSavedFilter(preset.id, name)
					renameTarget = null
				}
			},
		)
	}
	overwriteName?.let { name ->
		AlertDialog(
			onDismissRequest = { overwriteName = null },
			title = { Text(stringResource(R.string.save_filter)) },
			text = { Text(stringResource(R.string.filter_overwrite_confirm, name)) },
			confirmButton = {
				TextButton(
					onClick = {
						filter.saveCurrentFilter(name)
						overwriteName = null
					},
				) { Text(stringResource(R.string.overwrite)) }
			},
			dismissButton = {
				TextButton(
					onClick = {
						overwriteName = null
						saveDialogOpen = true
					},
				) { Text(stringResource(android.R.string.cancel)) }
			},
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> DropdownSection(
	title: String,
	property: FilterProperty<T>,
	label: @Composable (T) -> String,
	onSelect: (T) -> Unit,
) {
	if (property.isEmpty()) return
	val selected = property.selectedItems.firstOrNull() ?: return
	var expanded by remember { mutableStateOf(false) }
	Column(modifier = Modifier.fillMaxWidth()) {
		Text(title, style = MaterialTheme.typography.titleSmall)
		ExposedDropdownMenuBox(
			expanded = expanded,
			onExpandedChange = { expanded = it },
			modifier = Modifier.padding(top = 4.dp),
		) {
			OutlinedTextField(
				value = label(selected),
				onValueChange = {},
				readOnly = true,
				modifier = Modifier
					.fillMaxWidth()
					.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
				trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
			)
			ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
				property.availableItems.forEach { item ->
					DropdownMenuItem(
						text = { Text(label(item)) },
						onClick = {
							onSelect(item)
							expanded = false
						},
					)
				}
			}
		}
	}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> ChipSection(
	title: String,
	property: FilterProperty<T>,
	label: @Composable (T) -> String,
	error: String? = null,
	onToggle: (T, Boolean) -> Unit,
	onMore: (() -> Unit)? = null,
) {
	if (property.isEmptyAndSuccess()) return
	Column(modifier = Modifier.fillMaxWidth()) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically,
		) {
			Text(title, style = MaterialTheme.typography.titleSmall)
			if (onMore != null) {
				TextButton(onClick = onMore) {
					Text(stringResource(R.string.more))
				}
			}
		}
		error?.let {
			Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
		}
		if (!property.isEmpty()) {
			FlowRow(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp),
			) {
				property.availableItems.forEach { item ->
					val checked = item in property.selectedItems
					FilterChip(
						selected = checked,
						onClick = { onToggle(item, checked) },
						label = { Text(label(item)) },
					)
				}
			}
		}
	}
}

@Composable
private fun YearSliderSection(
	year: FilterProperty<Int>,
	onChange: (Int) -> Unit,
) {
	if (year.isEmpty()) return
	val from = year.availableItems.first().toFloat()
	val to = year.availableItems.last().toFloat()
	val current = year.selectedItems.singleOrNull() ?: YEAR_UNKNOWN
	Column(modifier = Modifier.fillMaxWidth()) {
		Text(
			text = stringResource(R.string.year) + ": " +
				if (current == YEAR_UNKNOWN) stringResource(R.string.any) else current.toString(),
			style = MaterialTheme.typography.titleSmall,
		)
		Slider(
			value = if (current == YEAR_UNKNOWN) from else current.toFloat().coerceIn(from, to),
			onValueChange = { value ->
				val intValue = value.toInt()
				onChange(if (intValue <= from.toIntUp()) YEAR_UNKNOWN else intValue)
			},
			valueRange = from..to,
		)
	}
}

@Composable
private fun YearRangeSliderSection(
	yearRange: FilterProperty<Int>,
	onChange: (Int, Int) -> Unit,
) {
	if (yearRange.isEmpty()) return
	val from = yearRange.availableItems.first().toFloat()
	val to = yearRange.availableItems.last().toFloat()
	val selectedFrom = yearRange.selectedItems.firstOrNull()?.toFloat() ?: from
	val selectedTo = yearRange.selectedItems.lastOrNull()?.toFloat() ?: to
	Column(modifier = Modifier.fillMaxWidth()) {
		Text(
			text = stringResource(
				R.string.memory_usage_pattern,
				selectedFrom.toInt().toString(),
				selectedTo.toInt().toString(),
			),
			style = MaterialTheme.typography.titleSmall,
		)
		RangeSlider(
			value = selectedFrom.coerceIn(from, to)..selectedTo.coerceIn(from, to),
			onValueChange = { range ->
				onChange(
					if (range.start <= from) YEAR_UNKNOWN else range.start.toInt(),
					if (range.endInclusive >= to) YEAR_UNKNOWN else range.endInclusive.toInt(),
				)
			},
			valueRange = from..to,
		)
	}
}

@Composable
private fun SaveFilterNameDialog(
	initial: String,
	existingNames: Collection<String>,
	onDismiss: () -> Unit,
	onSave: (String) -> Unit,
) {
	var text by remember(initial) { mutableStateOf(initial) }
	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text(stringResource(R.string.save_filter)) },
		text = {
			OutlinedTextField(
				value = text,
				onValueChange = { if (it.length <= MAX_TITLE_LENGTH) text = it },
				singleLine = true,
				label = { Text(stringResource(R.string.enter_name)) },
				modifier = Modifier.fillMaxWidth(),
			)
		},
		confirmButton = {
			TextButton(
				onClick = {
					val trimmed = text.trim()
					if (trimmed.isEmpty()) {
						return@TextButton
					}
					onSave(trimmed)
				},
			) { Text(stringResource(R.string.save)) }
		},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text(stringResource(android.R.string.cancel))
			}
		},
	)
}
