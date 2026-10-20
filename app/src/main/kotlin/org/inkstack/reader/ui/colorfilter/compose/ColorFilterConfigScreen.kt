package org.inkstack.reader.ui.colorfilter.compose

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest
import org.inkstack.R
import org.inkstack.core.ui.compose.InkstackScaffold
import org.inkstack.core.util.ext.call
import org.inkstack.reader.ui.colorfilter.ColorFilterConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorFilterConfigScreen(
	onBack: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: ColorFilterConfigViewModel = hiltViewModel(),
) {
	val colorFilter by viewModel.colorFilter.collectAsState()
	val isLoading by viewModel.isLoading.collectAsState()
	var confirmSave by remember { mutableStateOf(false) }
	var confirmDiscard by remember { mutableStateOf(false) }

	LaunchedEffect(viewModel) {
		viewModel.onDismiss.collectLatest { event ->
			event?.consume { onBack() }
		}
	}

	BackHandler {
		if (viewModel.isChanged) confirmDiscard = true else viewModel.onDismiss.call(Unit)
	}

	val composeFilter = remember(colorFilter) {
		colorFilter?.let {
			ColorFilter.colorMatrix(ColorMatrix(it.toColorMatrix().array.copyOf()))
		}
	}

	InkstackScaffold(
		modifier = modifier,
		topBar = {
			TopAppBar(
				title = { Text(stringResource(R.string.color_correction)) },
				navigationIcon = {
					IconButton(onClick = {
						if (viewModel.isChanged) confirmDiscard = true else onBack()
					}) {
						Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
					}
				},
				actions = {
					TextButton(onClick = viewModel::reset, enabled = !isLoading) {
						Text(stringResource(R.string.reset))
					}
					Button(
						onClick = { confirmSave = true },
						enabled = !isLoading,
						modifier = Modifier.padding(end = 8.dp),
					) { Text(stringResource(R.string.apply)) }
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
			verticalArrangement = Arrangement.spacedBy(12.dp),
		) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.height(180.dp),
				horizontalArrangement = Arrangement.spacedBy(8.dp),
			) {
				AsyncImage(
					model = viewModel.preview.url,
					contentDescription = null,
					modifier = Modifier.weight(1f),
					contentScale = ContentScale.Fit,
				)
				AsyncImage(
					model = viewModel.preview.url,
					contentDescription = null,
					modifier = Modifier.weight(1f),
					contentScale = ContentScale.Fit,
					colorFilter = composeFilter,
				)
			}
			Text(stringResource(R.string.brightness))
			Slider(
				value = colorFilter?.brightness ?: 0f,
				onValueChange = viewModel::setBrightness,
				valueRange = -1f..1f,
				enabled = !isLoading,
			)
			Text(stringResource(R.string.contrast))
			Slider(
				value = colorFilter?.contrast ?: 0f,
				onValueChange = viewModel::setContrast,
				valueRange = -1f..1f,
				enabled = !isLoading,
			)
			SwitchRow(
				title = stringResource(R.string.invert_colors),
				checked = colorFilter?.isInverted == true,
				enabled = !isLoading,
				onCheckedChange = viewModel::setInversion,
			)
			SwitchRow(
				title = stringResource(R.string.grayscale),
				checked = colorFilter?.isGrayscale == true,
				enabled = !isLoading,
				onCheckedChange = viewModel::setGrayscale,
			)
			SwitchRow(
				title = stringResource(R.string.book_effect),
				checked = colorFilter?.isBookBackground == true,
				enabled = !isLoading,
				onCheckedChange = viewModel::setBookEffect,
			)
		}
	}

	if (confirmSave) {
		AlertDialog(
			onDismissRequest = { confirmSave = false },
			title = { Text(stringResource(R.string.apply)) },
			text = { Text(stringResource(R.string.color_correction_apply_text)) },
			confirmButton = {
				Row {
					TextButton(onClick = {
						viewModel.saveGlobally()
						confirmSave = false
					}) { Text(stringResource(R.string.globally)) }
					TextButton(onClick = {
						viewModel.save()
						confirmSave = false
					}) { Text(stringResource(R.string.this_manga)) }
				}
			},
			dismissButton = {
				TextButton(onClick = { confirmSave = false }) {
					Text(stringResource(android.R.string.cancel))
				}
			},
		)
	}

	if (confirmDiscard) {
		AlertDialog(
			onDismissRequest = { confirmDiscard = false },
			title = { Text(stringResource(R.string.color_correction)) },
			text = { Text(stringResource(R.string.text_unsaved_changes_prompt)) },
			confirmButton = {
				TextButton(onClick = {
					confirmDiscard = false
					confirmSave = true
				}) { Text(stringResource(R.string.save)) }
			},
			dismissButton = {
				TextButton(onClick = {
					confirmDiscard = false
					viewModel.onDismiss.call(Unit)
				}) { Text(stringResource(R.string.discard)) }
			},
		)
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
		Text(title)
		Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
	}
}
