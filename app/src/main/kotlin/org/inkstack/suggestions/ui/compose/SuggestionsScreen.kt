package org.inkstack.suggestions.ui.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.ImageLoader
import org.inkstack.R
import org.inkstack.list.ui.compose.MangaListPane
import org.inkstack.list.ui.compose.rememberMangaListSelection
import org.inkstack.parsers.model.Manga
import org.inkstack.suggestions.ui.SuggestionsViewModel

@Composable
fun SuggestionsScreen(
	onMangaClick: (Manga) -> Unit,
	modifier: Modifier = Modifier,
	viewModel: SuggestionsViewModel = hiltViewModel(),
	imageLoader: ImageLoader? = null,
	contentPadding: PaddingValues = PaddingValues(),
) {
	val content by viewModel.content.collectAsState()
	val isLoading by viewModel.isLoading.collectAsState()
	val selection = rememberMangaListSelection(
		content = content,
		menuRes = R.menu.mode_remote,
	)
	MangaListPane(
		content = content,
		onMangaClick = onMangaClick,
		selection = selection,
		modifier = modifier,
		quickFilterListener = viewModel,
		onRefresh = viewModel::updateSuggestions,
		isRefreshing = isLoading,
		imageLoader = imageLoader,
		contentPadding = contentPadding,
	)
}
