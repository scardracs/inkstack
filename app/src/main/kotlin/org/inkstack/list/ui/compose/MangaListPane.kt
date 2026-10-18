package org.inkstack.list.ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import org.inkstack.core.model.getTitle
import org.inkstack.explore.ui.model.MangaSourceItem
import org.inkstack.explore.ui.model.RecommendationsItem
import org.inkstack.list.domain.QuickFilterListener
import org.inkstack.list.ui.model.EmptyState
import org.inkstack.list.ui.model.ErrorState
import org.inkstack.list.ui.model.InfoModel
import org.inkstack.list.ui.model.ListHeader
import org.inkstack.list.ui.model.ListModel
import org.inkstack.list.ui.model.LoadingFooter
import org.inkstack.list.ui.model.LoadingState
import org.inkstack.list.ui.model.MangaListModel
import org.inkstack.list.ui.model.QuickFilter
import org.inkstack.parsers.model.Manga
import org.inkstack.parsers.model.MangaSource
import org.inkstack.tracker.ui.feed.model.FeedItem
import org.inkstack.tracker.ui.feed.model.UpdatedMangaHeader

private const val DEFAULT_MIN_CELL = 140

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaListPane(
	content: List<ListModel>,
	onMangaClick: (Manga) -> Unit,
	modifier: Modifier = Modifier,
	selection: MangaListSelectionState? = null,
	quickFilterListener: QuickFilterListener? = null,
	onSourceClick: ((MangaSource) -> Unit)? = null,
	onEmptyAction: (() -> Unit)? = null,
	onRetry: (() -> Unit)? = null,
	onLoadMore: (() -> Unit)? = null,
	onRefresh: (() -> Unit)? = null,
	isRefreshing: Boolean = false,
	imageLoader: ImageLoader? = null,
	contentPadding: PaddingValues = PaddingValues(bottom = 96.dp),
	minCellDp: Int = DEFAULT_MIN_CELL,
	gridState: LazyGridState = rememberLazyGridState(),
) {
	LaunchedEffect(gridState, onLoadMore) {
		if (onLoadMore == null) return@LaunchedEffect
		snapshotFlow {
			val info = gridState.layoutInfo
			val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
			val total = info.totalItemsCount
			total > 0 && last >= total - 4
		}
			.distinctUntilChanged()
			.filter { it }
			.collect { onLoadMore() }
	}

	val grid: @Composable () -> Unit = {
		LazyVerticalGrid(
			columns = GridCells.Adaptive(minSize = minCellDp.dp),
			state = gridState,
			modifier = Modifier.fillMaxSize(),
			contentPadding = contentPadding,
			horizontalArrangement = Arrangement.spacedBy(12.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp),
		) {
		itemsIndexed(
			items = content,
			key = { index, item -> listKey(item, index) },
			span = { _, item ->
				if (item is MangaListModel || item is FeedItem) {
					GridItemSpan(1)
				} else {
					GridItemSpan(maxLineSpan)
				}
			},
		) { _, item ->
			when (item) {
				is QuickFilter -> if (quickFilterListener != null) {
					QuickFilterRow(
						filter = item,
						onToggle = quickFilterListener::toggleFilterOption,
						modifier = Modifier.padding(top = 4.dp),
					)
				}

				is MangaListModel -> MangaGridItem(
					item = item,
					onClick = {
						val open = { onMangaClick(item.toMangaWithOverride()) }
						if (selection != null) {
							selection.onMangaClick(item.id, open)
						} else {
							open()
						}
					},
					onLongClick = selection?.let { sel -> { sel.onMangaLongClick(item.id) } },
					selected = selection?.selectedIds?.contains(item.id) == true,
					imageLoader = imageLoader,
					modifier = Modifier.padding(horizontal = 4.dp),
				)

				is ListHeader -> {
					val context = LocalContext.current
					Text(
						text = item.getText(context)?.toString().orEmpty(),
						style = MaterialTheme.typography.titleMedium,
						color = MaterialTheme.colorScheme.onSurface,
						modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
					)
				}

				is EmptyState -> EmptyStatePane(
					state = item,
					onAction = onEmptyAction,
				)

				is ErrorState -> ErrorStatePane(
					state = item,
					onRetry = onRetry,
				)

				is LoadingState -> Box(
					modifier = Modifier
						.fillMaxWidth()
						.padding(48.dp),
					contentAlignment = Alignment.Center,
				) {
					CircularProgressIndicator()
				}

				is LoadingFooter -> Box(
					modifier = Modifier
						.fillMaxWidth()
						.padding(16.dp),
					contentAlignment = Alignment.Center,
				) {
					CircularProgressIndicator()
				}

				is InfoModel -> InfoBanner(item)

				is MangaSourceItem -> SourceRow(
					item = item,
					onClick = { onSourceClick?.invoke(item.source.mangaSource) },
				)

				is RecommendationsItem -> LazyRow(
					contentPadding = PaddingValues(horizontal = 12.dp),
					horizontalArrangement = Arrangement.spacedBy(12.dp),
				) {
					items(item.manga, key = { it.id }) { manga ->
						MangaGridItem(
							item = manga,
							onClick = {
								val open = { onMangaClick(manga.toMangaWithOverride()) }
								if (selection != null) {
									selection.onMangaClick(manga.id, open)
								} else {
									open()
								}
							},
							onLongClick = selection?.let { sel -> { sel.onMangaLongClick(manga.id) } },
							selected = selection?.selectedIds?.contains(manga.id) == true,
							imageLoader = imageLoader,
							modifier = Modifier.width(120.dp),
						)
					}
				}

				is FeedItem -> FeedItemCell(
					item = item,
					onClick = { onMangaClick(item.toMangaWithOverride()) },
					imageLoader = imageLoader,
				)

				is UpdatedMangaHeader -> {
					// Header strip of recently updated manga — open details on tap.
					LazyRow(
						contentPadding = PaddingValues(horizontal = 12.dp),
						horizontalArrangement = Arrangement.spacedBy(12.dp),
					) {
						items(item.list, key = { it.id }) { manga ->
							MangaGridItem(
								item = manga,
								onClick = { onMangaClick(manga.toMangaWithOverride()) },
								imageLoader = imageLoader,
								modifier = Modifier.width(120.dp),
							)
						}
					}
				}

				else -> Unit
			}
		}
		}
	}

	if (onRefresh != null) {
		PullToRefreshBox(
			isRefreshing = isRefreshing,
			onRefresh = onRefresh,
			modifier = modifier.fillMaxSize(),
		) {
			grid()
		}
	} else {
		Box(modifier.fillMaxSize()) {
			grid()
		}
	}
}

@Composable
private fun EmptyStatePane(
	state: EmptyState,
	onAction: (() -> Unit)?,
) {
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
		if (state.actionStringRes != 0 && onAction != null) {
			TextButton(onClick = onAction) {
				Text(stringResource(state.actionStringRes))
			}
		}
	}
}

@Composable
private fun ErrorStatePane(
	state: ErrorState,
	onRetry: (() -> Unit)?,
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(32.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(8.dp),
	) {
		Text(
			text = state.exception.localizedMessage ?: state.exception.javaClass.simpleName,
			style = MaterialTheme.typography.bodyMedium,
			textAlign = TextAlign.Center,
		)
		if (state.canRetry && onRetry != null) {
			TextButton(onClick = onRetry) {
				Text(stringResource(state.buttonText))
			}
		}
	}
}

@Composable
private fun InfoBanner(item: InfoModel) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 16.dp, vertical = 8.dp),
	) {
		Text(
			text = stringResource(item.title),
			style = MaterialTheme.typography.titleSmall,
		)
		Text(
			text = stringResource(item.text),
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
		)
	}
}

@Composable
private fun SourceRow(
	item: MangaSourceItem,
	onClick: () -> Unit,
) {
	val context = LocalContext.current
	Text(
		text = item.source.getTitle(context),
		style = MaterialTheme.typography.bodyLarge,
		color = MaterialTheme.colorScheme.onSurface,
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onClick)
			.padding(horizontal = 16.dp, vertical = 12.dp),
	)
}

@Composable
private fun FeedItemCell(
	item: FeedItem,
	onClick: () -> Unit,
	imageLoader: ImageLoader?,
) {
	val context = LocalContext.current
	Column(
		modifier = Modifier
			.padding(horizontal = 4.dp)
			.clickable(onClick = onClick),
	) {
		org.inkstack.core.ui.compose.MangaCover(
			model = coil3.request.ImageRequest.Builder(context)
				.data(item.imageUrl)
				.build(),
			contentDescription = item.title,
			imageLoader = imageLoader,
			modifier = Modifier.fillMaxWidth(),
		)
		Text(
			text = item.title,
			style = MaterialTheme.typography.bodySmall,
			maxLines = 2,
			modifier = Modifier.padding(top = 6.dp),
		)
	}
}

private fun listKey(item: ListModel, index: Int): Any = when (item) {
	is MangaListModel -> "manga-${item.id}"
	is FeedItem -> "feed-${item.id}"
	is UpdatedMangaHeader -> "updated-header"
	is ListHeader -> "header-$index-${item.hashCode()}"
	is QuickFilter -> "quick-filter"
	is EmptyState -> "empty"
	is ErrorState -> "error"
	is LoadingState -> "loading"
	is LoadingFooter -> "loading-footer"
	is InfoModel -> "info-${item.key}"
	is MangaSourceItem -> "source-${item.id}"
	else -> "item-$index-${item.javaClass.simpleName}"
}
