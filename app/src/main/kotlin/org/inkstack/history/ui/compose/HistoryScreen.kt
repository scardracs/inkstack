package org.inkstack.history.ui.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.ImageLoader
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.inkstack.R
import org.inkstack.history.ui.HistoryListViewModel
import org.inkstack.list.ui.compose.MangaListPane
import org.inkstack.list.ui.compose.rememberMangaListSelection
import org.inkstack.parsers.model.Manga

@Composable
fun HistoryScreen(
	onMangaClick: (Manga) -> Unit,
	modifier: Modifier = Modifier,
	viewModel: HistoryListViewModel = hiltViewModel(),
	imageLoader: ImageLoader? = null,
	contentPadding: PaddingValues = PaddingValues(),
) {
	val context = LocalContext.current
	val content by viewModel.content.collectAsState()
	val selection = rememberMangaListSelection(
		content = content,
		menuRes = R.menu.mode_history,
	) { itemId, ids, manga, finish ->
		when (itemId) {
			R.id.action_remove -> {
				viewModel.removeFromHistory(ids)
				finish()
				true
			}
			R.id.action_mark_current -> {
				MaterialAlertDialogBuilder(context)
					.setTitle(R.string.mark_as_completed)
					.setMessage(R.string.mark_as_completed_prompt)
					.setNegativeButton(android.R.string.cancel, null)
					.setPositiveButton(android.R.string.ok) { _, _ ->
						viewModel.markAsRead(manga)
						finish()
					}
					.show()
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
		onEmptyAction = viewModel::clearFilter,
		onLoadMore = viewModel::requestMoreItems,
		imageLoader = imageLoader,
		contentPadding = contentPadding,
	)
}
