package org.inkstack.main.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withResumed
import coil3.ImageLoader
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.inkstack.backups.ui.periodical.PeriodicalBackupService
import org.inkstack.core.nav.router
import org.inkstack.core.prefs.AppSettings
import org.inkstack.core.ui.BaseComposeActivity
import org.inkstack.core.util.ext.observeEvent
import org.inkstack.core.util.ext.printStackTraceDebug
import org.inkstack.details.service.MangaPrefetchService
import org.inkstack.local.ui.LocalIndexUpdateService
import org.inkstack.local.ui.LocalStorageCleanupWorker
import org.inkstack.main.ui.compose.MainScreen
import org.inkstack.search.domain.ScreenSearchQuery
import org.inkstack.sourcescore.data.CommunitySettings
import org.inkstack.sourcescore.domain.CommunityReplyNotifier
import org.inkstack.sourcescore.ui.CommunityCommentsSheet
import org.inkstack.sourcescore.ui.CommunityOnboardingDialog
import org.inkstack.sourcescore.ui.SourceScoreSyncWorker
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseComposeActivity() {

	@Inject
	lateinit var communitySettings: CommunitySettings

	@Inject
	lateinit var sourceScoreSyncScheduler: SourceScoreSyncWorker.Scheduler

	@Inject
	lateinit var communityReplyNotifier: CommunityReplyNotifier

	@Inject
	lateinit var settings: AppSettings

	@Inject
	lateinit var screenSearchQuery: ScreenSearchQuery

	@Inject
	lateinit var imageLoader: ImageLoader

	private val viewModel by viewModels<MainViewModel>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		CommunityOnboardingDialog.showIfNeeded(this, communitySettings, supportFragmentManager)
		sourceScoreSyncScheduler.schedule()
		communityReplyNotifier.watchWhileOpen(this)
		if (savedInstanceState == null) {
			openCommentsFromNotification(intent)
			onFirstStart()
		}

		viewModel.onFirstStart.observeEvent(this) { router.showWelcomeSheet() }

		setComposeContent {
			MainScreen(
				settings = settings,
				screenSearchQuery = screenSearchQuery,
				imageLoader = imageLoader,
				mainViewModel = viewModel,
				onOpenDetails = { manga -> router.openDetails(manga) },
				onOpenReader = { manga -> router.openReader(manga) },
				onOpenSettings = { router.openSettings() },
				onOpenList = { source -> router.openList(source, null, null) },
				onOpenSearch = { query -> router.openSearch(query) },
				onFinish = { finishAfterTransition() },
			)
		}
	}

	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)
		openCommentsFromNotification(intent)
	}

	/** A tapped reply notification: open that thread over whatever screen is showing. */
	private fun openCommentsFromNotification(intent: Intent?) {
		if (intent?.action != CommunityReplyNotifier.ACTION_OPEN_COMMENTS) return
		val workId = intent.getStringExtra(CommunityReplyNotifier.EXTRA_WORK_ID) ?: return
		val chapterId = intent.getLongExtra(CommunityReplyNotifier.EXTRA_CHAPTER_ID, 0L).takeIf { it != 0L }
		CommunityCommentsSheet.show(
			fm = supportFragmentManager,
			workId = workId,
			chapterId = chapterId,
			focusCommentId = intent.getStringExtra(CommunityReplyNotifier.EXTRA_COMMENT_ID),
		)
		intent.action = null
	}

	private fun onFirstStart() = try {
		lifecycleScope.launch(Dispatchers.Main) {
			withContext(Dispatchers.Default) {
				LocalStorageCleanupWorker.enqueue(applicationContext)
			}
			withResumed {
				MangaPrefetchService.prefetchLast(this@MainActivity)
				requestNotificationsPermission()
				startService(Intent(this@MainActivity, LocalIndexUpdateService::class.java))
				startService(Intent(this@MainActivity, PeriodicalBackupService::class.java))
			}
		}
	} catch (e: IllegalStateException) {
		e.printStackTraceDebug()
	}

	private fun requestNotificationsPermission() {
		if (ContextCompat.checkSelfPermission(
				this,
				Manifest.permission.POST_NOTIFICATIONS,
			) != PERMISSION_GRANTED
		) {
			ActivityCompat.requestPermissions(
				this,
				arrayOf(Manifest.permission.POST_NOTIFICATIONS),
				1,
			)
		}
	}
}
