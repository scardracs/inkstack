package org.inkstack.reader.ui.compose

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil3.ImageLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.inkstack.core.exceptions.resolve.ExceptionResolver
import org.inkstack.core.os.NetworkState
import org.inkstack.reader.domain.PageLoader
import org.inkstack.reader.ui.ReaderState
import org.inkstack.reader.ui.ReaderViewModel
import org.inkstack.reader.ui.pager.ReaderPage

class ReaderScrollController(
	private val listState: LazyListState,
	private val pages: () -> List<ReaderPage>,
	private val chapterId: () -> Long?,
	private val switchChapterBy: (Int) -> Unit,
	private val scope: CoroutineScope,
) {
	fun scrollByPages(delta: Int) {
		val list = pages()
		if (list.isEmpty()) return
		val next = listState.firstVisibleItemIndex + delta
		when {
			next in list.indices -> scrollToGlobal(next)
			delta > 0 -> switchChapterBy(1)
			delta < 0 -> switchChapterBy(-1)
		}
	}

	fun scrollToChapterPage(pageIndex: Int) {
		val list = pages()
		val id = chapterId() ?: return
		val global = list.indexOfFirst { it.chapterId == id && it.index == pageIndex }
		if (global >= 0) scrollToGlobal(global)
	}

	fun scrollPixels(delta: Int, smooth: Boolean): Boolean {
		if (!listState.canScrollForward && delta > 0) return false
		if (!listState.canScrollBackward && delta < 0) return false
		scope.launch {
			if (smooth) {
				listState.animateScrollBy(delta.toFloat())
			} else {
				listState.scroll {
					scrollBy(delta.toFloat())
				}
			}
		}
		return true
	}

	/** Live position for history: first visible page + in-page scroll offset. */
	fun currentState(): ReaderState? {
		val list = pages()
		val page = list.getOrNull(listState.firstVisibleItemIndex) ?: return null
		return ReaderState(
			chapterId = page.chapterId,
			page = page.index,
			scroll = listState.firstVisibleItemScrollOffset.coerceAtLeast(0),
		)
	}

	private fun scrollToGlobal(index: Int) {
		val last = (pages().size - 1).coerceAtLeast(0)
		val target = index.coerceIn(0, last)
		scope.launch {
			listState.animateScrollToItem(target)
		}
	}
}

@Composable
fun ReaderScreen(
	viewModel: ReaderViewModel,
	pageLoader: PageLoader,
	networkState: NetworkState,
	exceptionResolver: ExceptionResolver,
	imageLoader: ImageLoader,
	onBack: () -> Unit,
	onOpenMenu: () -> Unit,
	onComments: () -> Unit,
	systemUiVisible: Boolean,
	onToggleUi: () -> Unit,
	onScrollController: ((ReaderScrollController?) -> Unit)? = null,
	onTapAt: ((xFraction: Float, yFraction: Float) -> Unit)? = null,
) {
	val content by viewModel.content.collectAsState()
	val mode by viewModel.readerMode.collectAsState()
	val uiState by viewModel.uiState.collectAsState()
	val isLoading by viewModel.isLoading.collectAsState()
	val webtoonGaps by viewModel.isWebtoonGapsEnabled.collectAsState()
	val zoomEnabled by viewModel.isWebtoonZooEnabled.collectAsState()
	val settingsProducer = viewModel.readerSettingsProducer
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	val pages = content.pages
	val initialIndex = indexOfState(pages, content.state).coerceIn(0, (pages.size - 1).coerceAtLeast(0))
	val listState = rememberLazyListState(
		initialFirstVisibleItemIndex = initialIndex,
		initialFirstVisibleItemScrollOffset = content.state?.scroll?.coerceAtLeast(0) ?: 0,
	)

	val pagesRef = rememberUpdatedState(pages)
	val chapterIdRef = rememberUpdatedState(uiState?.chapter?.id ?: content.state?.chapterId)
	val scrollController = remember(listState, scope) {
		ReaderScrollController(
			listState = listState,
			pages = { pagesRef.value },
			chapterId = { chapterIdRef.value },
			switchChapterBy = viewModel::switchChapterBy,
			scope = scope,
		)
	}
	DisposableEffect(scrollController) {
		onScrollController?.invoke(scrollController)
		onDispose {
			scrollController.currentState()?.let(viewModel::saveCurrentState)
				?: viewModel.saveCurrentState()
			onScrollController?.invoke(null)
		}
	}

	var trackedChapterId by remember { mutableStateOf<Long?>(null) }
	LaunchedEffect(content.state?.chapterId, content.state?.page, content.state?.scroll, pages.isNotEmpty()) {
		val state = content.state ?: return@LaunchedEffect
		if (pages.isEmpty()) return@LaunchedEffect
		val visibleChapter = pages.getOrNull(listState.firstVisibleItemIndex)?.chapterId
		val jumped = trackedChapterId != null && trackedChapterId != state.chapterId && visibleChapter != state.chapterId
		val firstOpen = trackedChapterId == null
		if (firstOpen || jumped) {
			listState.scrollToItem(
				index = indexOfState(pages, state),
				scrollOffset = state.scroll.coerceAtLeast(0),
			)
		}
		trackedChapterId = state.chapterId
	}

	Box(modifier = Modifier.fillMaxSize()) {
		if (pages.isEmpty() || mode == null) {
			if (isLoading) {
				CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
			}
		} else {
			ReaderPager(
				pages = pages,
				listState = listState,
				webtoonGaps = webtoonGaps,
				loader = pageLoader,
				settingsProducer = settingsProducer,
				networkState = networkState,
				exceptionResolver = exceptionResolver,
				imageLoader = imageLoader,
				onPageRangeChanged = { lo, hi, scroll ->
					viewModel.onCurrentPageChanged(lo, hi, scroll)
				},
				onTap = onToggleUi,
				onTapAt = onTapAt,
				zoomEnabled = zoomEnabled,
				modifier = Modifier.fillMaxSize(),
			)
		}

		val chapterTitle = uiState?.getChapterTitle(context.resources).orEmpty()
		ReaderTopBar(
			visible = systemUiVisible,
			uiState = uiState,
			chapterTitle = chapterTitle,
			onBack = onBack,
			modifier = Modifier.align(Alignment.TopCenter),
		)
		ReaderBottomBar(
			visible = systemUiVisible,
			uiState = uiState,
			reversed = false,
			onPrevChapter = { viewModel.switchChapterBy(-1) },
			onNextChapter = { viewModel.switchChapterBy(1) },
			onPrevPage = { scrollController.scrollByPages(-1) },
			onNextPage = { scrollController.scrollByPages(1) },
			onPageSeek = { page -> scrollController.scrollToChapterPage(page) },
			onMenu = onOpenMenu,
			onComments = onComments,
			modifier = Modifier
				.align(Alignment.BottomCenter)
				.navigationBarsPadding(),
		)
	}

	LaunchedEffect(content.state) {
		content.state?.let { viewModel.saveCurrentState(it) }
	}
}

private fun indexOfState(
	pages: List<ReaderPage>,
	state: ReaderState?,
): Int {
	if (state == null || pages.isEmpty()) return 0
	val index = pages.indexOfFirst { it.chapterId == state.chapterId && it.index == state.page }
	return if (index >= 0) index else 0
}
