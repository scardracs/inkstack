package org.inkstack.reader.ui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.inkstack.reader.ui.pager.ReaderUiState

@OptIn(ExperimentalMaterial3Api::class)
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
		modifier = modifier,
	) {
		TopAppBar(
			title = {
				Column {
					Text(
						text = uiState?.mangaName.orEmpty(),
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
						style = MaterialTheme.typography.titleMedium,
					)
					Text(
						text = chapterTitle,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
						style = MaterialTheme.typography.bodySmall,
					)
				}
			},
			navigationIcon = {
				IconButton(onClick = onBack) {
					Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
				}
			},
			colors = TopAppBarDefaults.topAppBarColors(
				containerColor = MaterialTheme.colorScheme.surface,
			),
			modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
		)
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
		modifier = modifier,
	) {
		val total = (uiState?.totalPages ?: 1).coerceAtLeast(1)
		val current = (uiState?.currentPage ?: 0).coerceIn(0, total - 1)
		val sliderValue = if (reversed) (total - 1 - current).toFloat() else current.toFloat()
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.background(MaterialTheme.colorScheme.surfaceContainer)
				.padding(horizontal = 4.dp, vertical = 4.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween,
		) {
			IconButton(onClick = onPrevChapter) {
				Icon(Icons.Default.FastRewind, contentDescription = null)
			}
			IconButton(onClick = onPrevPage) {
				Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = null)
			}
			Slider(
				value = sliderValue,
				onValueChange = { v ->
					val page = if (reversed) total - 1 - v.toInt() else v.toInt()
					onPageSeek(page)
				},
				valueRange = 0f..(total - 1).toFloat().coerceAtLeast(0f),
				modifier = Modifier.weight(1f),
			)
			IconButton(onClick = onNextPage) {
				Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = null)
			}
			IconButton(onClick = onNextChapter) {
				Icon(Icons.Default.FastForward, contentDescription = null)
			}
			IconButton(onClick = onMenu) {
				Icon(Icons.Default.MoreVert, contentDescription = null)
			}
			IconButton(onClick = onComments) {
				Icon(Icons.Default.ChatBubbleOutline, contentDescription = null)
			}
		}
	}
}
