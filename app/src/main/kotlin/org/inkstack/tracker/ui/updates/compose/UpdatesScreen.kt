package org.inkstack.tracker.ui.updates.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.ImageLoader
import org.inkstack.R
import org.inkstack.list.ui.compose.MangaListPane
import org.inkstack.list.ui.compose.rememberMangaListSelection
import org.inkstack.parsers.model.Manga
import org.inkstack.tracker.ui.updates.UpdatesViewModel

@Composable
fun UpdatesScreen(
	onMangaClick: (Manga) -> Unit,
	modifier: Modifier = Modifier,
	viewModel: UpdatesViewModel = hiltViewModel(),
	imageLoader: ImageLoader? = null,
	contentPadding: PaddingValues = PaddingValues(),
) {
	val content by viewModel.content.collectAsState()
	val isLoading by viewModel.isLoading.collectAsState()
	val selection = rememberMangaListSelection(
		content = content,
		menuRes = R.menu.mode_updates,
	) { itemId, ids, _, finish ->
		when (itemId) {
			R.id.action_remove -> {
				viewModel.remove(ids)
				finish()
				true
			}
			else -> false
		}
	}
	MangaListPane(
		content = content,
		onMangaClick = onMangaClick,
		selection = selection,
		modifier = modifier,
		quickFilterListener = viewModel,
		onRefresh = viewModel::onRefresh,
		isRefreshing = isLoading,
		imageLoader = imageLoader,
		contentPadding = contentPadding,
	)
}
