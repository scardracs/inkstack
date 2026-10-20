package org.inkstack.core.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest

private val CoverShape = RoundedCornerShape(16.dp)

@Composable
fun MangaCover(
	model: Any?,
	contentDescription: String?,
	modifier: Modifier = Modifier,
	imageLoader: coil3.ImageLoader? = null,
	overlay: @Composable BoxScope.() -> Unit = {},
) {
	Box(
		modifier = modifier
			.fillMaxWidth()
			.aspectRatio(2f / 3f)
			.clip(CoverShape)
			.background(MaterialTheme.colorScheme.surfaceContainer),
	) {
		val request = when (model) {
			is ImageRequest -> model
			else -> model
		}
		if (imageLoader != null) {
			AsyncImage(
				model = request,
				contentDescription = contentDescription,
				imageLoader = imageLoader,
				modifier = Modifier.fillMaxSize(),
				contentScale = ContentScale.Crop,
			)
		} else {
			AsyncImage(
				model = request,
				contentDescription = contentDescription,
				modifier = Modifier.fillMaxSize(),
				contentScale = ContentScale.Crop,
			)
		}
		overlay()
	}
}
