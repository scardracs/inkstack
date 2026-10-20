package org.inkstack.settings

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.R
import org.inkstack.core.model.MangaSource
import org.inkstack.core.nav.AppRouter
import org.inkstack.core.prefs.AppSettings
import org.inkstack.core.ui.BaseComposeActivity
import org.inkstack.core.util.ext.getQuantityStringSafe
import org.inkstack.core.util.ext.observeEvent
import org.inkstack.settings.compose.SettingsDestination
import org.inkstack.settings.compose.SettingsScreen
import org.inkstack.settings.compose.rememberRootSettingsDestinations
import org.inkstack.settings.compose.sections.AboutSettingsScreen
import org.inkstack.settings.compose.sections.AppearanceSettingsScreen
import org.inkstack.settings.compose.sections.BackupsSettingsScreen
import org.inkstack.settings.compose.sections.ChangelogScreen
import org.inkstack.settings.compose.sections.CommunitySettingsScreen
import org.inkstack.settings.compose.sections.DataCleanupSettingsScreen
import org.inkstack.settings.compose.sections.DiscordSettingsScreen
import org.inkstack.settings.compose.sections.DownloadsSettingsScreen
import org.inkstack.settings.compose.sections.NavConfigScreen
import org.inkstack.settings.compose.sections.PeriodicalBackupSettingsScreen
import org.inkstack.settings.compose.sections.ProxySettingsScreen
import org.inkstack.settings.compose.sections.ReaderSettingsScreen
import org.inkstack.settings.compose.sections.ServicesSettingsScreen
import org.inkstack.settings.compose.sections.SourceSettingsScreen
import org.inkstack.settings.compose.sections.SourcesManageScreen
import org.inkstack.settings.compose.sections.SourcesSettingsScreen
import org.inkstack.settings.compose.sections.StorageNetworkSettingsScreen
import org.inkstack.settings.compose.sections.SuggestionsSettingsScreen
import org.inkstack.settings.compose.sections.SyncSettingsScreen
import org.inkstack.settings.compose.sections.TrackerSettingsScreen
import org.inkstack.settings.search.SettingsItem
import org.inkstack.settings.search.SettingsSearchViewModel
import org.inkstack.settings.search.SettingsSection

private sealed class SettingsRoute {
	data object Root : SettingsRoute()
	data object About : SettingsRoute()
	data object Suggestions : SettingsRoute()
	data object Appearance : SettingsRoute()
	data object Reader : SettingsRoute()
	data object Downloads : SettingsRoute()
	data object Tracker : SettingsRoute()
	data object Services : SettingsRoute()
	data object Sync : SettingsRoute()
	data object Community : SettingsRoute()
	data object Discord : SettingsRoute()
	data object Backups : SettingsRoute()
	data object PeriodicalBackup : SettingsRoute()
	data object StorageNetwork : SettingsRoute()
	data object Proxy : SettingsRoute()
	data object DataCleanup : SettingsRoute()
	data object Sources : SettingsRoute()
	data object SourcesManage : SettingsRoute()
	data object NavConfig : SettingsRoute()
	data object Changelog : SettingsRoute()
	data object Debug : SettingsRoute()
	data class Source(val sourceName: String) : SettingsRoute()
}

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class SettingsActivity : BaseComposeActivity() {

	private val searchViewModel: SettingsSearchViewModel by viewModels()

	private var route by mutableStateOf<SettingsRoute>(SettingsRoute.Root)
	private var stack by mutableStateOf<List<SettingsRoute>>(emptyList())

	private val backCallback = object : OnBackPressedCallback(false) {
		override fun handleOnBackPressed() {
			when {
				stack.size > 1 -> {
					stack = stack.dropLast(1)
					route = stack.last()
					applyTitle(route)
				}
				stack.isNotEmpty() || route !is SettingsRoute.Root -> {
					stack = emptyList()
					route = SettingsRoute.Root
					isEnabled = false
					setTitle(R.string.settings)
				}
				else -> isEnabled = false
			}
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		onBackPressedDispatcher.addCallback(this, backCallback)

		if (savedInstanceState == null) {
			resolveDeepLinkRoute()?.let { openRoute(it, replaceStack = true) }
		}

		searchViewModel.onNavigateToPreference.observeEvent(this) { item ->
			navigateToPreference(item)
		}

		setComposeContent {
			val rootViewModel: RootSettingsViewModel = hiltViewModel()
			val enabledCount by rootViewModel.enabledSourcesCount.collectAsState()
			val sourcesSummary = if (enabledCount >= 0) {
				getString(R.string.enabled_d_of_d, enabledCount, rootViewModel.totalSourcesCount)
			} else {
				resources.getQuantityStringSafe(
					R.plurals.items,
					rootViewModel.totalSourcesCount,
					rootViewModel.totalSourcesCount,
				)
			}
			val destinations = rememberRootSettingsDestinations(sourcesSummary)
			val goBack = { onBackPressedDispatcher.onBackPressed() }

			when (val current = route) {
				SettingsRoute.Root -> SettingsScreen(
					destinations = destinations,
					onBack = { dispatchNavigateUp() },
					onOpen = ::openDestination,
					modifier = Modifier.fillMaxSize(),
				)
				SettingsRoute.About -> AboutSettingsScreen(
					onBack = goBack,
					onOpenChangelog = { openRoute(SettingsRoute.Changelog) },
					modifier = Modifier.fillMaxSize(),
				)
				SettingsRoute.Suggestions -> SuggestionsSettingsScreen(
					onBack = goBack,
					modifier = Modifier.fillMaxSize(),
				)
				SettingsRoute.Appearance -> AppearanceSettingsScreen(
					onBack = goBack,
					onOpenNavConfig = { openRoute(SettingsRoute.NavConfig) },
					modifier = Modifier.fillMaxSize(),
				)
				SettingsRoute.Reader -> ReaderSettingsScreen(onBack = goBack, modifier = Modifier.fillMaxSize())
				SettingsRoute.Downloads -> DownloadsSettingsScreen(onBack = goBack, modifier = Modifier.fillMaxSize())
				SettingsRoute.Tracker -> TrackerSettingsScreen(onBack = goBack, modifier = Modifier.fillMaxSize())
				SettingsRoute.Services -> ServicesSettingsScreen(
					onBack = goBack,
					onOpenSync = { openRoute(SettingsRoute.Sync) },
					onOpenCommunity = { openRoute(SettingsRoute.Community) },
					onOpenSuggestions = { openRoute(SettingsRoute.Suggestions) },
					onOpenDiscord = { openRoute(SettingsRoute.Discord) },
					modifier = Modifier.fillMaxSize(),
				)
				SettingsRoute.Sync -> SyncSettingsScreen(onBack = goBack, modifier = Modifier.fillMaxSize())
				SettingsRoute.Community -> CommunitySettingsScreen(onBack = goBack, modifier = Modifier.fillMaxSize())
				SettingsRoute.Discord -> DiscordSettingsScreen(onBack = goBack, modifier = Modifier.fillMaxSize())
				SettingsRoute.Backups -> BackupsSettingsScreen(
					onBack = goBack,
					onOpenPeriodic = { openRoute(SettingsRoute.PeriodicalBackup) },
					modifier = Modifier.fillMaxSize(),
				)
				SettingsRoute.PeriodicalBackup -> PeriodicalBackupSettingsScreen(
					onBack = goBack,
					modifier = Modifier.fillMaxSize(),
				)
				SettingsRoute.StorageNetwork -> StorageNetworkSettingsScreen(
					onBack = goBack,
					onOpenProxy = { openRoute(SettingsRoute.Proxy) },
					onOpenDataCleanup = { openRoute(SettingsRoute.DataCleanup) },
					modifier = Modifier.fillMaxSize(),
				)
				SettingsRoute.Proxy -> ProxySettingsScreen(onBack = goBack, modifier = Modifier.fillMaxSize())
				SettingsRoute.DataCleanup -> DataCleanupSettingsScreen(onBack = goBack, modifier = Modifier.fillMaxSize())
				SettingsRoute.Sources -> SourcesSettingsScreen(
					onBack = goBack,
					onOpenManage = { openRoute(SettingsRoute.SourcesManage) },
					modifier = Modifier.fillMaxSize(),
				)
				SettingsRoute.SourcesManage -> SourcesManageScreen(
					onBack = goBack,
					onOpenSource = { openRoute(SettingsRoute.Source(it)) },
					modifier = Modifier.fillMaxSize(),
				)
				is SettingsRoute.Source -> SourceSettingsScreen(
					sourceName = current.sourceName,
					onBack = goBack,
					modifier = Modifier.fillMaxSize(),
				)
				SettingsRoute.NavConfig -> NavConfigScreen(onBack = goBack, modifier = Modifier.fillMaxSize())
				SettingsRoute.Changelog -> ChangelogScreen(onBack = goBack, modifier = Modifier.fillMaxSize())
				SettingsRoute.Debug -> {
					val debugContent = DebugSettingsAccess.content
					if (debugContent == null) {
						SettingsScreen(
							destinations = destinations,
							onBack = { dispatchNavigateUp() },
							onOpen = ::openDestination,
							modifier = Modifier.fillMaxSize(),
						)
					} else {
						debugContent(goBack)
					}
				}
			}
		}
	}

	private fun openDestination(destination: SettingsDestination) {
		val next = when (destination.key) {
			"about" -> SettingsRoute.About
			"appearance" -> SettingsRoute.Appearance
			AppSettings.KEY_REMOTE_SOURCES -> SettingsRoute.Sources
			"reader" -> SettingsRoute.Reader
			"network" -> SettingsRoute.StorageNetwork
			"downloads" -> SettingsRoute.Downloads
			"tracker" -> SettingsRoute.Tracker
			"services" -> SettingsRoute.Services
			"userdata" -> SettingsRoute.Backups
			"debug" -> SettingsRoute.Debug.takeIf { DebugSettingsAccess.isAvailable }
			else -> return
		} ?: return
		openRoute(next, replaceStack = true)
	}

	private fun navigateToPreference(item: SettingsItem) {
		openRoute(item.section.toRoute(), replaceStack = true)
	}

	private fun SettingsSection.toRoute(): SettingsRoute = when (this) {
		SettingsSection.About -> SettingsRoute.About
		SettingsSection.Suggestions -> SettingsRoute.Suggestions
		SettingsSection.Appearance -> SettingsRoute.Appearance
		SettingsSection.Reader -> SettingsRoute.Reader
		SettingsSection.Downloads -> SettingsRoute.Downloads
		SettingsSection.Tracker -> SettingsRoute.Tracker
		SettingsSection.Services -> SettingsRoute.Services
		SettingsSection.Sync -> SettingsRoute.Sync
		SettingsSection.Community -> SettingsRoute.Community
		SettingsSection.Discord -> SettingsRoute.Discord
		SettingsSection.Backups -> SettingsRoute.Backups
		SettingsSection.PeriodicalBackup -> SettingsRoute.PeriodicalBackup
		SettingsSection.StorageNetwork -> SettingsRoute.StorageNetwork
		SettingsSection.Proxy -> SettingsRoute.Proxy
		SettingsSection.DataCleanup -> SettingsRoute.DataCleanup
		SettingsSection.Sources -> SettingsRoute.Sources
		SettingsSection.SourcesManage -> SettingsRoute.SourcesManage
		SettingsSection.NavConfig -> SettingsRoute.NavConfig
		SettingsSection.Changelog -> SettingsRoute.Changelog
	}

	private fun openRoute(next: SettingsRoute, replaceStack: Boolean = false) {
		route = next
		backCallback.isEnabled = next !is SettingsRoute.Root
		stack = when {
			next is SettingsRoute.Root -> emptyList()
			replaceStack -> listOf(next)
			else -> stack + next
		}
		applyTitle(next)
	}

	private fun applyTitle(route: SettingsRoute) {
		setTitle(
			when (route) {
				SettingsRoute.Root -> getString(R.string.settings)
				SettingsRoute.About -> getString(R.string.about)
				SettingsRoute.Suggestions -> getString(R.string.suggestions)
				SettingsRoute.Appearance -> getString(R.string.appearance)
				SettingsRoute.Reader -> getString(R.string.reader_settings)
				SettingsRoute.Downloads -> getString(R.string.downloads)
				SettingsRoute.Tracker -> getString(R.string.check_for_new_chapters)
				SettingsRoute.Services -> getString(R.string.services)
				SettingsRoute.Sync -> getString(R.string.sync_settings)
				SettingsRoute.Community -> getString(R.string.community)
				SettingsRoute.Discord -> getString(R.string.discord)
				SettingsRoute.Backups -> getString(R.string.backup_restore)
				SettingsRoute.PeriodicalBackup -> getString(R.string.periodic_backups)
				SettingsRoute.StorageNetwork -> getString(R.string.storage_and_network)
				SettingsRoute.Proxy -> getString(R.string.proxy)
				SettingsRoute.DataCleanup -> getString(R.string.data_removal)
				SettingsRoute.Sources -> getString(R.string.remote_sources)
				SettingsRoute.SourcesManage -> getString(R.string.manage_sources)
				SettingsRoute.NavConfig -> getString(R.string.main_screen_sections)
				SettingsRoute.Changelog -> getString(R.string.changelog)
				SettingsRoute.Debug -> getString(R.string.debug)
				is SettingsRoute.Source -> getString(R.string.remote_sources)
			},
		)
	}

	private fun resolveDeepLinkRoute(): SettingsRoute? = when (intent?.action) {
		AppRouter.ACTION_READER -> SettingsRoute.Reader
		AppRouter.ACTION_SUGGESTIONS -> SettingsRoute.Suggestions
		AppRouter.ACTION_HISTORY -> SettingsRoute.Backups
		AppRouter.ACTION_TRACKER -> SettingsRoute.Tracker
		AppRouter.ACTION_PERIODIC_BACKUP -> SettingsRoute.PeriodicalBackup
		AppRouter.ACTION_SOURCES -> SettingsRoute.Sources
		AppRouter.ACTION_MANAGE_DISCORD -> SettingsRoute.Discord
		AppRouter.ACTION_PROXY -> SettingsRoute.Proxy
		AppRouter.ACTION_MANAGE_DOWNLOADS -> SettingsRoute.Downloads
		AppRouter.ACTION_SOURCE -> SettingsRoute.Source(
			MangaSource(intent.getStringExtra(AppRouter.KEY_SOURCE)).name,
		)
		AppRouter.ACTION_MANAGE_SOURCES -> SettingsRoute.SourcesManage
		Intent.ACTION_VIEW -> when (intent.data?.host) {
			HOST_ABOUT -> SettingsRoute.About
			HOST_SYNC_SETTINGS -> SettingsRoute.Sync
			else -> null
		}
		else -> null
	}

	companion object {
		private const val HOST_ABOUT = "about"
		private const val HOST_SYNC_SETTINGS = "sync-settings"
		const val ARG_PREF_KEY = "pref_key"
	}
}
