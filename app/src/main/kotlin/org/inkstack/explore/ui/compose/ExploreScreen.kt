package org.inkstack.explore.ui.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.ImageLoader
import kotlinx.coroutines.flow.collectLatest
import org.inkstack.R
import org.inkstack.explore.ui.ExploreViewModel
import org.inkstack.list.ui.compose.MangaListPane
import org.inkstack.list.ui.compose.rememberMangaListSelection
import org.inkstack.parsers.model.Manga
import org.inkstack.parsers.model.MangaSource

@Composable
fun ExploreScreen(
	onMangaClick: (Manga) -> Unit,
	onSourceClick: (MangaSource) -> Unit,
	modifier: Modifier = Modifier,
	viewModel: ExploreViewModel = hiltViewModel(),
	imageLoader: ImageLoader? = null,
	contentPadding: PaddingValues = PaddingValues(),
) {
	val content by viewModel.content.collectAsState()
	val selection = rememberMangaListSelection(
		content = content,
		menuRes = R.menu.mode_remote,
	)
	LaunchedEffect(viewModel) {
		viewModel.onOpenManga.collectLatest { event ->
			event?.consume { onMangaClick(it) }
		}
	}
	MangaListPane(
		content = content,
		onMangaClick = onMangaClick,
		selection = selection,
		modifier = modifier,
		onSourceClick = onSourceClick,
		imageLoader = imageLoader,
		contentPadding = contentPadding,
	)
}
