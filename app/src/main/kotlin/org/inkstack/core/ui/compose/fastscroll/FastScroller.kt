package org.inkstack.core.ui.compose.fastscroll

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.inkstack.R
import kotlin.math.roundToInt

private const val ScrollbarHideDelayMs = 1000L

enum class FastScrollBubbleSize {
	NORMAL,
	SMALL,
}

/**
 * Overlay matching View [org.inkstack.core.ui.list.fastscroll.FastScroller]:
 * auto-hide ~1s, optional section bubble, small/normal bubble sizes from dimens.
 */
@Composable
fun BoxScope.FastScroller(
	state: LazyListState,
	itemCount: Int,
	modifier: Modifier = Modifier,
	sectionIndexer: ((Int) -> String?)? = null,
	bubbleSize: FastScrollBubbleSize = FastScrollBubbleSize.SMALL,
	hideScrollbar: Boolean = true,
	showBubble: Boolean = true,
	enabled: Boolean = true,
) {
	FastScrollerInternal(
		itemCount = itemCount,
		scrollFraction = {
			val info = state.layoutInfo
			val total = info.totalItemsCount
			if (total <= 1) 0f else {
				val first = state.firstVisibleItemIndex
				val offset = state.firstVisibleItemScrollOffset
				val avgSize = info.visibleItemsInfo.map { it.size }.average().toFloat().coerceAtLeast(1f)
				((first * avgSize) + offset) / ((total - 1) * avgSize)
			}.coerceIn(0f, 1f)
		},
		isScrollInProgress = { state.isScrollInProgress },
		canScroll = {
			state.canScrollForward || state.canScrollBackward
		},
		scrollToFraction = { fraction ->
			val last = (itemCount - 1).coerceAtLeast(0)
			state.scrollToItem((fraction * last).roundToInt().coerceIn(0, last))
		},
		targetIndexForFraction = { fraction ->
			val last = (itemCount - 1).coerceAtLeast(0)
			(fraction * last).roundToInt().coerceIn(0, last)
		},
		sectionIndexer = sectionIndexer,
		bubbleSize = bubbleSize,
		hideScrollbar = hideScrollbar,
		showBubble = showBubble,
		enabled = enabled,
		modifier = modifier.align(Alignment.CenterEnd),
	)
}

@Composable
fun BoxScope.FastScroller(
	state: LazyGridState,
	itemCount: Int,
	modifier: Modifier = Modifier,
	sectionIndexer: ((Int) -> String?)? = null,
	bubbleSize: FastScrollBubbleSize = FastScrollBubbleSize.SMALL,
	hideScrollbar: Boolean = true,
	showBubble: Boolean = true,
	enabled: Boolean = true,
) {
	FastScrollerInternal(
		itemCount = itemCount,
		scrollFraction = {
			val info = state.layoutInfo
			val total = info.totalItemsCount
			if (total <= 1) 0f else {
				state.firstVisibleItemIndex.toFloat() / (total - 1).toFloat()
			}.coerceIn(0f, 1f)
		},
		isScrollInProgress = { state.isScrollInProgress },
		canScroll = {
			state.canScrollForward || state.canScrollBackward
		},
		scrollToFraction = { fraction ->
			val last = (itemCount - 1).coerceAtLeast(0)
			state.scrollToItem((fraction * last).roundToInt().coerceIn(0, last))
		},
		targetIndexForFraction = { fraction ->
			val last = (itemCount - 1).coerceAtLeast(0)
			(fraction * last).roundToInt().coerceIn(0, last)
		},
		sectionIndexer = sectionIndexer,
		bubbleSize = bubbleSize,
		hideScrollbar = hideScrollbar,
		showBubble = showBubble,
		enabled = enabled,
		modifier = modifier.align(Alignment.CenterEnd),
	)
}

@Composable
private fun FastScrollerInternal(
	itemCount: Int,
	scrollFraction: () -> Float,
	isScrollInProgress: () -> Boolean,
	canScroll: () -> Boolean,
	scrollToFraction: suspend (Float) -> Unit,
	targetIndexForFraction: (Float) -> Int,
	sectionIndexer: ((Int) -> String?)?,
	bubbleSize: FastScrollBubbleSize,
	hideScrollbar: Boolean,
	showBubble: Boolean,
	enabled: Boolean,
	modifier: Modifier = Modifier,
) {
	if (!enabled || itemCount <= 0) return

	val scope = rememberCoroutineScope()
	val density = LocalDensity.current
	val handleHeight = dimensionResource(R.dimen.fastscroll_handle_height)
	val handleWidth = dimensionResource(R.dimen.fastscroll_handle_width)
	val handleRadius = dimensionResource(R.dimen.fastscroll_handle_radius)
	val marginTop = dimensionResource(R.dimen.fastscroll_scrollbar_margin_top)
	val marginBottom = dimensionResource(R.dimen.fastscroll_scrollbar_margin_bottom)
	val paddingEnd = dimensionResource(R.dimen.fastscroll_scrollbar_padding_end)
	val bubbleDim = when (bubbleSize) {
		FastScrollBubbleSize.NORMAL -> dimensionResource(R.dimen.fastscroll_bubble_size)
		FastScrollBubbleSize.SMALL -> dimensionResource(R.dimen.fastscroll_bubble_size_small)
	}
	val bubbleTextSp = when (bubbleSize) {
		FastScrollBubbleSize.NORMAL -> dimensionResource(R.dimen.fastscroll_bubble_text_size).value.sp
		FastScrollBubbleSize.SMALL -> dimensionResource(R.dimen.fastscroll_bubble_text_size_small).value.sp
	}
	val bubbleCorner = when (bubbleSize) {
		FastScrollBubbleSize.NORMAL -> dimensionResource(R.dimen.fastscroll_bubble_radius)
		FastScrollBubbleSize.SMALL -> dimensionResource(R.dimen.fastscroll_bubble_radius_small)
	}

	var dragging by remember { mutableStateOf(false) }
	var visible by remember { mutableStateOf(!hideScrollbar) }
	var thumbFraction by remember { mutableFloatStateOf(0f) }
	var bubbleText by remember { mutableStateOf<String?>(null) }
	var hideJob by remember { mutableStateOf<Job?>(null) }
	val sectionIndexerState = rememberUpdatedState(sectionIndexer)

	fun scheduleHide() {
		hideJob?.cancel()
		if (!hideScrollbar || dragging) return
		hideJob = scope.launch {
			delay(ScrollbarHideDelayMs)
			if (!dragging) visible = false
		}
	}

	fun showNow() {
		hideJob?.cancel()
		if (canScroll()) visible = true
	}

	LaunchedEffect(Unit) {
		snapshotFlow { scrollFraction() to isScrollInProgress() }
			.distinctUntilChanged()
			.collect { (fraction, scrolling) ->
				if (!dragging) {
					thumbFraction = fraction
					if (scrolling) {
						showNow()
					} else {
						scheduleHide()
					}
				}
			}
	}

	val alpha by animateFloatAsState(
		targetValue = if (visible || dragging) 1f else 0f,
		animationSpec = tween(durationMillis = 200),
		label = "fastscrollAlpha",
	)
	if (alpha < 0.01f && !dragging) return

	val tertiary = MaterialTheme.colorScheme.tertiary
	val onTertiary = MaterialTheme.colorScheme.onTertiary

	BoxWithConstraints(
		modifier = modifier
			.fillMaxHeight()
			.padding(top = marginTop, bottom = marginBottom, end = paddingEnd)
			.alpha(alpha),
	) {
		val trackHeightPx = with(density) { maxHeight.toPx() }
		val handleHeightPx = with(density) { handleHeight.toPx() }
		val bubbleSizePx = with(density) { bubbleDim.toPx() }
		val travel = (trackHeightPx - handleHeightPx).coerceAtLeast(1f)
		val thumbY = (thumbFraction * travel).coerceIn(0f, travel)
		val bubbleY = (thumbY - bubbleSizePx + handleHeightPx / 2f)
			.coerceIn(0f, (trackHeightPx - bubbleSizePx - handleHeightPx / 2f).coerceAtLeast(0f))

		if (showBubble && (dragging || !bubbleText.isNullOrEmpty()) && sectionIndexer != null) {
			val text = bubbleText
			if (!text.isNullOrEmpty()) {
				Box(
					modifier = Modifier
						.align(Alignment.TopEnd)
						.offset { IntOffset(x = -with(density) { (handleWidth + 12.dp).roundToPx() }, y = bubbleY.roundToInt()) }
						.size(bubbleDim)
						.clip(
							RoundedCornerShape(
								topStart = bubbleCorner,
								topEnd = bubbleCorner,
								bottomStart = bubbleCorner,
								bottomEnd = 8.dp,
							),
						)
						.background(tertiary),
					contentAlignment = Alignment.Center,
				) {
					Text(
						text = text,
						color = onTertiary,
						fontSize = bubbleTextSp,
						textAlign = TextAlign.Center,
						maxLines = 1,
					)
				}
			}
		}

		Box(
			modifier = Modifier
				.align(Alignment.TopEnd)
				.offset { IntOffset(0, thumbY.roundToInt()) }
				.width(handleWidth)
				.height(handleHeight)
				.clip(RoundedCornerShape(handleRadius))
				.background(if (dragging) tertiary else tertiary.copy(alpha = 0.85f))
				.pointerInput(itemCount, travel) {
					detectVerticalDragGestures(
						onDragStart = {
							dragging = true
							showNow()
						},
						onDragEnd = {
							dragging = false
							bubbleText = null
							scheduleHide()
						},
						onDragCancel = {
							dragging = false
							bubbleText = null
							scheduleHide()
						},
						onVerticalDrag = { change, dragAmount ->
							change.consume()
							val nextY = (thumbFraction * travel + dragAmount).coerceIn(0f, travel)
							val fraction = (nextY / travel).coerceIn(0f, 1f)
							thumbFraction = fraction
							val index = targetIndexForFraction(fraction)
							bubbleText = sectionIndexerState.value?.invoke(index)
							scope.launch { scrollToFraction(fraction) }
						},
					)
				},
		)
	}
}
