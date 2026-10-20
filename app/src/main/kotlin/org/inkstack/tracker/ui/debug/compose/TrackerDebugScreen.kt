package org.inkstack.tracker.ui.debug.compose

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import org.inkstack.R
import org.inkstack.core.ui.compose.InkstackScaffold
import org.inkstack.parsers.model.Manga
import org.inkstack.tracker.data.TrackEntity
import org.inkstack.tracker.ui.debug.TrackerDebugViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerDebugScreen(
	onBack: () -> Unit,
	onOpenManga: (Manga) -> Unit,
	modifier: Modifier = Modifier,
	viewModel: TrackerDebugViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val content by viewModel.content.collectAsState()

	InkstackScaffold(
		modifier = modifier,
		topBar = {
			TopAppBar(
				title = { Text(stringResource(R.string.tracker_debug_info)) },
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
		LazyColumn(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding),
		) {
			items(content, key = { it.manga.id }) { item ->
				val summary = buildString {
					append(
						item.lastCheckTime?.let {
							DateUtils.getRelativeDateTimeString(
								context,
								it.toEpochMilli(),
								DateUtils.MINUTE_IN_MILLIS,
								DateUtils.WEEK_IN_MILLIS,
								0,
							)
						} ?: context.getString(R.string.never),
					)
					if (item.lastResult == TrackEntity.RESULT_FAILED) {
						append(" - ")
						append(item.lastError ?: context.getString(R.string.error))
					}
					if (item.newChapters > 0) {
						append(" (+${item.newChapters})")
					}
				}
				ListItem(
					headlineContent = { Text(item.manga.title) },
					supportingContent = { Text(summary) },
					leadingContent = {
						AsyncImage(
							model = item.manga.coverUrl,
							contentDescription = null,
							modifier = Modifier.size(48.dp),
							contentScale = ContentScale.Crop,
						)
					},
					modifier = Modifier
						.fillMaxWidth()
						.clickable { onOpenManga(item.manga) },
				)
			}
		}
	}
}
