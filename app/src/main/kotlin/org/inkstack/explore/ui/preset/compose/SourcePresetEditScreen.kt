package org.inkstack.explore.ui.preset.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import org.inkstack.R
import org.inkstack.core.ui.compose.InkstackScaffold
import org.inkstack.core.util.ext.getDisplayMessage
import org.inkstack.core.util.ext.getDisplayName
import org.inkstack.core.util.ext.toLocale
import org.inkstack.explore.ui.preset.SourcePresetEditViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SourcePresetEditScreen(
	onBack: () -> Unit,
	onSaved: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: SourcePresetEditViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val preset by viewModel.preset.collectAsState()
	val isLoading by viewModel.isLoading.collectAsState()
	val locales = remember {
		viewModel.allLocales.sortedBy { it.toLocale().getDisplayName(context) }
	}

	var name by remember { mutableStateOf("") }
	var selected by remember { mutableStateOf(setOf<String>()) }
	var error by remember { mutableStateOf<String?>(null) }
	var initialized by remember { mutableStateOf(false) }

	LaunchedEffect(preset) {
		if (!initialized) {
			if (preset != null) {
				name = preset!!.title
				selected = preset!!.languages
				initialized = true
			} else if (!isLoading) {
				initialized = true
			}
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

	val titleRes = if (preset == null) R.string.create_preset else R.string.edit_preset
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
							viewModel.save(name.trim(), selected)
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
				label = { Text(stringResource(R.string.preset_name)) },
				enabled = !isLoading,
				singleLine = true,
			)
			Text(
				text = stringResource(R.string.preset_languages),
				style = MaterialTheme.typography.titleMedium,
			)
			FlowRow(
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp),
			) {
				locales.forEach { locale ->
					val checked = locale in selected
					FilterChip(
						selected = checked,
						onClick = {
							selected = if (checked) selected - locale else selected + locale
						},
						label = { Text(locale.toLocale().getDisplayName(context)) },
						enabled = !isLoading,
					)
				}
			}
			error?.let {
				Text(text = it, color = MaterialTheme.colorScheme.error)
			}
		}
	}
}
