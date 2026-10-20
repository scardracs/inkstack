package org.inkstack.details.ui.compose.chapters

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.ImageLoader
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import org.inkstack.R
import org.inkstack.bookmarks.domain.Bookmark
import org.inkstack.core.model.isLocal
import org.inkstack.core.nav.ReaderIntent
import org.inkstack.core.nav.router
import org.inkstack.core.prefs.AppSettings
import org.inkstack.core.ui.compose.InkstackGlassShape
import org.inkstack.core.ui.compose.InkstackGlassSurface
import org.inkstack.core.ui.compose.fastscroll.FastScrollBubbleSize
import org.inkstack.core.ui.compose.fastscroll.FastScroller
import org.inkstack.details.data.MangaDetails
import org.inkstack.details.ui.DetailsViewModel
import org.inkstack.details.ui.model.ChapterListItem
import org.inkstack.details.ui.model.HistoryInfo
import org.inkstack.details.ui.DetailsTabs
import org.inkstack.details.ui.pager.bookmarks.BookmarksViewModel
import org.inkstack.details.ui.pager.pages.PageThumbnail
import org.inkstack.details.ui.pager.pages.PagesViewModel
import org.inkstack.details.ui.withVolumeHeaders
import org.inkstack.list.domain.ListFilterOption
import org.inkstack.list.ui.model.EmptyState
import org.inkstack.list.ui.model.ListHeader
import org.inkstack.list.ui.model.ListModel
import org.inkstack.list.ui.model.LoadingState
import org.inkstack.parsers.model.Manga
import org.inkstack.parsers.model.MangaChapter
import org.inkstack.reader.ui.ReaderState

/**
 * Inline chapters / pages / bookmarks under details info.
 * HorizontalPager: swipe right→left through chapters → pages → bookmarks.
 */
@Composable
fun ChaptersPagesPane(
	imageLoader: ImageLoader,
	settings: AppSettings,
	viewModel: DetailsViewModel,
	onRead: (Manga, MangaChapter?, incognito: Boolean) -> Unit,
	modifier: Modifier = Modifier,
	pagesViewModel: PagesViewModel = hiltViewModel(),
	bookmarksViewModel: BookmarksViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val activity = context as FragmentActivity
	val scope = rememberCoroutineScope()
	val pagesEnabled = settings.isPagesTabEnabled
	val pageCount = if (pagesEnabled) 3 else 2
	val initialPage = pagerPositionFromLogicalTab(settings.defaultDetailsTab, pagesEnabled)
		.coerceIn(0, pageCount - 1)
	val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { pageCount })
	val logicalTab = remember(pagerState.currentPage, pagesEnabled) {
		logicalTabFromPagerPosition(pagerState.currentPage, pagesEnabled)
	}
	val newChapters by viewModel.newChaptersCount.collectAsState()
	val mangaDetails by viewModel.mangaDetails.collectAsState()
	val readingState by viewModel.readingState.collectAsState()
	val selectedBranch by viewModel.selectedBranch.collectAsState()
	val historyInfo by viewModel.historyInfo.collectAsState()
	val isLoading by viewModel.isLoading.collectAsState()
	val manga = mangaDetails?.toManga()

	LaunchedEffect(mangaDetails, readingState, selectedBranch) {
		pagesViewModel.updateState(
			mangaDetails?.let {
				PagesViewModel.State(it, readingState, selectedBranch)
			},
		)
		bookmarksViewModel.emit(mangaDetails)
	}

	LaunchedEffect(pagerState) {
		snapshotFlow { pagerState.currentPage }.collect { page ->
			settings.lastDetailsTab = logicalTabFromPagerPosition(page, pagesEnabled)
		}
	}

	Column(modifier = modifier.fillMaxSize()) {
		manga?.let { m ->
			ReadSplitButton(
				manga = m,
				historyInfo = historyInfo,
				isLoading = isLoading,
				viewModel = viewModel,
				onRead = onRead,
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 12.dp, vertical = 4.dp),
			)
		}
		InkstackGlassSurface(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 12.dp, vertical = 4.dp),
			shape = InkstackGlassShape,
			alpha = 0.88f,
		) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 4.dp, vertical = 2.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				Text(
					text = stringResource(R.string.chapters),
					style = MaterialTheme.typography.titleMedium,
					modifier = Modifier
						.weight(1f)
						.padding(start = 12.dp),
				)
				PaneTabs(
					pagesEnabled = pagesEnabled,
					selectedPagerTab = pagerState.currentPage,
					newChapters = newChapters,
					onSelectLogical = { logical ->
						scope.launch {
							pagerState.animateScrollToPage(
								pagerPositionFromLogicalTab(logical, pagesEnabled),
							)
						}
					},
				)
				PaneOverflowMenu(
					viewModel = viewModel,
					settings = settings,
					logicalTab = logicalTab,
					mangaDetails = mangaDetails,
				)
			}
		}

		HorizontalPager(
			state = pagerState,
			modifier = Modifier
				.weight(1f)
				.fillMaxWidth(),
			beyondViewportPageCount = 0,
		) { page ->
			when (logicalTabFromPagerPosition(page, pagesEnabled)) {
				DetailsTabs.CHAPTERS -> ChaptersTab(
					viewModel = viewModel,
					onChapterClick = { chapter ->
						manga?.let { onRead(it, chapter, false) }
					},
					modifier = Modifier.fillMaxSize(),
				)
				DetailsTabs.PAGES -> PagesTab(
					pagesViewModel = pagesViewModel,
					imageLoader = imageLoader,
					onPageClick = { thumb ->
						manga?.let { m ->
							activity.router.openReader(
								ReaderIntent.Builder(context)
									.manga(m)
									.state(ReaderState(thumb.page.chapterId, thumb.page.index, 0))
									.build(),
							)
						}
					},
					modifier = Modifier.fillMaxSize(),
				)
				else -> BookmarksTab(
					bookmarksViewModel = bookmarksViewModel,
					imageLoader = imageLoader,
					onBookmarkClick = { bookmark ->
						activity.router.openReader(
							ReaderIntent.Builder(context)
								.manga(bookmark.manga)
								.bookmark(bookmark)
								.incognito()
								.build(),
						)
					},
					modifier = Modifier.fillMaxSize(),
				)
			}
		}
	}
}

@Composable
private fun PaneTabs(
	pagesEnabled: Boolean,
	selectedPagerTab: Int,
	newChapters: Int,
	onSelectLogical: (Int) -> Unit,
) {
	val icons = buildList {
		add(R.drawable.ic_list to DetailsTabs.CHAPTERS)
		if (pagesEnabled) add(R.drawable.ic_grid to DetailsTabs.PAGES)
		add(R.drawable.ic_bookmark to DetailsTabs.BOOKMARKS)
	}
	Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
		icons.forEachIndexed { pagerIndex, (icon, logical) ->
			val selected = selectedPagerTab == pagerIndex
			IconButton(onClick = { onSelectLogical(logical) }) {
				BadgedBox(
					badge = {
						if (logical == DetailsTabs.CHAPTERS && newChapters > 0) {
							Badge { Text(if (newChapters > 99) "99+" else newChapters.toString()) }
						}
					},
				) {
					Icon(
						painter = painterResource(icon),
						contentDescription = null,
						tint = if (selected) {
							MaterialTheme.colorScheme.primary
						} else {
							MaterialTheme.colorScheme.onSurfaceVariant
						},
					)
				}
			}
		}
	}
}

@Composable
private fun PaneOverflowMenu(
	viewModel: DetailsViewModel,
	settings: AppSettings,
	logicalTab: Int,
	mangaDetails: MangaDetails?,
) {
	var expanded by remember { mutableStateOf(false) }
	var searchOpen by remember { mutableStateOf(false) }
	var searchQuery by remember { mutableStateOf("") }
	val reversed by viewModel.isChaptersReversed.collectAsState()
	val grid by viewModel.isChaptersInGridView.collectAsState()
	val downloadedOnly by viewModel.isDownloadedOnly.collectAsState()
	val emptyReason by viewModel.emptyReason.collectAsState()

	if (searchOpen && logicalTab == DetailsTabs.CHAPTERS) {
		OutlinedTextField(
			value = searchQuery,
			onValueChange = {
				searchQuery = it
				viewModel.performChapterSearch(it.ifBlank { null })
			},
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 12.dp),
			singleLine = true,
			placeholder = { Text(stringResource(R.string.search_chapters)) },
			trailingIcon = {
				TextButton(
					onClick = {
						searchOpen = false
						searchQuery = ""
						viewModel.performChapterSearch(null)
					},
				) { Text(stringResource(android.R.string.cancel)) }
			},
		)
	}

	Box {
		IconButton(onClick = { expanded = true }) {
			Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more))
		}
		DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
			when (logicalTab) {
				DetailsTabs.CHAPTERS -> {
					if (emptyReason == null) {
						DropdownMenuItem(
							text = { Text(stringResource(R.string.search_chapters)) },
							onClick = {
								expanded = false
								searchOpen = true
							},
							leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
						)
					}
					DropdownMenuItem(
						text = { Text(stringResource(R.string.reverse)) },
						onClick = {
							viewModel.setChaptersReversed(!reversed)
							expanded = false
						},
						trailingIcon = {
							Checkbox(checked = reversed, onCheckedChange = null)
						},
					)
					DropdownMenuItem(
						text = { Text(stringResource(R.string.chapters_grid_view)) },
						onClick = {
							viewModel.setChaptersInGridView(!grid)
							expanded = false
						},
						trailingIcon = {
							Checkbox(checked = grid, onCheckedChange = null)
						},
					)
					if (mangaDetails?.local != null) {
						DropdownMenuItem(
							text = { Text(stringResource(R.string.on_device)) },
							onClick = {
								viewModel.isDownloadedOnly.value = !downloadedOnly
								expanded = false
							},
							trailingIcon = {
								Checkbox(checked = downloadedOnly, onCheckedChange = null)
							},
						)
					}
				}
				else -> {
					var gridSize by remember { mutableFloatStateOf(settings.gridSizePages.toFloat()) }
					Text(
						text = stringResource(R.string.grid_size),
						modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
						style = MaterialTheme.typography.labelLarge,
					)
					Slider(
						value = gridSize,
						onValueChange = {
							gridSize = it
							settings.gridSizePages = it.toInt()
						},
						valueRange = 50f..150f,
						steps = 19,
						modifier = Modifier
							.width(220.dp)
							.padding(horizontal = 16.dp),
					)
				}
			}
		}
	}
}

@Composable
private fun ReadSplitButton(
	manga: Manga,
	historyInfo: HistoryInfo,
	isLoading: Boolean,
	viewModel: DetailsViewModel,
	onRead: (Manga, MangaChapter?, incognito: Boolean) -> Unit,
	modifier: Modifier = Modifier,
) {
	val context = LocalContext.current
	val activity = context as FragmentActivity
	var menuExpanded by remember { mutableStateOf(false) }
	val branches by viewModel.branches.collectAsState()
	val chaptersLoading = isLoading && (historyInfo.totalChapters <= 0 || historyInfo.isChapterMissing)
	val label = when {
		chaptersLoading -> stringResource(R.string.loading_)
		historyInfo.isIncognitoMode -> stringResource(R.string.incognito)
		historyInfo.canContinue -> {
			val chapterNumber = manga.chapters
				?.filter { it.branch == viewModel.selectedBranchValue }
				?.getOrNull(historyInfo.currentChapter)
				?.numberString()
			val base = stringResource(R.string._continue)
			if (chapterNumber.isNullOrEmpty()) base else "$base · $chapterNumber"
		}
		else -> stringResource(R.string.read)
	}
	val enabled = !chaptersLoading && historyInfo.isValid

	Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
		Button(
			onClick = { if (enabled) onRead(manga, null, false) },
			enabled = enabled,
			modifier = Modifier.weight(1f),
			shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp, topEnd = 4.dp, bottomEnd = 4.dp),
		) {
			Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
			Spacer(Modifier.width(8.dp))
			Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
		}
		Box {
			FilledIconButton(
				onClick = { menuExpanded = true },
				enabled = enabled,
				shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 24.dp, bottomEnd = 24.dp),
				colors = IconButtonDefaults.filledIconButtonColors(
					containerColor = MaterialTheme.colorScheme.primary,
					contentColor = MaterialTheme.colorScheme.onPrimary,
				),
			) {
				Icon(Icons.Default.ArrowDropDown, contentDescription = null)
			}
			DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
				if (!historyInfo.isIncognitoMode) {
					DropdownMenuItem(
						text = { Text(stringResource(R.string.incognito_mode)) },
						onClick = {
							menuExpanded = false
							onRead(manga, null, true)
						},
					)
				}
				if (historyInfo.history != null) {
					DropdownMenuItem(
						text = { Text(stringResource(R.string.remove_from_history)) },
						onClick = {
							menuExpanded = false
							viewModel.removeFromHistory()
						},
					)
				}
				if (!manga.isLocal) {
					DropdownMenuItem(
						text = { Text(stringResource(R.string.download)) },
						onClick = {
							menuExpanded = false
							activity.router.showDownloadDialog(setOf(manga), null)
						},
					)
				}
				if (branches.size > 1) {
					branches.forEach { branch ->
						DropdownMenuItem(
							text = {
								Text(
									text = buildString {
										append(branch.name ?: context.getString(R.string.system_default))
										append("  ")
										append(branch.count)
									},
									fontWeight = if (branch.isSelected) FontWeight.Bold else FontWeight.Normal,
								)
							},
							onClick = {
								menuExpanded = false
								viewModel.setSelectedBranch(branch.name)
							},
						)
					}
				}
			}
		}
	}
}

@Composable
private fun ChaptersTab(
	viewModel: DetailsViewModel,
	onChapterClick: (MangaChapter) -> Unit,
	modifier: Modifier = Modifier,
) {
	val context = LocalContext.current
	val chapters by viewModel.chapters.collectAsState()
	val grid by viewModel.isChaptersInGridView.collectAsState()
	val quickFilter by viewModel.quickFilter.collectAsState(initial = emptyList())
	val emptyReason by viewModel.emptyReason.collectAsState()
	val items = remember(chapters, context) { chapters.withVolumeHeaders(context) }
	val listState = rememberLazyListState()
	val gridState = rememberLazyGridState()

	LaunchedEffect(items) {
		val currentIndex = items.indexOfFirst { it is ChapterListItem && it.isCurrent }
		if (currentIndex >= 0) {
			if (grid) gridState.scrollToItem(currentIndex) else listState.scrollToItem(currentIndex)
		}
	}

	Column(modifier = modifier.fillMaxSize()) {
		if (quickFilter.isNotEmpty()) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.horizontalScroll(rememberScrollState())
					.padding(horizontal = 12.dp, vertical = 4.dp),
				horizontalArrangement = Arrangement.spacedBy(8.dp),
			) {
				quickFilter.forEach { chip ->
					val option = chip.data as? ListFilterOption.Branch ?: return@forEach
					FilterChip(
						selected = chip.isChecked,
						onClick = { viewModel.setSelectedBranch(option.titleText) },
						label = { Text(chip.title?.toString() ?: option.titleText.orEmpty()) },
					)
				}
			}
		}
		if (emptyReason != null) {
			Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
				Text(stringResource(emptyReason!!.msgResId))
			}
		} else {
		Box(Modifier.fillMaxSize()) {
			val hasVolumes = remember(items) { items.any { model -> model is ListHeader } }
			val sectionIndexer: (Int) -> String? = remember(items, hasVolumes) {
				{ position: Int ->
					if (hasVolumes) {
						(items.getOrNull(position) as? ListHeader)?.getText(context)?.toString()
							?: items.take(position + 1).lastOrNull { model -> model is ListHeader }
								.let { header -> (header as? ListHeader)?.getText(context)?.toString() }
					} else {
						(items.getOrNull(position) as? ChapterListItem)?.chapter?.numberString()
					}
				}
			}
			if (grid) {
				LazyVerticalGrid(
					columns = GridCells.Adaptive(minSize = 96.dp),
					state = gridState,
					contentPadding = PaddingValues(12.dp),
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					verticalArrangement = Arrangement.spacedBy(8.dp),
					modifier = Modifier.fillMaxSize(),
				) {
					itemsIndexed(items, key = { index, item -> itemKey(item, index) }) { _, item ->
						when (item) {
							is ListHeader -> Text(
								text = item.getText(context)?.toString().orEmpty(),
								style = MaterialTheme.typography.titleSmall,
								modifier = Modifier.padding(vertical = 8.dp),
							)
							is ChapterListItem -> ChapterGridCell(item = item, onClick = { onChapterClick(item.chapter) })
						}
					}
				}
				FastScroller(
					state = gridState,
					itemCount = items.size,
					sectionIndexer = sectionIndexer,
					bubbleSize = FastScrollBubbleSize.SMALL,
				)
			} else {
				LazyColumn(
					state = listState,
					contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
					verticalArrangement = Arrangement.spacedBy(4.dp),
					modifier = Modifier.fillMaxSize(),
				) {
					itemsIndexed(items, key = { index, item -> itemKey(item, index) }) { _, item ->
						when (item) {
							is ListHeader -> Text(
								text = item.getText(context)?.toString().orEmpty(),
								style = MaterialTheme.typography.titleSmall,
								modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
							)
							is ChapterListItem -> ChapterListRow(item = item, onClick = { onChapterClick(item.chapter) })
						}
					}
				}
				FastScroller(
					state = listState,
					itemCount = items.size,
					sectionIndexer = sectionIndexer,
					bubbleSize = FastScrollBubbleSize.SMALL,
				)
			}
		}
		}
	}
}

@Composable
private fun ChapterListRow(item: ChapterListItem, onClick: () -> Unit) {
	val resources = LocalContext.current.resources
	val container = when {
		item.isCurrent -> MaterialTheme.colorScheme.primaryContainer
		item.isUnread && item.isNew -> MaterialTheme.colorScheme.secondaryContainer
		item.isUnread -> MaterialTheme.colorScheme.surfaceContainer
		else -> MaterialTheme.colorScheme.surfaceContainerLow
	}
	val content = when {
		item.isCurrent -> MaterialTheme.colorScheme.onPrimaryContainer
		item.isUnread && item.isNew -> MaterialTheme.colorScheme.onSecondaryContainer
		item.isUnread -> MaterialTheme.colorScheme.onSurface
		else -> MaterialTheme.colorScheme.onSurfaceVariant
	}
	Surface(
		onClick = onClick,
		shape = RoundedCornerShape(16.dp),
		color = container,
		modifier = Modifier.fillMaxWidth(),
	) {
		Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
			Text(
				text = item.getTitle(resources),
				color = content,
				style = MaterialTheme.typography.titleSmall,
				fontWeight = if (item.isCurrent || item.isNew) FontWeight.Bold else FontWeight.Normal,
				maxLines = 2,
				overflow = TextOverflow.Ellipsis,
			)
			item.description?.let {
				Text(
					text = it,
					color = content.copy(alpha = 0.8f),
					style = MaterialTheme.typography.bodySmall,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
			}
		}
	}
}

@Composable
private fun ChapterGridCell(item: ChapterListItem, onClick: () -> Unit) {
	val container = when {
		item.isCurrent -> MaterialTheme.colorScheme.primaryContainer
		item.isUnread -> MaterialTheme.colorScheme.surfaceContainer
		else -> MaterialTheme.colorScheme.surfaceContainerLow
	}
	Surface(
		onClick = onClick,
		shape = RoundedCornerShape(12.dp),
		color = container,
		modifier = Modifier
			.fillMaxWidth()
			.height(56.dp),
	) {
		Box(contentAlignment = Alignment.Center) {
			Text(
				text = item.chapter.numberString() ?: "?",
				style = MaterialTheme.typography.titleMedium,
				fontWeight = if (item.isCurrent) FontWeight.Bold else FontWeight.Normal,
			)
		}
	}
}

@Composable
private fun PagesTab(
	pagesViewModel: PagesViewModel,
	imageLoader: ImageLoader,
	onPageClick: (PageThumbnail) -> Unit,
	modifier: Modifier = Modifier,
) {
	val thumbnails by pagesViewModel.thumbnails.collectAsState()
	val gridScale by pagesViewModel.gridScale.collectAsState()
	val gridState = rememberLazyGridState()
	val context = LocalContext.current
	val minSize = (100.dp * gridScale).coerceIn(64.dp, 160.dp)

	LaunchedEffect(gridState, thumbnails.size) {
		snapshotNearEdges(gridState, thumbnails.size, onTop = {
			pagesViewModel.loadPrevChapter()
		}, onBottom = {
			pagesViewModel.loadNextChapter()
		})
	}

	Box(modifier = modifier.fillMaxSize()) {
		if (thumbnails.isEmpty()) {
			Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
				Text(stringResource(R.string.loading_))
			}
		} else {
			val sectionIndexer: (Int) -> String? = remember(thumbnails) {
				{ position ->
					thumbnails.take(position + 1).lastOrNull { it is ListHeader }
						.let { (it as? ListHeader)?.getText(context)?.toString() }
				}
			}
			LazyVerticalGrid(
				columns = GridCells.Adaptive(minSize = minSize),
				state = gridState,
				contentPadding = PaddingValues(12.dp),
				horizontalArrangement = Arrangement.spacedBy(6.dp),
				verticalArrangement = Arrangement.spacedBy(6.dp),
				modifier = Modifier.fillMaxSize(),
			) {
				itemsIndexed(
					thumbnails,
					key = { index, item -> itemKey(item, index) },
					span = { _, item ->
						if (item is ListHeader) GridItemSpan(maxLineSpan) else GridItemSpan(1)
					},
				) { _, item ->
					when (item) {
						is ListHeader -> Text(
							text = item.getText(context)?.toString().orEmpty(),
							style = MaterialTheme.typography.titleSmall,
							modifier = Modifier.padding(vertical = 6.dp),
						)
						is PageThumbnail -> PageThumbCell(
							item = item,
							imageLoader = imageLoader,
							onClick = { onPageClick(item) },
						)
					}
				}
			}
			FastScroller(
				state = gridState,
				itemCount = thumbnails.size,
				sectionIndexer = sectionIndexer,
				bubbleSize = FastScrollBubbleSize.SMALL,
			)
		}
	}
}

/** Portrait page preview — matches View thumbs (width:height = 13:18). */
private val PageThumbAspect = 13f / 18f

@Composable
private fun PageThumbCell(
	item: PageThumbnail,
	imageLoader: ImageLoader,
	onClick: () -> Unit,
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			// width/height = 13/18 → taller than wide (portrait manga page).
			.aspectRatio(ratio = PageThumbAspect, matchHeightConstraintsFirst = false)
			.clip(RoundedCornerShape(8.dp))
			.clickable(onClick = onClick)
			.background(MaterialTheme.colorScheme.surfaceContainer),
	) {
		AsyncImage(
			model = item.page.toMangaPage(),
			contentDescription = null,
			imageLoader = imageLoader,
			modifier = Modifier.fillMaxSize(),
			contentScale = ContentScale.Crop,
			alignment = Alignment.TopCenter,
		)
		Text(
			text = item.number.toString(),
			style = MaterialTheme.typography.labelSmall,
			color = if (item.isCurrent) {
				MaterialTheme.colorScheme.onTertiary
			} else {
				MaterialTheme.colorScheme.onSurface
			},
			modifier = Modifier
				.align(Alignment.BottomEnd)
				.padding(4.dp)
				.background(
					if (item.isCurrent) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
					RoundedCornerShape(6.dp),
				)
				.padding(horizontal = 6.dp, vertical = 2.dp),
		)
	}
}

@Composable
private fun BookmarksTab(
	bookmarksViewModel: BookmarksViewModel,
	imageLoader: ImageLoader,
	onBookmarkClick: (Bookmark) -> Unit,
	modifier: Modifier = Modifier,
) {
	val content by bookmarksViewModel.content.collectAsState()
	val gridScale by bookmarksViewModel.gridScale.collectAsState()
	val gridState = rememberLazyGridState()
	val context = LocalContext.current
	val minSize = (100.dp * gridScale).coerceIn(64.dp, 160.dp)

	Box(modifier = modifier.fillMaxSize()) {
		when {
			content.any { it is LoadingState } && content.size == 1 -> {
				Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
					Text(stringResource(R.string.loading_))
				}
			}
			content.any { it is EmptyState } -> {
				Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
					Text(stringResource(R.string.no_bookmarks_summary))
				}
			}
			else -> {
				val sectionIndexer: (Int) -> String? = remember(content) {
					{ position ->
						content.take(position + 1).lastOrNull { it is ListHeader }
							.let { (it as? ListHeader)?.getText(context)?.toString() }
					}
				}
				LazyVerticalGrid(
					columns = GridCells.Adaptive(minSize = minSize),
					state = gridState,
					contentPadding = PaddingValues(12.dp),
					horizontalArrangement = Arrangement.spacedBy(6.dp),
					verticalArrangement = Arrangement.spacedBy(6.dp),
					modifier = Modifier.fillMaxSize(),
				) {
					itemsIndexed(content, key = { index, item -> itemKey(item, index) }) { _, item ->
						when (item) {
							is ListHeader -> Text(
								text = item.getText(context)?.toString().orEmpty(),
								style = MaterialTheme.typography.titleSmall,
								modifier = Modifier.padding(vertical = 6.dp),
							)
							is Bookmark -> {
								AsyncImage(
									model = item.imageUrl,
									contentDescription = null,
									imageLoader = imageLoader,
									modifier = Modifier
										.fillMaxWidth()
										.aspectRatio(PageThumbAspect)
										.clip(RoundedCornerShape(8.dp))
										.clickable { onBookmarkClick(item) },
									contentScale = ContentScale.Crop,
								)
							}
						}
					}
				}
				FastScroller(
					state = gridState,
					itemCount = content.size,
					sectionIndexer = sectionIndexer,
					bubbleSize = FastScrollBubbleSize.SMALL,
				)
			}
		}
	}
}

private fun itemKey(item: ListModel, index: Int): Any = when (item) {
	is ChapterListItem -> "ch-${item.chapter.id}"
	is PageThumbnail -> "pg-${item.page.chapterId}-${item.page.index}"
	is Bookmark -> "bm-${item.pageId}"
	is ListHeader -> "hd-$index-${item.hashCode()}"
	else -> index
}

private fun pagerPositionFromLogicalTab(tab: Int, isPagesTabEnabled: Boolean): Int {
	if (!isPagesTabEnabled && tab > DetailsTabs.CHAPTERS) {
		return (tab - 1).coerceAtLeast(DetailsTabs.CHAPTERS)
	}
	return tab
}

private fun logicalTabFromPagerPosition(position: Int, isPagesTabEnabled: Boolean): Int {
	if (!isPagesTabEnabled && position >= DetailsTabs.PAGES) {
		return position + 1
	}
	return position
}

private suspend fun snapshotNearEdges(
	state: androidx.compose.foundation.lazy.grid.LazyGridState,
	itemCount: Int,
	onTop: () -> Unit,
	onBottom: () -> Unit,
) {
	snapshotFlow {
		val info = state.layoutInfo
		val first = info.visibleItemsInfo.firstOrNull()?.index ?: 0
		val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
		first to last
	}.collect { (first, last) ->
		if (first <= 2) onTop()
		if (itemCount > 0 && last >= itemCount - 3) onBottom()
	}
}
