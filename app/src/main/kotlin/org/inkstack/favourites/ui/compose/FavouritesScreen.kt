package org.inkstack.favourites.ui.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.ImageLoader
import org.inkstack.R
import org.inkstack.favourites.ui.list.FavouritesListViewModel
import org.inkstack.list.ui.compose.MangaListPane
import org.inkstack.list.ui.compose.confirmMarkAsCompleted
import org.inkstack.list.ui.compose.rememberMangaListSelection
import org.inkstack.parsers.model.Manga

@Composable
fun FavouritesScreen(
	onMangaClick: (Manga) -> Unit,
	modifier: Modifier = Modifier,
	viewModel: FavouritesListViewModel = hiltViewModel(),
	imageLoader: ImageLoader? = null,
	contentPadding: PaddingValues = PaddingValues(),
) {
	val context = LocalContext.current
	val content by viewModel.content.collectAsState()
	val isLoading by viewModel.isLoading.collectAsState()
	val selection = rememberMangaListSelection(
		content = content,
		menuRes = R.menu.mode_favourites,
	) { itemId, ids, manga, finish ->
		when (itemId) {
			R.id.action_pin -> {
				viewModel.setPinned(ids, isPinned = true)
				finish()
				true
			}
			R.id.action_unpin -> {
				viewModel.setPinned(ids, isPinned = false)
				finish()
				true
			}
			R.id.action_remove -> {
				viewModel.removeFromFavourites(ids)
				finish()
				true
			}
			R.id.action_mark_current -> confirmMarkAsCompleted(
				context = context,
				manga = manga,
				finish = finish,
				onConfirm = viewModel::markAsRead,
			)
			else -> false
		}
	}
	MangaListPane(
		content = content,
		onMangaClick = onMangaClick,
		selection = selection,
		modifier = modifier,
		quickFilterListener = viewModel,
		onEmptyAction = viewModel::clearFilter,
		onLoadMore = viewModel::requestMoreItems,
		onRefresh = viewModel::onRefresh,
		isRefreshing = isLoading,
		imageLoader = imageLoader,
		contentPadding = contentPadding,
	)
}
