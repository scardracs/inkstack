package org.inkstack.reader.ui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil3.ImageLoader
import org.inkstack.core.exceptions.resolve.ExceptionResolver
import org.inkstack.core.os.NetworkState
import org.inkstack.reader.domain.PageLoader
import org.inkstack.reader.ui.ReaderContent
import org.inkstack.reader.ui.ReaderState
import org.inkstack.reader.ui.ReaderViewModel

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
) {
	val content by viewModel.content.collectAsState()
	val mode by viewModel.readerMode.collectAsState()
	val uiState by viewModel.uiState.collectAsState()
	val isLoading by viewModel.isLoading.collectAsState()
	val webtoonGaps by viewModel.isWebtoonGapsEnabled.collectAsState()
	val settingsProducer = viewModel.readerSettingsProducer
	val context = LocalContext.current

	val pages = content.pages
	val initialIndex = remember(content.state, pages) {
		indexOfState(pages, content.state)
	}

	Box(modifier = Modifier.fillMaxSize()) {
		if (pages.isEmpty() || mode == null) {
			if (isLoading) {
				CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
			}
		} else {
			ReaderPager(
				pages = pages,
				initialIndex = initialIndex,
				webtoonGaps = webtoonGaps,
				loader = pageLoader,
				settingsProducer = settingsProducer,
				networkState = networkState,
				exceptionResolver = exceptionResolver,
				imageLoader = imageLoader,
				onPageRangeChanged = { lo, hi -> viewModel.onCurrentPageChanged(lo, hi) },
				onTap = onToggleUi,
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
			onPrevPage = { switchPageFallback(viewModel, content, -1) },
			onNextPage = { switchPageFallback(viewModel, content, 1) },
			onPageSeek = { page ->
				viewModel.onCurrentPageChanged(page, page)
			},
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
	pages: List<org.inkstack.reader.ui.pager.ReaderPage>,
	state: ReaderState?,
): Int {
	if (state == null || pages.isEmpty()) return 0
	val index = pages.indexOfFirst { it.chapterId == state.chapterId && it.index == state.page }
	return if (index >= 0) index else 0
}

private fun switchPageFallback(viewModel: ReaderViewModel, content: ReaderContent, delta: Int) {
	val state = content.state ?: return
	val pages = content.pages
	val current = pages.indexOfFirst { it.chapterId == state.chapterId && it.index == state.page }
	val next = (current + delta).coerceIn(0, pages.lastIndex)
	if (next != current && next in pages.indices) {
		viewModel.onCurrentPageChanged(next, next)
	} else {
		viewModel.switchChapterBy(delta)
	}
}
