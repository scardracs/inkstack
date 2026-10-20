package org.inkstack.alternatives.ui.covers.compose

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import org.inkstack.R
import org.inkstack.alternatives.ui.covers.AlternativeCoverModel
import org.inkstack.alternatives.ui.covers.AlternativeCoversViewModel
import org.inkstack.core.model.getTitle
import org.inkstack.core.ui.compose.InkstackScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlternativeCoversScreen(
	onBack: () -> Unit,
	onApply: (String) -> Unit,
	modifier: Modifier = Modifier,
	viewModel: AlternativeCoversViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val covers by viewModel.covers.collectAsState()
	val selectedUrl by viewModel.selectedUrl.collectAsState()
	val progress by viewModel.progress.collectAsState()
	val isRunning by viewModel.isRunning.collectAsState()
	val selected = covers.firstOrNull { it.coverUrl == selectedUrl }
	val showEmpty = covers.none { !it.isCurrent } && !isRunning
	val canApply = selected != null && !selected.isCurrent

	InkstackScaffold(
		modifier = modifier,
		topBar = {
			TopAppBar(
				title = {
					Column {
						Text(stringResource(R.string.alternative_covers))
						if (isRunning && progress.second > 0) {
							Text(
								stringResource(
									R.string.alternative_covers_progress,
									progress.first,
									progress.second,
								),
								style = MaterialTheme.typography.bodySmall,
							)
						}
					}
				},
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
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
				.padding(padding),
		) {
			if (isRunning) {
				LinearProgressIndicator(
					progress = {
						if (progress.second > 0) progress.first.toFloat() / progress.second else 0f
					},
					modifier = Modifier.fillMaxWidth(),
				)
			}
			AsyncImage(
				model = selectedUrl,
				contentDescription = null,
				modifier = Modifier
					.fillMaxWidth()
					.height(220.dp)
					.padding(16.dp),
				contentScale = ContentScale.Fit,
			)
			Text(
				text = when {
					selected == null -> ""
					selected.isCurrent -> stringResource(R.string.current_cover)
					else -> selected.source.getTitle(context)
				},
				modifier = Modifier
					.padding(horizontal = 16.dp)
					.align(Alignment.CenterHorizontally),
			)
			if (showEmpty) {
				Text(
					stringResource(R.string.nothing_found),
					modifier = Modifier
						.padding(32.dp)
						.align(Alignment.CenterHorizontally),
				)
			} else {
				LazyVerticalGrid(
					columns = GridCells.Adaptive(96.dp),
					modifier = Modifier.weight(1f),
					contentPadding = PaddingValues(12.dp),
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					verticalArrangement = Arrangement.spacedBy(8.dp),
				) {
					items(covers, key = { it.coverUrl }) { cover ->
						CoverCell(
							cover = cover,
							selected = cover.coverUrl == selectedUrl,
							onClick = { viewModel.select(cover.coverUrl) },
						)
					}
				}
			}
			Button(
				onClick = {
					val url = selectedUrl ?: return@Button
					viewModel.cancelSearch()
					onApply(url)
				},
				enabled = canApply,
				modifier = Modifier
					.fillMaxWidth()
					.padding(16.dp),
			) {
				Text(stringResource(R.string.apply))
			}
		}
	}
}

@Composable
private fun CoverCell(
	cover: AlternativeCoverModel,
	selected: Boolean,
	onClick: () -> Unit,
) {
	AsyncImage(
		model = cover.coverUrl,
		contentDescription = null,
		modifier = Modifier
			.aspectRatio(2f / 3f)
			.then(
				if (selected) {
					Modifier.border(2.dp, MaterialTheme.colorScheme.primary)
				} else {
					Modifier
				},
			)
			.clickable(onClick = onClick),
		contentScale = ContentScale.Crop,
	)
}
