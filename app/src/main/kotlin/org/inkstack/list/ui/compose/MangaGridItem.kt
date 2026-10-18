package org.inkstack.list.ui.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.request.ImageRequest
import org.inkstack.core.ui.compose.MangaCover
import org.inkstack.core.util.ext.mangaExtra
import org.inkstack.list.ui.model.MangaGridModel
import org.inkstack.list.ui.model.MangaListModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MangaGridItem(
	item: MangaListModel,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	onLongClick: (() -> Unit)? = null,
	selected: Boolean = false,
	imageLoader: ImageLoader? = null,
) {
	val context = LocalContext.current
	val request = ImageRequest.Builder(context)
		.data(item.coverUrl)
		.mangaExtra(item.manga)
		.build()
	val shape = RoundedCornerShape(16.dp)
	MangaCover(
		model = request,
		contentDescription = item.title,
		modifier = modifier
			.fillMaxWidth()
			.clip(shape)
			.then(
				if (selected) {
					Modifier.border(3.dp, MaterialTheme.colorScheme.primary, shape)
				} else {
					Modifier
				},
			)
			.combinedClickable(
				onClick = onClick,
				onLongClick = onLongClick,
			),
		imageLoader = imageLoader,
		overlay = {
			val grid = item as? MangaGridModel
			if (grid != null) {
				ProgressBadge(
					item = grid,
					modifier = Modifier
						.align(Alignment.TopEnd)
						.padding(8.dp),
				)
			}
			if (selected) {
				Box(
					modifier = Modifier
						.align(Alignment.TopStart)
						.padding(8.dp)
						.size(24.dp)
						.background(MaterialTheme.colorScheme.primary, CircleShape),
					contentAlignment = Alignment.Center,
				) {
					Icon(
						imageVector = Icons.Filled.Check,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onPrimary,
						modifier = Modifier.size(16.dp),
					)
				}
			}
			Text(
				text = item.title,
				color = MaterialTheme.colorScheme.onPrimary,
				style = MaterialTheme.typography.labelMedium,
				maxLines = 2,
				overflow = TextOverflow.Ellipsis,
				modifier = Modifier
					.align(Alignment.BottomStart)
					.padding(10.dp),
			)
		},
	)
}

@Composable
private fun ProgressBadge(
	item: MangaGridModel,
	modifier: Modifier = Modifier,
) {
	val progress = item.progress
	when {
		progress != null && progress.isCompleted() -> {
			Box(
				modifier = modifier
					.size(28.dp)
					.background(MaterialTheme.colorScheme.primary, CircleShape),
				contentAlignment = Alignment.Center,
			) {
				Icon(
					imageVector = Icons.Filled.Check,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onPrimary,
					modifier = Modifier.size(16.dp),
				)
			}
		}

		progress != null && progress.isValid() -> {
			Box(modifier = modifier.size(28.dp), contentAlignment = Alignment.Center) {
				CircularProgressIndicator(
					progress = { progress.percent.coerceIn(0f, 1f) },
					modifier = Modifier.size(28.dp),
					color = MaterialTheme.colorScheme.primary,
					trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f),
					strokeWidth = 2.5.dp,
					strokeCap = StrokeCap.Round,
				)
			}
		}

		item.counter > 0 -> {
			Box(
				modifier = modifier
					.size(28.dp)
					.background(MaterialTheme.colorScheme.error, CircleShape),
				contentAlignment = Alignment.Center,
			) {
				Text(
					text = if (item.counter > 99) "99+" else item.counter.toString(),
					color = MaterialTheme.colorScheme.onError,
					style = MaterialTheme.typography.labelSmall,
				)
			}
		}
	}
}
