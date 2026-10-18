package org.inkstack.reader.ui

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil3.ImageLoader
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.inkstack.R
import org.inkstack.core.nav.AppRouter
import org.inkstack.core.nav.router
import org.inkstack.core.os.NetworkState
import org.inkstack.core.prefs.AppSettings
import org.inkstack.core.ui.BaseComposeFullscreenActivity
import org.inkstack.core.ui.dialog.buildAlertDialog
import org.inkstack.core.ui.dialog.setCheckbox
import org.inkstack.core.util.ext.observeEvent
import org.inkstack.reader.data.TapGridSettings
import org.inkstack.reader.domain.PageLoader
import org.inkstack.reader.ui.compose.ReaderScreen
import org.inkstack.reader.ui.compose.ReaderScrollController
import org.inkstack.reader.ui.config.ReaderConfigSheet
import org.inkstack.sourcescore.ui.CommunityCommentsSheet
import org.inkstack.sourcescore.ui.CommunityRatingDelegate
import javax.inject.Inject

@AndroidEntryPoint
class ReaderActivity :
	BaseComposeFullscreenActivity(),
	ReaderConfigSheet.Callback,
	ReaderControlDelegate.OnInteractionListener {

	@Inject lateinit var communityRating: CommunityRatingDelegate
	@Inject lateinit var settings: AppSettings
	@Inject lateinit var tapGridSettings: TapGridSettings
	@Inject lateinit var pageLoader: PageLoader
	@Inject lateinit var networkState: NetworkState
	@Inject lateinit var imageLoader: ImageLoader
	@Inject lateinit var pageSaveHelperFactory: PageSaveHelper.Factory
	@Inject lateinit var scrollTimerFactory: ScrollTimer.Factory
	@Inject lateinit var screenOrientationHelper: ScreenOrientationHelper

	private val viewModel: ReaderViewModel by viewModels()
	private lateinit var pageSaveHelper: PageSaveHelper
	private lateinit var scrollTimer: ScrollTimer
	private lateinit var controlDelegate: ReaderControlDelegate

	private var uiVisible by mutableStateOf(true)
	private var scrollController: ReaderScrollController? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		pageSaveHelper = pageSaveHelperFactory.create(this)
		scrollTimer = scrollTimerFactory.create(resources, this, this)
		controlDelegate = ReaderControlDelegate(resources, settings, tapGridSettings, this)
		screenOrientationHelper.applySettings()

		viewModel.onAskNsfwIncognito.observeEvent(this) { askForIncognitoMode() }
		viewModel.onShowToast.observeEvent(this) { msgId ->
			window.decorView.let { Snackbar.make(it, msgId, Snackbar.LENGTH_SHORT).show() }
		}

		lifecycleScope.launch {
			repeatOnLifecycle(Lifecycle.State.STARTED) {
				viewModel.readerMode.collect { mode ->
					if (mode != null && uiVisible) {
						window.decorView.postDelayed({
							if (uiVisible) setUiIsVisible(false)
						}, 1000L)
					}
				}
			}
		}

		setComposeContent {
			ReaderScreen(
				viewModel = viewModel,
				pageLoader = pageLoader,
				networkState = networkState,
				exceptionResolver = exceptionResolver,
				imageLoader = imageLoader,
				onBack = { dispatchNavigateUp() },
				onOpenMenu = { openMenu() },
				onComments = { onCommentsClick() },
				systemUiVisible = uiVisible,
				onToggleUi = { toggleUiVisibility() },
				onScrollController = { scrollController = it },
			)
		}
	}

	override fun getParentActivityIntent(): Intent? {
		val manga = viewModel.getMangaOrNull() ?: return null
		return AppRouter.detailsIntent(this, manga)
	}

	override fun onPause() {
		super.onPause()
		viewModel.onPause()
	}

	override fun onStop() {
		super.onStop()
		viewModel.onStop()
	}

	override fun isNsfwContent(): Flow<Boolean> = viewModel.isMangaNsfw

	override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
		return controlDelegate.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
	}

	override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
		return controlDelegate.onKeyUp(keyCode, event) || super.onKeyUp(keyCode, event)
	}

	override fun switchPageBy(delta: Int) {
		scrollController?.scrollByPages(delta) ?: run {
			val state = viewModel.getCurrentState() ?: return
			val pages = viewModel.content.value.pages
			val idx = pages.indexOfFirst { it.chapterId == state.chapterId && it.index == state.page }
			val next = idx + delta
			when {
				next in pages.indices -> viewModel.onCurrentPageChanged(next, next)
				delta > 0 -> viewModel.switchChapterBy(1)
				delta < 0 -> viewModel.switchChapterBy(-1)
			}
		}
	}

	override fun switchPageTo(index: Int) {
		scrollController?.scrollToChapterPage(index)
	}

	override fun switchChapterBy(delta: Int) = viewModel.switchChapterBy(delta)

	override fun scrollBy(delta: Int, smooth: Boolean): Boolean =
		scrollController?.scrollPixels(delta, smooth) == true

	override fun toggleUiVisibility() = setUiIsVisible(!uiVisible)

	override fun onBookmarkClick() = viewModel.toggleBookmark()

	override fun onCommentsClick() {
		if (!communityRating.isAvailable) {
			Snackbar.make(window.decorView, R.string.community_comments_unavailable, Snackbar.LENGTH_SHORT).show()
			return
		}
		val manga = viewModel.getMangaOrNull() ?: return
		val localId = viewModel.getCurrentState()?.chapterId
		val number = manga.chapters?.firstOrNull { it.id == localId }?.number
		val chapterId = number?.let { communityRating.chapterKey(it) }
		lifecycleScope.launch {
			val workId = communityRating.workId(manga)
			if (workId == null) {
				Snackbar.make(window.decorView, R.string.community_comments_unavailable, Snackbar.LENGTH_SHORT).show()
			} else {
				CommunityCommentsSheet.show(supportFragmentManager, workId, chapterId)
			}
		}
	}

	override fun openMenu() {
		viewModel.saveCurrentState()
		router.showReaderConfigSheet()
	}

	override fun onSavePageClick() = viewModel.saveCurrentPage(pageSaveHelper)

	override fun onScrollTimerClick(isLongClick: Boolean) {
		if (isLongClick) {
			scrollTimer.setActive(!scrollTimer.isActive.value)
		}
	}

	override fun toggleScreenOrientation() {
		screenOrientationHelper.toggleScreenOrientation()
	}

	override fun isReaderResumed(): Boolean = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)

	private fun setUiIsVisible(visible: Boolean) {
		uiVisible = visible
		val fullscreen = settings.isReaderFullscreenEnabled
		systemUiController.setSystemUiVisible(visible || !fullscreen)
	}

	private fun askForIncognitoMode() {
		buildAlertDialog(this, isCentered = true) {
			var dontAskAgain = false
			val listener = DialogInterface.OnClickListener { _, which ->
				if (which == DialogInterface.BUTTON_NEUTRAL) {
					finishAfterTransition()
				} else {
					viewModel.setIncognitoMode(which == DialogInterface.BUTTON_POSITIVE, dontAskAgain)
				}
			}
			setCheckbox(R.string.dont_ask_again, dontAskAgain) { _, isChecked ->
				dontAskAgain = isChecked
			}
			setIcon(R.drawable.ic_incognito)
			setTitle(R.string.incognito_mode)
			setMessage(R.string.incognito_mode_hint_nsfw)
			setPositiveButton(R.string.incognito, listener)
			setNegativeButton(R.string.disable, listener)
			setNeutralButton(android.R.string.cancel, listener)
			setOnCancelListener { finishAfterTransition() }
			setCancelable(true)
		}.show()
	}
}
