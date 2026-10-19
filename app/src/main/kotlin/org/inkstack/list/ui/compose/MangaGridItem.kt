package org.inkstack.list.ui.compose

import android.view.LayoutInflater
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.ImageLoader
import coil3.request.ImageRequest
import org.inkstack.R
import org.inkstack.core.ui.compose.MangaCover
import org.inkstack.core.util.ext.mangaExtra
import org.inkstack.history.ui.util.ReadingProgressView
import org.inkstack.list.domain.ReadingProgress
import org.inkstack.list.ui.model.MangaDetailedListModel
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
	val progress = item.readingProgress()
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
			ReadingProgressBadge(
				progress = progress,
				modifier = Modifier
					.align(Alignment.TopEnd)
					.padding(dimensionResource(R.dimen.card_indicator_offset)),
			)
			val status = item.statusBadges()
			Row(
				modifier = Modifier
					.align(Alignment.TopStart)
					.padding(dimensionResource(R.dimen.card_indicator_offset)),
				horizontalArrangement = Arrangement.spacedBy(4.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				if (item.counter > 0) {
					Box(
						modifier = Modifier
							.size(dimensionResource(R.dimen.card_indicator_size_glyph))
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
				if (status.isFavorite) {
					StatusGlyph(Icons.Filled.Favorite, Color(0xFFE91E63))
				}
				if (status.isPinned) {
					StatusGlyph(Icons.Filled.PushPin, MaterialTheme.colorScheme.onSurface)
				}
				if (status.isSaved) {
					StatusGlyph(Icons.Filled.Storage, MaterialTheme.colorScheme.onSurface)
				}
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
private fun ReadingProgressBadge(
	progress: ReadingProgress?,
	modifier: Modifier = Modifier,
) {
	if (progress == null || !progress.isValid()) return
	AndroidView(
		factory = { context ->
			LayoutInflater.from(context)
				.inflate(R.layout.view_reading_progress, null, false) as ReadingProgressView
		},
		update = { view ->
			view.setProgress(progress, animate = false)
		},
		modifier = modifier.size(dimensionResource(R.dimen.card_indicator_size)),
	)
}

@Composable
private fun StatusGlyph(
	icon: androidx.compose.ui.graphics.vector.ImageVector,
	tint: Color,
) {
	Box(
		modifier = Modifier
			.size(dimensionResource(R.dimen.card_indicator_size_glyph))
			.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.82f), CircleShape),
		contentAlignment = Alignment.Center,
	) {
		Icon(
			imageVector = icon,
			contentDescription = null,
			tint = tint,
			modifier = Modifier.size(14.dp),
		)
	}
}

private data class StatusBadges(
	val isFavorite: Boolean = false,
	val isPinned: Boolean = false,
	val isSaved: Boolean = false,
)

private fun MangaListModel.readingProgress(): ReadingProgress? = when (this) {
	is MangaGridModel -> progress
	is MangaDetailedListModel -> progress
	else -> null
}

private fun MangaListModel.statusBadges(): StatusBadges = when (this) {
	is MangaGridModel -> StatusBadges(isFavorite, isPinned, isSaved)
	is MangaDetailedListModel -> StatusBadges(isFavorite, isPinned, isSaved)
	else -> StatusBadges()
}
