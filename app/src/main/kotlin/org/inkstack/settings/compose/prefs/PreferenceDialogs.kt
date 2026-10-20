package org.inkstack.settings.compose.prefs

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun ListPreferenceItem(
	@StringRes titleRes: Int,
	entries: Array<String>,
	entryValues: Array<String>,
	value: String,
	onValueChange: (String) -> Unit,
	modifier: Modifier = Modifier,
	@DrawableRes iconRes: Int? = null,
	enabled: Boolean = true,
	summaryOverride: String? = null,
) {
	var open by remember { mutableStateOf(false) }
	val index = entryValues.indexOf(value).coerceAtLeast(0)
	val summary = summaryOverride ?: entries.getOrNull(index)
	ListItem(
		headlineContent = { Text(stringResource(titleRes)) },
		supportingContent = { if (summary != null) Text(summary) },
		leadingContent = iconRes?.let {
			{
				Icon(painter = painterResource(it), contentDescription = null)
			}
		},
		modifier = modifier
			.fillMaxWidth()
			.clickable(enabled = enabled) { open = true },
	)
	if (open) {
		AlertDialog(
			onDismissRequest = { open = false },
			title = { Text(stringResource(titleRes)) },
			text = {
				Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
					entries.forEachIndexed { i, entry ->
						val selected = entryValues.getOrNull(i) == value
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.selectable(
									selected = selected,
									onClick = {
										entryValues.getOrNull(i)?.let(onValueChange)
										open = false
									},
									role = Role.RadioButton,
								)
								.padding(vertical = 8.dp),
							verticalAlignment = Alignment.CenterVertically,
						) {
							RadioButton(selected = selected, onClick = null)
							Text(
								text = entry,
								modifier = Modifier.padding(start = 12.dp),
								style = MaterialTheme.typography.bodyLarge,
							)
						}
					}
				}
			},
			confirmButton = {
				TextButton(onClick = { open = false }) {
					Text(stringResource(android.R.string.cancel))
				}
			},
		)
	}
}

@Composable
fun MultiSelectPreferenceItem(
	@StringRes titleRes: Int,
	entries: Array<String>,
	entryValues: Array<String>,
	values: Set<String>,
	onValuesChange: (Set<String>) -> Unit,
	modifier: Modifier = Modifier,
	@DrawableRes iconRes: Int? = null,
	@StringRes emptySummaryRes: Int,
	enabled: Boolean = true,
) {
	var open by remember { mutableStateOf(false) }
	var draft by remember(values) { mutableStateOf(values) }
	val summary = if (values.isEmpty()) {
		stringResource(emptySummaryRes)
	} else {
		entryValues.mapIndexedNotNull { i, v -> if (v in values) entries.getOrNull(i) else null }
			.joinToString()
	}
	ListItem(
		headlineContent = { Text(stringResource(titleRes)) },
		supportingContent = { Text(summary) },
		leadingContent = iconRes?.let {
			{
				Icon(painter = painterResource(it), contentDescription = null)
			}
		},
		modifier = modifier
			.fillMaxWidth()
			.clickable(enabled = enabled) {
				draft = values
				open = true
			},
	)
	if (open) {
		AlertDialog(
			onDismissRequest = { open = false },
			title = { Text(stringResource(titleRes)) },
			text = {
				Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
					entries.forEachIndexed { i, entry ->
						val key = entryValues.getOrNull(i) ?: return@forEachIndexed
						val checked = key in draft
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.toggleable(
									value = checked,
									onValueChange = {
										draft = if (it) draft + key else draft - key
									},
									role = Role.Checkbox,
								)
								.padding(vertical = 8.dp),
							verticalAlignment = Alignment.CenterVertically,
						) {
							Checkbox(checked = checked, onCheckedChange = null)
							Text(
								text = entry,
								modifier = Modifier.padding(start = 12.dp),
								style = MaterialTheme.typography.bodyLarge,
							)
						}
					}
				}
			},
			confirmButton = {
				TextButton(
					onClick = {
						onValuesChange(draft)
						open = false
					},
				) {
					Text(stringResource(android.R.string.ok))
				}
			},
			dismissButton = {
				TextButton(onClick = { open = false }) {
					Text(stringResource(android.R.string.cancel))
				}
			},
		)
	}
}

@Composable
fun EditTextPreferenceItem(
	@StringRes titleRes: Int,
	value: String,
	onValueChange: (String) -> Unit,
	modifier: Modifier = Modifier,
	@DrawableRes iconRes: Int? = null,
	summary: String? = null,
	@StringRes summaryRes: Int? = null,
	dialogMessage: String? = null,
	enabled: Boolean = true,
	singleLine: Boolean = true,
	neutralLabelRes: Int? = null,
	onNeutral: (() -> Unit)? = null,
) {
	var open by remember { mutableStateOf(false) }
	var draft by remember { mutableStateOf(value) }
	val supporting = summary ?: summaryRes?.let { stringResource(it) } ?: value.ifEmpty { null }
	ListItem(
		headlineContent = { Text(stringResource(titleRes)) },
		supportingContent = { if (supporting != null) Text(supporting) },
		leadingContent = iconRes?.let {
			{
				Icon(painter = painterResource(it), contentDescription = null)
			}
		},
		modifier = modifier
			.fillMaxWidth()
			.clickable(enabled = enabled) {
				draft = value
				open = true
			},
	)
	if (open) {
		AlertDialog(
			onDismissRequest = { open = false },
			title = { Text(stringResource(titleRes)) },
			text = {
				Column {
					if (dialogMessage != null) {
						Text(
							text = dialogMessage,
							modifier = Modifier.padding(bottom = 12.dp),
						)
					}
					OutlinedTextField(
						value = draft,
						onValueChange = { draft = it },
						modifier = Modifier.fillMaxWidth(),
						singleLine = singleLine,
					)
				}
			},
			confirmButton = {
				TextButton(
					onClick = {
						onValueChange(draft)
						open = false
					},
				) {
					Text(stringResource(android.R.string.ok))
				}
			},
			dismissButton = {
				Row {
					if (neutralLabelRes != null && onNeutral != null) {
						TextButton(
							onClick = {
								open = false
								onNeutral()
							},
						) {
							Text(stringResource(neutralLabelRes))
						}
					}
					TextButton(onClick = { open = false }) {
						Text(stringResource(android.R.string.cancel))
					}
				}
			},
		)
	}
}

@Composable
fun SliderPreferenceItem(
	@StringRes titleRes: Int,
	value: Int,
	valueFrom: Int,
	valueTo: Int,
	step: Int,
	onValueChange: (Int) -> Unit,
	modifier: Modifier = Modifier,
	@DrawableRes iconRes: Int? = null,
	summary: String? = null,
	enabled: Boolean = true,
) {
	var slider by remember(value) { mutableFloatStateOf(value.toFloat()) }
	ListItem(
		headlineContent = { Text(stringResource(titleRes)) },
		supportingContent = {
			Column {
				if (summary != null) Text(summary)
				Slider(
					value = slider,
					onValueChange = { slider = it },
					onValueChangeFinished = {
						val stepped = ((slider - valueFrom) / step.toFloat()).roundToInt() * step + valueFrom
						onValueChange(stepped.coerceIn(valueFrom, valueTo))
					},
					valueRange = valueFrom.toFloat()..valueTo.toFloat(),
					steps = ((valueTo - valueFrom) / step) - 1,
					enabled = enabled,
					modifier = Modifier.padding(top = 4.dp),
				)
			}
		},
		leadingContent = iconRes?.let {
			{
				Icon(painter = painterResource(it), contentDescription = null)
			}
		},
		modifier = modifier.fillMaxWidth(),
	)
}
