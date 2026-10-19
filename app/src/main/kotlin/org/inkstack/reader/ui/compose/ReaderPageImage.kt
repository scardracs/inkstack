package org.inkstack.reader.ui.compose

import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.bitmapConfig
import coil3.request.transformations
import coil3.size.Size
import coil3.transform.Transformation
import me.saket.telephoto.zoomable.EnabledZoomGestures
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState
import org.inkstack.R
import org.inkstack.core.exceptions.resolve.ExceptionResolver
import org.inkstack.core.os.NetworkState
import org.inkstack.core.util.ext.decodeRegion
import org.inkstack.core.util.ext.getDisplayMessage
import org.inkstack.core.util.ext.mangaSourceExtra
import org.inkstack.reader.domain.PageLoader
import org.inkstack.reader.ui.config.ReaderSettings
import org.inkstack.reader.ui.pager.ReaderPage
import org.inkstack.reader.ui.pager.vm.PageState
import org.inkstack.reader.ui.pager.vm.PageViewModel

private const val DEFAULT_ASPECT = 1f / 1.6f

@Composable
fun ReaderPageImage(
	page: ReaderPage,
	loader: PageLoader,
	settingsProducer: ReaderSettings.Producer,
	networkState: NetworkState,
	exceptionResolver: ExceptionResolver,
	imageLoader: ImageLoader,
	knownAspectRatio: Float?,
	onAspectRatio: (Float) -> Unit,
	zoomEnabled: Boolean = false,
	modifier: Modifier = Modifier,
	onClick: (() -> Unit)? = null,
	onTapAt: ((xFraction: Float, yFraction: Float) -> Unit)? = null,
) {
	val pageVm = remember(page.id) {
		PageViewModel(loader, settingsProducer, networkState, exceptionResolver, isWebtoon = true)
	}
	DisposableEffect(pageVm, page.id) {
		pageVm.onBind(page.toMangaPage())
		onDispose { pageVm.onRecycle() }
	}
	val state by pageVm.state.collectAsState()
	val settings by settingsProducer.collectAsState()
	val colorFilter = remember(settings.colorFilter) { settings.composeColorFilter() }

	val aspect = knownAspectRatio
		?: stateAspect(state)
		?: DEFAULT_ASPECT

	LaunchedEffect(state) {
		stateAspect(state)?.let(onAspectRatio)
	}

	Box(
		modifier = modifier
			.fillMaxWidth()
			.aspectRatio(aspect)
			.background(MaterialTheme.colorScheme.surface)
			.then(
				if (onTapAt != null) {
					Modifier.pointerInput(onTapAt) {
						detectTapGestures { offset ->
							val w = size.width.coerceAtLeast(1)
							val h = size.height.coerceAtLeast(1)
							onTapAt(offset.x / w, offset.y / h)
						}
					}
				} else if (onClick != null) {
					Modifier.pointerInput(onClick) {
						detectTapGestures { onClick() }
					}
				} else {
					Modifier
				},
			),
		contentAlignment = Alignment.Center,
	) {
		when (val s = state) {
			is PageState.Empty,
			is PageState.Loading,
			is PageState.Converting -> {
				val preview = (s as? PageState.Loading)?.preview
				if (preview != null) {
					AsyncImage(
						model = preview,
						contentDescription = null,
						imageLoader = imageLoader,
						modifier = Modifier.fillMaxWidth(),
						contentScale = ContentScale.FillWidth,
						colorFilter = colorFilter,
					)
				}
				CircularProgressIndicator()
				val progress = (s as? PageState.Loading)?.progress ?: -1
				if (progress in 0..99) {
					Text(
						text = "$progress%",
						modifier = Modifier
							.align(Alignment.BottomCenter)
							.padding(16.dp),
						style = MaterialTheme.typography.labelLarge,
					)
				}
			}

			is PageState.Loaded,
			is PageState.Shown -> {
				val uri = when (s) {
					is PageState.Loaded -> s.uri
					is PageState.Shown -> s.uri
				}
				val crop = when (s) {
					is PageState.Loaded -> s.crop
					is PageState.Shown -> s.crop
				}
				val model = coilModel(uri, page, crop, settings.bitmapConfig)
				if (zoomEnabled) {
					val zoomableState = rememberZoomableState(
						zoomSpec = ZoomSpec(maxZoomFactor = 5f),
					)
					val imageState = rememberZoomableImageState(zoomableState)
					ZoomableAsyncImage(
						model = model,
						contentDescription = null,
						imageLoader = imageLoader,
						state = imageState,
						modifier = Modifier.fillMaxSize(),
						contentScale = ContentScale.FillWidth,
						gestures = EnabledZoomGestures.ZoomAndPan,
						colorFilter = colorFilter,
						onClick = onClick?.let { { _ -> it() } },
					)
					LaunchedEffect(imageState.isImageDisplayed) {
						if (imageState.isImageDisplayed) pageVm.onImageShown()
					}
				} else {
					AsyncImage(
						model = model,
						contentDescription = null,
						imageLoader = imageLoader,
						modifier = Modifier.fillMaxWidth(),
						contentScale = ContentScale.FillWidth,
						colorFilter = colorFilter,
					)
					LaunchedEffect(uri) {
						pageVm.onImageShown()
					}
				}
			}

			is PageState.Error -> {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(240.dp)
						.padding(24.dp),
					contentAlignment = Alignment.Center,
				) {
					Text(
						text = s.error.getDisplayMessage(LocalContext.current.resources),
						style = MaterialTheme.typography.bodyMedium,
					)
					TextButton(
						onClick = { pageVm.retry(page.toMangaPage(), isFromUser = true) },
						modifier = Modifier.align(Alignment.BottomCenter),
					) {
						Text(stringResource(R.string.try_again))
					}
				}
			}
		}
	}
}

private fun stateAspect(state: PageState): Float? {
	val (w, h) = when (state) {
		is PageState.Loaded -> state.width to state.height
		is PageState.Shown -> state.width to state.height
		else -> return null
	}
	return if (w > 0 && h > 0) w.toFloat() / h.toFloat() else null
}

@Composable
private fun coilModel(
	uri: Uri,
	page: ReaderPage,
	crop: Rect?,
	bitmapConfig: Bitmap.Config,
): ImageRequest {
	val context = LocalContext.current
	return remember(uri, page.id, crop, bitmapConfig) {
		ImageRequest.Builder(context)
			.data(uri)
			.mangaSourceExtra(page.source)
			.bitmapConfig(bitmapConfig)
			.decodeRegion()
			.apply {
				if (crop != null && crop.width() > 0 && crop.height() > 0) {
					transformations(CropRectTransformation(crop))
				}
			}
			.build()
	}
}

private class CropRectTransformation(
	private val rect: Rect,
) : Transformation() {

	override val cacheKey: String = "crop-${rect.flattenToString()}"

	override suspend fun transform(input: Bitmap, size: Size): Bitmap {
		val left = rect.left.coerceIn(0, input.width - 1)
		val top = rect.top.coerceIn(0, input.height - 1)
		val width = rect.width().coerceAtMost(input.width - left).coerceAtLeast(1)
		val height = rect.height().coerceAtMost(input.height - top).coerceAtLeast(1)
		return Bitmap.createBitmap(input, left, top, width, height)
	}
}
