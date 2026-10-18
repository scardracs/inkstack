package org.inkstack.reader.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import kotlinx.coroutines.flow.distinctUntilChanged
import org.inkstack.core.exceptions.resolve.ExceptionResolver
import org.inkstack.core.os.NetworkState
import org.inkstack.core.prefs.ReaderMode
import org.inkstack.reader.domain.PageLoader
import org.inkstack.reader.ui.config.ReaderSettings
import org.inkstack.reader.ui.pager.ReaderPage

@Composable
fun ReaderPager(
	pages: List<ReaderPage>,
	initialIndex: Int,
	mode: ReaderMode,
	doublePage: Boolean,
	webtoonGaps: Boolean,
	loader: PageLoader,
	settingsProducer: ReaderSettings.Producer,
	networkState: NetworkState,
	exceptionResolver: ExceptionResolver,
	imageLoader: ImageLoader,
	onPageRangeChanged: (lower: Int, upper: Int) -> Unit,
	onTap: () -> Unit,
	pagerStateHolder: (PagerState) -> Unit,
	modifier: Modifier = Modifier,
) {
	val safeIndex = initialIndex.coerceIn(0, (pages.size - 1).coerceAtLeast(0))
	when {
		mode == ReaderMode.WEBTOON -> {
			WebtoonPager(
				pages = pages,
				initialIndex = safeIndex,
				gaps = webtoonGaps,
				loader = loader,
				settingsProducer = settingsProducer,
				networkState = networkState,
				exceptionResolver = exceptionResolver,
				imageLoader = imageLoader,
				onPageRangeChanged = onPageRangeChanged,
				onTap = onTap,
				modifier = modifier,
			)
		}
		mode == ReaderMode.VERTICAL -> {
			val pagerState = rememberPagerState(initialPage = safeIndex) { pages.size }
			pagerStateHolder(pagerState)
			LaunchedEffect(pagerState) {
				snapshotFlow { pagerState.currentPage }
					.distinctUntilChanged()
					.collect { onPageRangeChanged(it, it) }
			}
			VerticalPager(
				state = pagerState,
				modifier = modifier.fillMaxSize(),
				beyondViewportPageCount = 1,
			) { index ->
				ReaderPageImage(
					page = pages[index],
					loader = loader,
					settingsProducer = settingsProducer,
					networkState = networkState,
					exceptionResolver = exceptionResolver,
					imageLoader = imageLoader,
					isWebtoon = false,
					gesturesEnabled = true,
					onClick = onTap,
					modifier = Modifier.fillMaxSize(),
				)
			}
		}
		doublePage && (mode == ReaderMode.STANDARD || mode == ReaderMode.REVERSED) -> {
			DoublePagePager(
				pages = pages,
				initialIndex = safeIndex,
				reversed = mode == ReaderMode.REVERSED,
				loader = loader,
				settingsProducer = settingsProducer,
				networkState = networkState,
				exceptionResolver = exceptionResolver,
				imageLoader = imageLoader,
				onPageRangeChanged = onPageRangeChanged,
				onTap = onTap,
				pagerStateHolder = pagerStateHolder,
				rootModifier = modifier,
			)
		}
		else -> {
			val pageCount = pages.size
			val start = if (mode == ReaderMode.REVERSED) {
				(pageCount - 1 - safeIndex).coerceAtLeast(0)
			} else {
				safeIndex
			}
			val pagerState = rememberPagerState(initialPage = start) { pageCount }
			pagerStateHolder(pagerState)
			LaunchedEffect(pagerState, mode, pageCount) {
				snapshotFlow { pagerState.currentPage }
					.distinctUntilChanged()
					.collect { raw ->
						val index = if (mode == ReaderMode.REVERSED) pageCount - 1 - raw else raw
						onPageRangeChanged(index, index)
					}
			}
			HorizontalPager(
				state = pagerState,
				modifier = modifier.fillMaxSize(),
				beyondViewportPageCount = 1,
				reverseLayout = mode == ReaderMode.REVERSED,
			) { raw ->
				val index = if (mode == ReaderMode.REVERSED) pageCount - 1 - raw else raw
				ReaderPageImage(
					page = pages[index],
					loader = loader,
					settingsProducer = settingsProducer,
					networkState = networkState,
					exceptionResolver = exceptionResolver,
					imageLoader = imageLoader,
					isWebtoon = false,
					gesturesEnabled = true,
					onClick = onTap,
					modifier = Modifier.fillMaxSize(),
				)
			}
		}
	}
}

@Composable
private fun WebtoonPager(
	pages: List<ReaderPage>,
	initialIndex: Int,
	gaps: Boolean,
	loader: PageLoader,
	settingsProducer: ReaderSettings.Producer,
	networkState: NetworkState,
	exceptionResolver: ExceptionResolver,
	imageLoader: ImageLoader,
	onPageRangeChanged: (Int, Int) -> Unit,
	onTap: () -> Unit,
	modifier: Modifier,
) {
	val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
	LaunchedEffect(listState) {
		snapshotFlow {
			val info = listState.layoutInfo
			val visible = info.visibleItemsInfo
			if (visible.isEmpty()) 0 to 0
			else visible.first().index to visible.last().index
		}.distinctUntilChanged().collect { (lo, hi) ->
			onPageRangeChanged(lo, hi)
		}
	}
	LazyColumn(
		state = listState,
		modifier = modifier.fillMaxSize(),
		verticalArrangement = if (gaps) Arrangement.spacedBy(8.dp) else Arrangement.spacedBy(0.dp),
	) {
		itemsIndexed(pages, key = { _, p -> p.id }) { _, page ->
			ReaderPageImage(
				page = page,
				loader = loader,
				settingsProducer = settingsProducer,
				networkState = networkState,
				exceptionResolver = exceptionResolver,
				imageLoader = imageLoader,
				isWebtoon = true,
				gesturesEnabled = true,
				onClick = onTap,
				modifier = Modifier.fillMaxWidth(),
			)
		}
	}
}

@Composable
private fun DoublePagePager(
	pages: List<ReaderPage>,
	initialIndex: Int,
	reversed: Boolean,
	loader: PageLoader,
	settingsProducer: ReaderSettings.Producer,
	networkState: NetworkState,
	exceptionResolver: ExceptionResolver,
	imageLoader: ImageLoader,
	onPageRangeChanged: (Int, Int) -> Unit,
	onTap: () -> Unit,
	pagerStateHolder: (PagerState) -> Unit,
	rootModifier: Modifier,
) {
	val spreads = ((pages.size + 1) / 2).coerceAtLeast(1)
	val startSpread = (initialIndex / 2).coerceIn(0, (spreads - 1).coerceAtLeast(0))
	val pagerState = rememberPagerState(
		initialPage = if (reversed) spreads - 1 - startSpread else startSpread,
	) { spreads }
	pagerStateHolder(pagerState)
	LaunchedEffect(pagerState, reversed, spreads) {
		snapshotFlow { pagerState.currentPage }
			.distinctUntilChanged()
			.collect { raw ->
				val spread = if (reversed) spreads - 1 - raw else raw
				val left = spread * 2
				val right = (left + 1).coerceAtMost(pages.lastIndex)
				onPageRangeChanged(left, right)
			}
	}
	HorizontalPager(
		state = pagerState,
		modifier = rootModifier.fillMaxSize(),
		beyondViewportPageCount = 1,
		reverseLayout = reversed,
	) { raw ->
		val spread = if (reversed) spreads - 1 - raw else raw
		val left = spread * 2
		val right = left + 1
		androidx.compose.foundation.layout.Row(Modifier.fillMaxSize()) {
			if (left in pages.indices) {
				ReaderPageImage(
					page = pages[left],
					loader = loader,
					settingsProducer = settingsProducer,
					networkState = networkState,
					exceptionResolver = exceptionResolver,
					imageLoader = imageLoader,
					isWebtoon = false,
					gesturesEnabled = true,
					onClick = onTap,
					modifier = Modifier.fillMaxHeight().weight(1f),
				)
			}
			if (right in pages.indices) {
				ReaderPageImage(
					page = pages[right],
					loader = loader,
					settingsProducer = settingsProducer,
					networkState = networkState,
					exceptionResolver = exceptionResolver,
					imageLoader = imageLoader,
					isWebtoon = false,
					gesturesEnabled = true,
					onClick = onTap,
					modifier = Modifier.fillMaxHeight().weight(1f),
				)
			} else {
				androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
			}
		}
	}
}
