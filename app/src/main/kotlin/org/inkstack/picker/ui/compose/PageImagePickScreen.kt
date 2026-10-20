package org.inkstack.picker.ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.ImageLoader
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest
import org.inkstack.R
import org.inkstack.core.ui.compose.InkstackScaffold
import org.inkstack.details.ui.pager.pages.PageThumbnail
import org.inkstack.list.ui.compose.MangaListPane
import org.inkstack.list.ui.model.ListHeader
import org.inkstack.parsers.model.Manga
import org.inkstack.picker.ui.PageImagePickViewModel
import org.inkstack.picker.ui.manga.MangaPickerViewModel
import org.inkstack.picker.ui.page.PagePickerViewModel
import org.inkstack.reader.ui.PageSaveHelper
import org.inkstack.reader.ui.pager.ReaderPage
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageImagePickScreen(
	mangaId: Long?,
	onBack: () -> Unit,
	onMangaPicked: (Manga) -> Unit,
	onFileReady: (File) -> Unit,
	pageSaveHelper: PageSaveHelper,
	imageLoader: ImageLoader?,
	modifier: Modifier = Modifier,
	pickViewModel: PageImagePickViewModel = hiltViewModel(),
) {
	val isLoading by pickViewModel.isLoading.collectAsState()

	LaunchedEffect(pickViewModel) {
		pickViewModel.onFileReady.collectLatest { event ->
			event?.consume { onFileReady(it) }
		}
	}

	InkstackScaffold(
		modifier = modifier,
		topBar = {
			TopAppBar(
				title = { Text(stringResource(R.string.pick_manga_page)) },
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
					}
				},
				windowInsets = WindowInsets.statusBars,
				colors = TopAppBarDefaults.topAppBarColors(),
			)
		},
	) { padding ->
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding),
		) {
			when {
				isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
				mangaId == null -> {
					val mangaVm: MangaPickerViewModel = hiltViewModel()
					val content by mangaVm.content.collectAsState()
					MangaListPane(
						content = content,
						onMangaClick = onMangaPicked,
						modifier = Modifier.fillMaxSize(),
						imageLoader = imageLoader,
					)
				}
				else -> {
					PagePickerPane(
						mangaId = mangaId,
						onPageClick = { manga, page ->
							pickViewModel.savePageToTempFile(
								pageSaveHelper,
								PageSaveHelper.Task(
									manga = manga,
									chapterId = page.chapterId,
									pageNumber = page.index + 1,
									page = page.toMangaPage(),
								),
							)
						},
					)
				}
			}
		}
	}
}

@Composable
private fun PagePickerPane(
	mangaId: Long,
	onPageClick: (Manga, ReaderPage) -> Unit,
) {
	val context = LocalContext.current
	// Activity puts KEY_ID on the intent before selectedMangaId updates, so the
	// Activity default CreationExtras already carry the manga id for SavedStateHandle.
	val viewModel: PagePickerViewModel = hiltViewModel(key = "pages-$mangaId")
	val thumbnails by viewModel.thumbnails.collectAsState()
	val mangaDetails by viewModel.manga.collectAsState()
	val manga = mangaDetails?.toManga()

	LazyVerticalGrid(
		columns = GridCells.Adaptive(96.dp),
		modifier = Modifier.fillMaxSize(),
		contentPadding = PaddingValues(8.dp),
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp),
	) {
		items(
			items = thumbnails,
			key = {
				when (it) {
					is PageThumbnail -> "${it.page.chapterId}:${it.page.index}"
					is ListHeader -> "h:${it.hashCode()}"
					else -> it.hashCode()
				}
			},
			span = {
				if (it is ListHeader) GridItemSpan(maxLineSpan) else GridItemSpan(1)
			},
		) { item ->
			when (item) {
				is ListHeader -> {
					ListItem(
						headlineContent = { Text(item.getText(context)?.toString().orEmpty()) },
						modifier = Modifier.fillMaxWidth(),
					)
				}
				is PageThumbnail -> {
					AsyncImage(
						model = item.page.preview,
						contentDescription = null,
						modifier = Modifier
							.aspectRatio(2f / 3f)
							.clickable {
								val m = manga ?: return@clickable
								onPageClick(m, item.page)
							},
						contentScale = ContentScale.Crop,
					)
				}
			}
		}
	}
}
