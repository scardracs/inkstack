package org.inkstack.details.ui

import android.app.assist.AssistContent
import android.os.Bundle
import androidx.activity.viewModels
import coil3.ImageLoader
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import org.inkstack.core.model.isNsfw
import org.inkstack.core.nav.ReaderIntent
import org.inkstack.core.nav.router
import org.inkstack.core.ui.BaseComposeActivity
import org.inkstack.core.util.ext.observeEvent
import org.inkstack.core.util.ext.toUriOrNull
import org.inkstack.details.ui.compose.DetailsScreen
import org.inkstack.download.ui.worker.DownloadStartedObserver
import org.inkstack.parsers.model.Manga
import org.inkstack.parsers.model.MangaChapter
import org.inkstack.reader.ui.ReaderState
import javax.inject.Inject

@AndroidEntryPoint
class DetailsActivity : BaseComposeActivity() {

	@Inject
	lateinit var coil: ImageLoader

	private val viewModel: DetailsViewModel by viewModels()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val appRouter = router
		viewModel.onError
			.filterNot { appRouter.isChapterPagesSheetShown() }
			.observeEvent(this, DetailsErrorObserver(this, viewModel, exceptionResolver))
		viewModel.onDownloadStarted
			.filterNot { appRouter.isChapterPagesSheetShown() }
			.observeEvent(this, DownloadStartedObserver(window.decorView))

		setComposeContent {
			DetailsScreen(
				imageLoader = coil,
				viewModel = viewModel,
				onBack = { dispatchNavigateUp() },
				onRead = { manga, chapter -> openReader(manga, chapter) },
				onChapterClick = { chapter ->
					val manga = viewModel.getMangaOrNull() ?: return@DetailsScreen
					openReader(manga, chapter)
				},
				onFavorite = { manga -> appRouter.showFavoriteDialog(manga) },
				onTagClick = { tag -> appRouter.showTagDialog(tag) },
				onSourceClick = { manga -> appRouter.openList(manga.source, null, null) },
			)
		}
	}

	override fun onProvideAssistContent(outContent: AssistContent) {
		super.onProvideAssistContent(outContent)
		viewModel.getMangaOrNull()?.publicUrl?.toUriOrNull()?.let { outContent.webUri = it }
	}

	override fun isNsfwContent(): Flow<Boolean> = viewModel.manga.map { it?.isNsfw() == true }

	private fun openReader(manga: Manga, chapter: MangaChapter?) {
		val intent = ReaderIntent.Builder(this)
			.manga(manga)
			.branch(viewModel.selectedBranchValue)
			.state(chapter?.let { ReaderState(it.id, 0, 0) })
			.build()
		router.openReader(intent)
	}
}
