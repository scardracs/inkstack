package org.inkstack.reader.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import kotlinx.coroutines.flow.distinctUntilChanged
import org.inkstack.core.exceptions.resolve.ExceptionResolver
import org.inkstack.core.os.NetworkState
import org.inkstack.reader.domain.PageLoader
import org.inkstack.reader.ui.config.ReaderSettings
import org.inkstack.reader.ui.pager.ReaderPage

@Composable
fun ReaderPager(
	pages: List<ReaderPage>,
	listState: LazyListState,
	webtoonGaps: Boolean,
	loader: PageLoader,
	settingsProducer: ReaderSettings.Producer,
	networkState: NetworkState,
	exceptionResolver: ExceptionResolver,
	imageLoader: ImageLoader,
	onPageRangeChanged: (lower: Int, upper: Int) -> Unit,
	onTap: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val aspectRatios = remember { mutableStateMapOf<Long, Float>() }

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
		verticalArrangement = if (webtoonGaps) Arrangement.spacedBy(8.dp) else Arrangement.spacedBy(0.dp),
		userScrollEnabled = true,
	) {
		itemsIndexed(pages, key = { _, p -> p.id }) { _, page ->
			ReaderPageImage(
				page = page,
				loader = loader,
				settingsProducer = settingsProducer,
				networkState = networkState,
				exceptionResolver = exceptionResolver,
				imageLoader = imageLoader,
				knownAspectRatio = aspectRatios[page.id],
				onAspectRatio = { ratio -> aspectRatios[page.id] = ratio },
				onClick = onTap,
				modifier = Modifier.fillMaxWidth(),
			)
		}
	}
}
