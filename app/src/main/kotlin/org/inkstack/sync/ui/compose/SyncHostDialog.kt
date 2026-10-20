package org.inkstack.sync.ui.compose

import android.webkit.URLUtil
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.inkstack.R
import org.inkstack.settings.utils.validation.DomainValidator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncHostDialog(
	initialUrl: String,
	onDismissRequest: () -> Unit,
	onConfirm: (String) -> Unit,
) {
	val suggestions = stringArrayResource(R.array.sync_url_list)
	var text by remember(initialUrl) { mutableStateOf(initialUrl) }
	var expanded by remember { mutableStateOf(false) }
	val trimmed = text.trim()
	val isValid = trimmed.isEmpty() ||
		URLUtil.isValidUrl(trimmed) ||
		DomainValidator.isValidDomain(trimmed)

	AlertDialog(
		onDismissRequest = onDismissRequest,
		title = { Text(stringResource(R.string.server_address)) },
		text = {
			Column(modifier = Modifier.fillMaxWidth()) {
				Text(
					text = stringResource(R.string.sync_host_description),
					modifier = Modifier.padding(bottom = 12.dp),
				)
				ExposedDropdownMenuBox(
					expanded = expanded,
					onExpandedChange = { expanded = it },
				) {
					OutlinedTextField(
						value = text,
						onValueChange = { text = it },
						modifier = Modifier
							.fillMaxWidth()
							.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
						singleLine = true,
						isError = trimmed.isNotEmpty() && !isValid,
						supportingText = if (trimmed.isNotEmpty() && !isValid) {
							{ Text(stringResource(R.string.invalid_server_address_message)) }
						} else {
							null
						},
						trailingIcon = {
							ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
						},
					)
					ExposedDropdownMenu(
						expanded = expanded,
						onDismissRequest = { expanded = false },
					) {
						suggestions.forEach { suggestion ->
							DropdownMenuItem(
								text = { Text(suggestion) },
								onClick = {
									text = suggestion
									expanded = false
								},
							)
						}
					}
				}
			}
		},
		confirmButton = {
			TextButton(
				onClick = {
					if (isValid && trimmed.isNotEmpty()) {
						onConfirm(trimmed)
					}
				},
				enabled = trimmed.isNotEmpty() && isValid,
			) {
				Text(stringResource(android.R.string.ok))
			}
		},
		dismissButton = {
			TextButton(onClick = onDismissRequest) {
				Text(stringResource(android.R.string.cancel))
			}
		},
	)
}
