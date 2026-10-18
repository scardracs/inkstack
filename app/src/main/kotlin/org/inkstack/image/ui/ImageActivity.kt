package org.inkstack.image.ui

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState
import org.inkstack.R
import coil3.memory.MemoryCache
import org.inkstack.core.image.CoilMemoryCacheKey
import org.inkstack.core.model.MangaSource
import org.inkstack.core.nav.AppRouter
import org.inkstack.core.ui.BaseComposeActivity
import org.inkstack.core.util.ShareHelper
import org.inkstack.core.util.ext.getDisplayMessage
import org.inkstack.core.util.ext.getParcelableExtraCompat
import org.inkstack.core.util.ext.isZipUri
import org.inkstack.core.util.ext.mangaSourceExtra
import org.inkstack.core.util.ext.observeEvent
import org.inkstack.core.util.ext.tryLaunch
import org.inkstack.parsers.model.MangaSource as ParserMangaSource
import javax.inject.Inject

@AndroidEntryPoint
class ImageActivity : BaseComposeActivity() {

	@Inject lateinit var coil: ImageLoader

	private val viewModel: ImageViewModel by viewModels()

	private val saveLauncher = registerForActivityResult(
		ActivityResultContracts.CreateDocument("image/png"),
	) { uri ->
		if (uri != null) viewModel.saveImage(uri)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		viewModel.onImageSaved.observeEvent(this) { uri ->
			Snackbar.make(window.decorView, R.string.page_saved, Snackbar.LENGTH_LONG)
				.setAction(R.string.share) { ShareHelper.shareImage(this, uri) }
				.show()
		}

		val dataUri = intent.data
		val previewKey = intent.getParcelableExtraCompat<CoilMemoryCacheKey>(AppRouter.KEY_PREVIEW)?.data
		val source = MangaSource(intent.getStringExtra(AppRouter.KEY_SOURCE))

		setComposeContent {
			val isLoading by viewModel.isLoading.collectAsState()
			ImageViewerScreen(
				uri = dataUri,
				previewKey = previewKey,
				source = source,
				imageLoader = coil,
				isLoading = isLoading,
				onBack = { dispatchNavigateUp() },
				onSave = ::launchSave,
			)
		}
	}

	private fun launchSave() {
		val name = intent.data?.let {
			if (it.isZipUri()) it.fragment else it.lastPathSegment
		}?.substringBeforeLast('.')?.plus(".png")
		if (name == null || !saveLauncher.tryLaunch(name)) {
			Snackbar.make(window.decorView, R.string.operation_not_supported, Snackbar.LENGTH_SHORT).show()
		}
	}
}

@Composable
private fun ImageViewerScreen(
	uri: Uri?,
	previewKey: MemoryCache.Key?,
	source: ParserMangaSource,
	imageLoader: ImageLoader,
	isLoading: Boolean,
	onBack: () -> Unit,
	onSave: () -> Unit,
) {
	val context = LocalContext.current
	var error by remember { mutableStateOf<Throwable?>(null) }
	var loading by remember { mutableStateOf(true) }
	var menuOpen by remember { mutableStateOf(false) }
	val request = remember(uri, previewKey, source) {
		ImageRequest.Builder(context)
			.data(uri)
			.memoryCacheKey(previewKey)
			.memoryCachePolicy(CachePolicy.READ_ONLY)
			.mangaSourceExtra(source)
			.listener(
				onStart = { loading = true; error = null },
				onSuccess = { _, _ -> loading = false },
				onError = { _, result -> loading = false; error = result.throwable },
			)
			.build()
	}

	Box(modifier = Modifier.fillMaxSize()) {
		val zoomableState = rememberZoomableState(zoomSpec = ZoomSpec(maxZoomFactor = 5f))
		ZoomableAsyncImage(
			model = request,
			contentDescription = null,
			imageLoader = imageLoader,
			state = rememberZoomableImageState(zoomableState),
			modifier = Modifier.fillMaxSize(),
			contentScale = ContentScale.Fit,
		)

		if (loading || isLoading) {
			CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
		}
		error?.let { e ->
			Text(
				text = e.getDisplayMessage(context.resources),
				modifier = Modifier
					.align(Alignment.Center)
					.padding(24.dp),
				style = MaterialTheme.typography.bodyMedium,
			)
		}

		IconButton(
			onClick = onBack,
			modifier = Modifier
				.align(Alignment.TopStart)
				.statusBarsPadding()
				.padding(8.dp),
		) {
			Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
		}
		Box(
			modifier = Modifier
				.align(Alignment.TopEnd)
				.statusBarsPadding()
				.padding(8.dp),
		) {
			IconButton(onClick = { menuOpen = true }) {
				Icon(Icons.Default.MoreVert, contentDescription = null)
			}
			DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
				DropdownMenuItem(
					text = { Text(stringResource(R.string.save)) },
					onClick = {
						menuOpen = false
						onSave()
					},
				)
			}
		}
	}
}
