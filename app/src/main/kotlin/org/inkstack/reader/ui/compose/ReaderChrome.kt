package org.inkstack.reader.ui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.inkstack.core.ui.compose.InkstackGlassSurface
import org.inkstack.core.ui.compose.inkstackGlassContentColor
import org.inkstack.core.ui.compose.inkstackGlassContentColorMuted
import org.inkstack.reader.ui.pager.ReaderUiState

private val ReaderChromeShape = RoundedCornerShape(24.dp)
private val ReaderChromeInset = 12.dp

@Composable
fun ReaderTopBar(
	visible: Boolean,
	uiState: ReaderUiState?,
	chapterTitle: String,
	onBack: () -> Unit,
	modifier: Modifier = Modifier,
) {
	AnimatedVisibility(
		visible = visible,
		enter = fadeIn() + slideInVertically(),
		exit = fadeOut() + slideOutVertically(),
		modifier = modifier
			.fillMaxWidth()
			.windowInsetsPadding(WindowInsets.statusBars)
			.padding(horizontal = ReaderChromeInset, vertical = 8.dp),
	) {
		InkstackGlassSurface(
			modifier = Modifier.fillMaxWidth(),
			shape = ReaderChromeShape,
		) {
			val content = inkstackGlassContentColor()
			val muted = inkstackGlassContentColorMuted()
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				IconButton(
					onClick = onBack,
					colors = IconButtonDefaults.iconButtonColors(contentColor = content),
				) {
					Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = content)
				}
				Column(modifier = Modifier.weight(1f)) {
					Text(
						text = uiState?.mangaName.orEmpty(),
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
						style = MaterialTheme.typography.titleMedium,
						color = content,
					)
					Text(
						text = chapterTitle,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
						style = MaterialTheme.typography.bodySmall,
						color = muted,
					)
				}
			}
		}
	}
}

@Composable
fun ReaderBottomBar(
	visible: Boolean,
	uiState: ReaderUiState?,
	reversed: Boolean,
	onPrevChapter: () -> Unit,
	onNextChapter: () -> Unit,
	onPrevPage: () -> Unit,
	onNextPage: () -> Unit,
	onPageSeek: (Int) -> Unit,
	onMenu: () -> Unit,
	onComments: () -> Unit,
	modifier: Modifier = Modifier,
) {
	AnimatedVisibility(
		visible = visible,
		enter = fadeIn() + slideInVertically { it },
		exit = fadeOut() + slideOutVertically { it },
		modifier = modifier
			.fillMaxWidth()
			.padding(horizontal = ReaderChromeInset, vertical = 8.dp),
	) {
		val total = (uiState?.totalPages ?: 1).coerceAtLeast(1)
		val current = (uiState?.currentPage ?: 0).coerceIn(0, total - 1)
		val sliderValue = if (reversed) (total - 1 - current).toFloat() else current.toFloat()
		InkstackGlassSurface(
			modifier = Modifier.fillMaxWidth(),
			shape = ReaderChromeShape,
		) {
			// Explicit chiaro tint — do not rely on theme primary/onSurfaceVariant for controls.
			val content = inkstackGlassContentColor()
			val iconColors = IconButtonDefaults.iconButtonColors(
				contentColor = content,
				containerColor = Color.Transparent,
			)
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 4.dp, vertical = 4.dp),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.SpaceBetween,
			) {
				IconButton(onClick = onPrevChapter, colors = iconColors) {
					Icon(Icons.Filled.FastRewind, contentDescription = null, tint = content)
				}
				IconButton(onClick = onPrevPage, colors = iconColors) {
					Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = null, tint = content)
				}
				Slider(
					value = sliderValue,
					onValueChange = { v ->
						val page = if (reversed) total - 1 - v.toInt() else v.toInt()
						onPageSeek(page)
					},
					valueRange = 0f..(total - 1).toFloat().coerceAtLeast(0f),
					modifier = Modifier.weight(1f),
					colors = SliderDefaults.colors(
						thumbColor = content,
						activeTrackColor = content,
						inactiveTrackColor = content.copy(alpha = 0.40f),
						activeTickColor = content,
						inactiveTickColor = content.copy(alpha = 0.40f),
					),
				)
				IconButton(onClick = onNextPage, colors = iconColors) {
					Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = null, tint = content)
				}
				IconButton(onClick = onNextChapter, colors = iconColors) {
					Icon(Icons.Filled.FastForward, contentDescription = null, tint = content)
				}
				IconButton(onClick = onMenu, colors = iconColors) {
					Icon(Icons.Filled.MoreVert, contentDescription = null, tint = content)
				}
				IconButton(onClick = onComments, colors = iconColors) {
					Icon(Icons.Filled.ChatBubbleOutline, contentDescription = null, tint = content)
				}
			}
		}
	}
}
