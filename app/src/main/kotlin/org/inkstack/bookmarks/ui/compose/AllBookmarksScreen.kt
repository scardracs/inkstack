package org.inkstack.bookmarks.ui.compose

import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import org.inkstack.R
import org.inkstack.bookmarks.domain.Bookmark
import org.inkstack.bookmarks.ui.AllBookmarksViewModel
import org.inkstack.core.nav.ReaderIntent
import org.inkstack.core.nav.router
import org.inkstack.core.ui.compose.fastscroll.FastScrollBubbleSize
import org.inkstack.core.ui.compose.fastscroll.FastScroller
import org.inkstack.core.ui.util.reverseAsync
import org.inkstack.core.util.ext.findActivity
import org.inkstack.list.domain.ReadingProgress
import org.inkstack.list.ui.compose.ReadingProgressBadge
import org.inkstack.list.ui.model.EmptyState
import org.inkstack.list.ui.model.ErrorState
import org.inkstack.list.ui.model.ListHeader
import org.inkstack.list.ui.model.ListModel
import org.inkstack.list.ui.model.LoadingState
import org.inkstack.parsers.model.Manga
import org.inkstack.reader.ui.PageSaveHelper

private val PageThumbAspect = 13f / 18f

@Composable
fun AllBookmarksScreen(
	pageSaveHelper: PageSaveHelper,
	modifier: Modifier = Modifier,
	viewModel: AllBookmarksViewModel = hiltViewModel(),
	imageLoader: ImageLoader? = null,
	contentPadding: PaddingValues = PaddingValues(),
	snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
	val context = LocalContext.current
	val content by viewModel.content.collectAsState()
	val selection = rememberBookmarkSelection(content) { itemId, ids, finish ->
		when (itemId) {
			R.id.action_remove -> {
				viewModel.removeBookmarks(ids)
				finish()
				true
			}
			R.id.action_save -> {
				viewModel.savePages(pageSaveHelper, ids)
				finish()
				true
			}
			else -> false
		}
	}

	LaunchedEffect(viewModel) {
		viewModel.onError.collect { event ->
			event?.consume { error ->
				snackbarHostState.showSnackbar(
					error.localizedMessage ?: error.javaClass.simpleName,
				)
			}
		}
	}
	LaunchedEffect(viewModel) {
		viewModel.onActionDone.collect { event ->
			event?.consume { action ->
				val result = snackbarHostState.showSnackbar(
					message = context.getString(action.stringResId),
					actionLabel = if (action.handle != null) context.getString(R.string.undo) else null,
					duration = if (action.handle != null) SnackbarDuration.Long else SnackbarDuration.Short,
				)
				if (result == SnackbarResult.ActionPerformed) {
					action.handle?.reverseAsync()
				}
			}
		}
	}

	val gridState = rememberLazyGridState()
	val sectionIndexer: (Int) -> String? = remember(content) {
		{ position ->
			content.take(position + 1).lastOrNull { it is ListHeader }
				.let { (it as? ListHeader)?.getText(context)?.toString() }
		}
	}

	Box(modifier = modifier.fillMaxSize()) {
		LazyVerticalGrid(
			columns = GridCells.Adaptive(minSize = 120.dp),
			state = gridState,
			modifier = Modifier.fillMaxSize(),
			contentPadding = contentPadding,
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp),
		) {
			itemsIndexed(
				items = content,
				key = { index, item -> bookmarkListKey(item, index) },
				span = { _, item ->
					if (item is Bookmark) GridItemSpan(1) else GridItemSpan(maxLineSpan)
				},
			) { _, item ->
				when (item) {
					is ListHeader -> BookmarkHeader(
						item = item,
						onMoreClick = {
							val manga = item.payload as? Manga ?: return@BookmarkHeader
							(context.findActivity() as? FragmentActivity)?.router?.openDetails(manga)
						},
					)
					is Bookmark -> BookmarkThumb(
						bookmark = item,
						selected = selection.selectedIds.contains(item.pageId),
						imageLoader = imageLoader,
						onClick = {
							selection.onItemClick(item.pageId) {
								val intent = ReaderIntent.Builder(context)
									.bookmark(item)
									.incognito()
									.build()
								(context.findActivity() as? FragmentActivity)?.router?.openReader(intent)
								Toast.makeText(context, R.string.incognito_mode, Toast.LENGTH_SHORT).show()
							}
						},
						onLongClick = { selection.onItemLongClick(item.pageId) },
					)
					is EmptyState -> EmptyPane(item)
					is ErrorState -> ErrorPane(item)
					is LoadingState -> Box(
						modifier = Modifier
							.fillMaxWidth()
							.padding(48.dp),
						contentAlignment = Alignment.Center,
					) {
						CircularProgressIndicator()
					}
					else -> Unit
				}
			}
		}
		if (content.size > 8) {
			FastScroller(
				state = gridState,
				itemCount = content.size,
				sectionIndexer = sectionIndexer,
				bubbleSize = FastScrollBubbleSize.SMALL,
			)
		}
	}
}

@Composable
private fun BookmarkHeader(
	item: ListHeader,
	onMoreClick: () -> Unit,
) {
	val context = LocalContext.current
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 8.dp, vertical = 4.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.SpaceBetween,
	) {
		Text(
			text = item.getText(context)?.toString().orEmpty(),
			style = MaterialTheme.typography.titleMedium,
			modifier = Modifier.weight(1f),
		)
		if (item.buttonTextRes != 0) {
			TextButton(onClick = onMoreClick) {
				Text(stringResource(item.buttonTextRes))
			}
		}
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BookmarkThumb(
	bookmark: Bookmark,
	selected: Boolean,
	imageLoader: ImageLoader?,
	onClick: () -> Unit,
	onLongClick: () -> Unit,
) {
	val context = LocalContext.current
	val shape = RoundedCornerShape(8.dp)
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.aspectRatio(PageThumbAspect)
			.clip(shape)
			.then(
				if (selected) {
					Modifier.border(3.dp, MaterialTheme.colorScheme.primary, shape)
				} else {
					Modifier
				},
			)
			.background(MaterialTheme.colorScheme.surfaceContainerHighest)
			.combinedClickable(onClick = onClick, onLongClick = onLongClick),
	) {
		val request = ImageRequest.Builder(context).data(bookmark.imageUrl).build()
		if (imageLoader != null) {
			AsyncImage(
				model = request,
				contentDescription = null,
				imageLoader = imageLoader,
				modifier = Modifier.fillMaxSize(),
				contentScale = ContentScale.Crop,
				alignment = Alignment.TopCenter,
			)
		} else {
			AsyncImage(
				model = request,
				contentDescription = null,
				modifier = Modifier.fillMaxSize(),
				contentScale = ContentScale.Crop,
				alignment = Alignment.TopCenter,
			)
		}
		ReadingProgressBadge(
			progress = ReadingProgress(
				percent = bookmark.percent,
				totalChapters = 0,
				mode = org.inkstack.core.prefs.ProgressIndicatorMode.PERCENT_READ,
			).takeIf { ReadingProgress.isValid(bookmark.percent) },
			modifier = Modifier
				.align(Alignment.BottomEnd)
				.padding(dimensionResource(R.dimen.card_indicator_offset)),
		)
	}
}

@Composable
private fun EmptyPane(state: EmptyState) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(32.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(8.dp),
	) {
		Text(
			text = stringResource(state.textPrimary),
			style = MaterialTheme.typography.titleMedium,
			textAlign = TextAlign.Center,
		)
		if (state.textSecondary != 0) {
			Text(
				text = stringResource(state.textSecondary),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				textAlign = TextAlign.Center,
			)
		}
	}
}

@Composable
private fun ErrorPane(state: ErrorState) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(32.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		Text(
			text = state.exception.localizedMessage ?: state.exception.javaClass.simpleName,
			style = MaterialTheme.typography.bodyMedium,
			textAlign = TextAlign.Center,
		)
	}
}

@Stable
private class BookmarkSelectionState(
	private val activity: AppCompatActivity?,
	private val contentProvider: () -> List<ListModel>,
	private val onAction: (itemId: Int, ids: Set<Long>, finish: () -> Unit) -> Boolean,
) {
	var selectedIds by mutableStateOf(emptySet<Long>())
		private set

	private var actionMode: ActionMode? = null

	fun onItemClick(id: Long, open: () -> Unit) {
		if (selectedIds.isEmpty()) {
			open()
		} else {
			toggle(id)
		}
	}

	fun onItemLongClick(id: Long) {
		if (selectedIds.isEmpty()) {
			selectedIds = setOf(id)
			actionMode = activity?.startSupportActionMode(callback)
			if (actionMode == null) {
				selectedIds = emptySet()
			}
		} else {
			toggle(id)
		}
	}

	fun clear() {
		actionMode?.finish()
		selectedIds = emptySet()
		actionMode = null
	}

	private fun toggle(id: Long) {
		val next = if (id in selectedIds) selectedIds - id else selectedIds + id
		selectedIds = next
		if (next.isEmpty()) {
			actionMode?.finish()
			actionMode = null
		} else {
			actionMode?.invalidate()
		}
	}

	private fun selectAll() {
		selectedIds = contentProvider().mapNotNull { (it as? Bookmark)?.pageId }.toSet()
		actionMode?.invalidate()
	}

	private val callback = object : ActionMode.Callback {
		override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
			mode.menuInflater.inflate(R.menu.mode_bookmarks, menu)
			return true
		}

		override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
			mode.title = selectedIds.size.toString()
			return true
		}

		override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
			if (item.itemId == R.id.action_select_all) {
				selectAll()
				return true
			}
			return onAction(item.itemId, selectedIds) { mode.finish() }
		}

		override fun onDestroyActionMode(mode: ActionMode) {
			selectedIds = emptySet()
			actionMode = null
		}
	}
}

@Composable
private fun rememberBookmarkSelection(
	content: List<ListModel>,
	onAction: (itemId: Int, ids: Set<Long>, finish: () -> Unit) -> Boolean,
): BookmarkSelectionState {
	val context = LocalContext.current
	val activity = remember(context) { context.findActivity() as? AppCompatActivity }
	val contentState = rememberUpdatedState(content)
	val actionState = rememberUpdatedState(onAction)
	val state = remember(activity) {
		BookmarkSelectionState(
			activity = activity,
			contentProvider = { contentState.value },
			onAction = { itemId, ids, finish -> actionState.value(itemId, ids, finish) },
		)
	}
	DisposableEffect(state) {
		onDispose { state.clear() }
	}
	return state
}

private fun bookmarkListKey(item: ListModel, index: Int): Any = when (item) {
	is Bookmark -> "bm-${item.pageId}"
	is ListHeader -> "hd-$index-${item.hashCode()}"
	is EmptyState -> "empty"
	is ErrorState -> "error"
	is LoadingState -> "loading"
	else -> "item-$index"
}
