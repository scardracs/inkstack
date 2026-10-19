package org.inkstack.details.ui

import android.app.assist.AssistContent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.lifecycle.lifecycleScope
import coil3.ImageLoader
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.inkstack.R
import org.inkstack.core.model.isLocal
import org.inkstack.core.model.isNsfw
import org.inkstack.core.nav.AppRouter
import org.inkstack.core.nav.ReaderIntent
import org.inkstack.core.nav.router
import org.inkstack.core.os.AppShortcutManager
import org.inkstack.core.prefs.AppSettings
import org.inkstack.core.ui.BaseComposeActivity
import org.inkstack.core.ui.dialog.buildAlertDialog
import org.inkstack.core.util.ext.isHttpUrl
import org.inkstack.core.util.ext.observeEvent
import org.inkstack.core.util.ext.toUriOrNull
import org.inkstack.details.ui.compose.DetailsHost
import org.inkstack.details.ui.compose.DetailsMenuVisibility
import org.inkstack.details.ui.compose.DetailsReadMenuVisibility
import org.inkstack.download.ui.worker.DownloadStartedObserver
import org.inkstack.main.ui.owners.BottomSheetOwner
import org.inkstack.parsers.model.Manga
import org.inkstack.parsers.model.MangaChapter
import org.inkstack.reader.ui.ReaderState
import javax.inject.Inject

@AndroidEntryPoint
class DetailsActivity : BaseComposeActivity(), BottomSheetOwner {

	@Inject
	lateinit var coil: ImageLoader

	@Inject
	lateinit var appShortcutManager: AppShortcutManager

	@Inject
	lateinit var settings: AppSettings

	private val viewModel: DetailsViewModel by viewModels()

	private val overrideEditLauncher = registerForActivityResult(
		ActivityResultContracts.StartActivityForResult(),
	) { result ->
		if (result.resultCode == RESULT_OK) {
			viewModel.reload()
		}
	}

	override val bottomSheet: View?
		get() = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val appRouter = router
		viewModel.onError.observeEvent(this, DetailsErrorObserver(this, viewModel, exceptionResolver))
		viewModel.onDownloadStarted.observeEvent(this, DownloadStartedObserver(window.decorView))
		viewModel.onMangaRemoved.observeEvent(this) {
			finishAfterTransition()
		}

		setComposeContent {
			val manga by viewModel.manga.collectAsState()
			val historyInfo by viewModel.historyInfo.collectAsState()
			val remoteManga by viewModel.remoteManga.collectAsState()
			val isStatsAvailable by viewModel.isStatsAvailable.collectAsState()
			val current = manga
			val menuVisibility = remember(current, remoteManga, isStatsAvailable) {
				DetailsMenuVisibility(
					share = current != null && AppRouter.isShareSupported(current),
					download = current != null && !current.isLocal,
					delete = current?.isLocal == true,
					browser = current?.publicUrl?.isHttpUrl() == true,
					alternatives = current != null && !current.isLocal,
					replaceSource = current != null && !current.isLocal,
					shortcut = ShortcutManagerCompat.isRequestPinShortcutSupported(this),
					scrobbling = viewModel.isScrobblingAvailable,
					online = remoteManga != null,
					stats = isStatsAvailable,
					chaptersSheet = false,
				)
			}
			val readMenuVisibility = remember(historyInfo, current) {
				DetailsReadMenuVisibility(
					incognito = !historyInfo.isIncognitoMode,
					forget = historyInfo.history != null,
					download = current != null && !current.isLocal,
				)
			}
			DetailsHost(
				imageLoader = coil,
				settings = settings,
				viewModel = viewModel,
				historyInfo = historyInfo,
				menuVisibility = menuVisibility,
				readMenuVisibility = readMenuVisibility,
				onBack = { dispatchNavigateUp() },
				onRead = { m, chapter, incognito -> openReader(m, chapter, incognito) },
				onFavorite = { m -> appRouter.showFavoriteDialog(m) },
				onTagClick = { tag -> appRouter.showTagDialog(tag) },
				onSourceClick = { m -> appRouter.openList(m.source, null, null) },
				onShare = {
					viewModel.getMangaOrNull()?.let(appRouter::showShareDialog)
				},
				onDownload = {
					viewModel.getMangaOrNull()?.let { appRouter.showDownloadDialog(it, window.decorView) }
				},
				onDelete = {
					val m = viewModel.getMangaOrNull() ?: return@DetailsHost
					buildAlertDialog(this) {
						setTitle(R.string.delete_manga)
						setMessage(getString(R.string.text_delete_local_manga, m.title))
						setPositiveButton(R.string.delete) { _, _ -> viewModel.deleteLocal() }
						setNegativeButton(android.R.string.cancel, null)
					}.show()
				},
				onEditOverride = {
					val m = viewModel.getMangaOrNull() ?: return@DetailsHost
					overrideEditLauncher.launch(AppRouter.overrideEditIntent(this, m))
				},
				onScrobbling = {
					viewModel.getMangaOrNull()?.let { appRouter.showScrobblingSelectorSheet(it, null) }
				},
				onStats = {
					viewModel.getMangaOrNull()?.let(appRouter::showStatisticSheet)
				},
				onFindSimilar = {
					viewModel.getMangaOrNull()?.title?.let(appRouter::openSearch)
				},
				onAlternatives = {
					viewModel.getMangaOrNull()?.let(appRouter::openAlternatives)
				},
				onReplaceSource = {
					viewModel.getMangaOrNull()?.let { appRouter.openSourceReplacement(listOf(it)) }
				},
				onOnline = {
					remoteManga?.let(appRouter::openDetails)
				},
				onBrowser = {
					val m = viewModel.getMangaOrNull() ?: return@DetailsHost
					appRouter.openBrowser(url = m.publicUrl, source = m.source, title = m.title)
				},
				onShortcut = {
					val m = viewModel.getMangaOrNull() ?: return@DetailsHost
					lifecycleScope.launch {
						if (!appShortcutManager.requestPinShortcut(m)) {
							Snackbar.make(window.decorView, R.string.operation_not_supported, Snackbar.LENGTH_SHORT)
								.show()
						}
					}
				},
				onForgetHistory = { viewModel.removeFromHistory() },
			)
		}
	}

	override fun onProvideAssistContent(outContent: AssistContent) {
		super.onProvideAssistContent(outContent)
		viewModel.getMangaOrNull()?.publicUrl?.toUriOrNull()?.let { outContent.webUri = it }
	}

	override fun isNsfwContent(): Flow<Boolean> = viewModel.manga.map { it?.isNsfw() == true }

	private fun openReader(manga: Manga, chapter: MangaChapter?, incognito: Boolean) {
		if (viewModel.historyInfo.value.isChapterMissing) {
			Snackbar.make(window.decorView, R.string.chapter_is_missing, Snackbar.LENGTH_SHORT).show()
			return
		}
		val intent = ReaderIntent.Builder(this)
			.manga(manga)
			.branch(viewModel.selectedBranchValue)
			.state(chapter?.let { ReaderState(it.id, 0, 0) })
			.apply { if (incognito) incognito() }
			.build()
		router.openReader(intent)
		if (incognito) {
			Toast.makeText(this, R.string.incognito_mode, Toast.LENGTH_SHORT).show()
		}
	}
}
