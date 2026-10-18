package org.inkstack.reader.ui.compose

import android.graphics.Rect
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState
import org.inkstack.R
import org.inkstack.core.exceptions.resolve.ExceptionResolver
import org.inkstack.core.model.ZoomMode
import org.inkstack.core.os.NetworkState
import org.inkstack.core.util.ext.getDisplayMessage
import org.inkstack.core.util.ext.mangaSourceExtra
import org.inkstack.reader.domain.PageLoader
import org.inkstack.reader.ui.config.ReaderSettings
import org.inkstack.reader.ui.pager.ReaderPage
import org.inkstack.reader.ui.pager.vm.PageState
import org.inkstack.reader.ui.pager.vm.PageViewModel

@Composable
fun ReaderPageImage(
	page: ReaderPage,
	loader: PageLoader,
	settingsProducer: ReaderSettings.Producer,
	networkState: NetworkState,
	exceptionResolver: ExceptionResolver,
	imageLoader: ImageLoader,
	isWebtoon: Boolean,
	gesturesEnabled: Boolean,
	modifier: Modifier = Modifier,
	onClick: (() -> Unit)? = null,
) {
	val pageVm = remember(page.id, isWebtoon) {
		PageViewModel(loader, settingsProducer, networkState, exceptionResolver, isWebtoon)
	}
	DisposableEffect(pageVm, page.id) {
		pageVm.onBind(page.toMangaPage())
		onDispose { pageVm.onRecycle() }
	}
	val state by pageVm.state.collectAsState()
	val settings by settingsProducer.collectAsState()
	val colorFilter = remember(settings.colorFilter) { settings.composeColorFilter() }
	val (contentScale, alignment) = zoomModeToScale(settings.zoomMode)

	Box(
		modifier = modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.surface),
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
						modifier = Modifier.fillMaxSize(),
						contentScale = ContentScale.Fit,
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
					else -> error("unreachable")
				}
				val crop = when (s) {
					is PageState.Loaded -> s.crop
					is PageState.Shown -> s.crop
					else -> null
				}
				val zoomableState = rememberZoomableState(
					zoomSpec = ZoomSpec(maxZoomFactor = 5f),
				)
				val imageState = rememberZoomableImageState(zoomableState)
				ZoomableAsyncImage(
					model = coilModel(uri, page, imageLoader, crop),
					contentDescription = null,
					imageLoader = imageLoader,
					state = imageState,
					modifier = Modifier.fillMaxSize(),
					contentScale = contentScale,
					alignment = alignment,
					gesturesEnabled = gesturesEnabled,
					colorFilter = colorFilter,
					onClick = onClick?.let { { _ -> it() } },
				)
				LaunchedEffect(imageState.isImageDisplayed) {
					if (imageState.isImageDisplayed) {
						pageVm.onImageShown()
					}
				}
			}

			is PageState.Error -> {
				Box(
					modifier = Modifier
						.fillMaxWidth()
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

@Composable
private fun coilModel(
	uri: Uri,
	page: ReaderPage,
	imageLoader: ImageLoader,
	crop: Rect?,
): ImageRequest {
	val context = LocalContext.current
	return remember(uri, page.id, crop) {
		ImageRequest.Builder(context)
			.data(uri)
			.mangaSourceExtra(page.source)
			.build()
	}
}

private fun zoomModeToScale(mode: ZoomMode): Pair<ContentScale, Alignment> = when (mode) {
	ZoomMode.FIT_CENTER -> ContentScale.Fit to Alignment.Center
	ZoomMode.FIT_WIDTH -> ContentScale.FillWidth to Alignment.TopCenter
	ZoomMode.FIT_HEIGHT -> ContentScale.FillHeight to Alignment.Center
	ZoomMode.KEEP_START -> ContentScale.Fit to Alignment.TopStart
}
