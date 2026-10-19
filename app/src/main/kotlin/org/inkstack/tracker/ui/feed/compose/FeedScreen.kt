package org.inkstack.tracker.ui.feed.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.ImageLoader
import org.inkstack.list.ui.compose.MangaListPane
import org.inkstack.parsers.model.Manga
import org.inkstack.tracker.ui.feed.FeedViewModel

@Composable
fun FeedScreen(
	onMangaClick: (Manga) -> Unit,
	modifier: Modifier = Modifier,
	viewModel: FeedViewModel = hiltViewModel(),
	imageLoader: ImageLoader? = null,
	contentPadding: PaddingValues = PaddingValues(),
) {
	val content by viewModel.content.collectAsState()
	val isRunning by viewModel.isRunning.collectAsState()
	MangaListPane(
		content = content,
		onMangaClick = onMangaClick,
		modifier = modifier,
		quickFilterListener = viewModel,
		onLoadMore = viewModel::requestMoreItems,
		onRefresh = viewModel::update,
		isRefreshing = isRunning,
		imageLoader = imageLoader,
		contentPadding = contentPadding,
	)
}
