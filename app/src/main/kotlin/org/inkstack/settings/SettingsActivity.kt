package org.inkstack.settings

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.R
import org.inkstack.backups.ui.periodical.PeriodicalBackupSettingsFragment
import org.inkstack.core.model.MangaSource
import org.inkstack.core.nav.AppRouter
import org.inkstack.core.prefs.AppSettings
import org.inkstack.core.ui.BaseComposeActivity
import org.inkstack.core.ui.compose.InkstackScaffold
import org.inkstack.core.util.ext.buildBundle
import org.inkstack.core.util.ext.getQuantityStringSafe
import org.inkstack.core.util.ext.observeEvent
import org.inkstack.settings.about.AboutSettingsFragment
import org.inkstack.settings.about.changelog.ChangelogFragment
import org.inkstack.settings.compose.PreferenceFragmentPane
import org.inkstack.settings.compose.SettingsDestination
import org.inkstack.settings.compose.SettingsScreen
import org.inkstack.settings.compose.rememberRootSettingsDestinations
import org.inkstack.settings.compose.sections.AboutSettingsScreen
import org.inkstack.settings.compose.sections.SuggestionsSettingsScreen
import org.inkstack.settings.discord.DiscordSettingsFragment
import org.inkstack.settings.search.SettingsItem
import org.inkstack.settings.search.SettingsSearchViewModel
import org.inkstack.settings.sources.SourceSettingsFragment
import org.inkstack.settings.sources.SourcesSettingsFragment
import org.inkstack.settings.sources.manage.SourcesManageFragment
import org.inkstack.settings.tracker.TrackerSettingsFragment
import org.inkstack.settings.userdata.BackupsSettingsFragment

private sealed class SettingsRoute {
	data object Root : SettingsRoute()
	data object About : SettingsRoute()
	data object Suggestions : SettingsRoute()
	data class PreferenceHost(
		val fragmentClass: Class<out Fragment>,
		val title: String,
		val args: Bundle? = null,
	) : SettingsRoute()
}

@AndroidEntryPoint
class SettingsActivity :
	BaseComposeActivity(),
	PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

	private val searchViewModel: SettingsSearchViewModel by viewModels()

	private var route by mutableStateOf<SettingsRoute>(SettingsRoute.Root)
	private var nestedPreferenceStack by mutableStateOf<List<SettingsRoute.PreferenceHost>>(emptyList())

	private val backCallback = object : OnBackPressedCallback(false) {
		override fun handleOnBackPressed() {
			when {
				nestedPreferenceStack.size > 1 -> {
					nestedPreferenceStack = nestedPreferenceStack.dropLast(1)
					route = nestedPreferenceStack.last()
				}
				nestedPreferenceStack.isNotEmpty() || route !is SettingsRoute.Root -> {
					nestedPreferenceStack = emptyList()
					route = SettingsRoute.Root
					isEnabled = false
					setTitle(R.string.settings)
				}
				else -> isEnabled = false
			}
		}
	}

	@OptIn(ExperimentalMaterial3Api::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		onBackPressedDispatcher.addCallback(this, backCallback)

		if (savedInstanceState == null) {
			resolveDeepLinkRoute()?.let { openRoute(it) }
		}

		searchViewModel.onNavigateToPreference.observeEvent(this, ::navigateToPreference)

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
			val current = route

			when (current) {
				SettingsRoute.Root -> SettingsScreen(
					destinations = destinations,
					onBack = { dispatchNavigateUp() },
					onOpen = ::openDestination,
					modifier = Modifier.fillMaxSize(),
				)

				SettingsRoute.About -> AboutSettingsScreen(
					onBack = { onBackPressedDispatcher.onBackPressed() },
					onOpenChangelog = {
						openRoute(
							SettingsRoute.PreferenceHost(
								ChangelogFragment::class.java,
								getString(R.string.changelog),
							),
						)
					},
					modifier = Modifier.fillMaxSize(),
				)

				SettingsRoute.Suggestions -> SuggestionsSettingsScreen(
					onBack = { onBackPressedDispatcher.onBackPressed() },
					modifier = Modifier.fillMaxSize(),
				)

				is SettingsRoute.PreferenceHost -> InkstackScaffold(
					modifier = Modifier.fillMaxSize(),
					topBar = {
						TopAppBar(
							title = { Text(current.title) },
							navigationIcon = {
								IconButton(onClick = { onBackPressedDispatcher.onBackPressed() }) {
									Icon(
										imageVector = Icons.AutoMirrored.Filled.ArrowBack,
										contentDescription = null,
									)
								}
							},
							windowInsets = WindowInsets.statusBars,
							colors = TopAppBarDefaults.topAppBarColors(),
						)
					},
					contentWindowInsets = WindowInsets.navigationBars,
				) { padding ->
					Box(
						modifier = Modifier
							.fillMaxSize()
							.padding(padding),
					) {
						PreferenceFragmentPane(
							fragmentClass = current.fragmentClass,
							args = current.args,
							modifier = Modifier.fillMaxSize(),
						)
					}
				}
			}
		}
	}

	override fun onPreferenceStartFragment(
		caller: PreferenceFragmentCompat,
		pref: Preference,
	): Boolean {
		val fragmentName = pref.fragment ?: return false
		val fragmentClass = FragmentFactory.loadFragmentClass(classLoader, fragmentName)
		when {
			fragmentClass == SuggestionsSettingsFragment::class.java -> openRoute(SettingsRoute.Suggestions)
			fragmentClass == AboutSettingsFragment::class.java -> openRoute(SettingsRoute.About)
			else -> openRoute(
				SettingsRoute.PreferenceHost(
					fragmentClass = fragmentClass,
					title = pref.title?.toString() ?: getString(R.string.settings),
					args = pref.peekExtras(),
				),
			)
		}
		return true
	}

	fun setSectionTitle(title: CharSequence?) {
		val resolved = title?.toString() ?: getString(R.string.settings)
		setTitle(resolved)
		val current = route
		if (current is SettingsRoute.PreferenceHost) {
			route = current.copy(title = resolved)
			nestedPreferenceStack = nestedPreferenceStack.dropLast(1) + route as SettingsRoute.PreferenceHost
		}
	}

	fun openFragment(fragmentClass: Class<out Fragment>, args: Bundle?, isFromRoot: Boolean) {
		openRoute(
			SettingsRoute.PreferenceHost(
				fragmentClass = fragmentClass,
				title = title?.toString() ?: getString(R.string.settings),
				args = args,
			),
			replaceStack = isFromRoot,
		)
	}

	private fun openDestination(destination: SettingsDestination) {
		when (destination.key) {
			"about" -> openRoute(SettingsRoute.About)
			"services" -> {
				// Services hub still Preference; suggestions is Compose via deep-link / search
				openRoute(
					SettingsRoute.PreferenceHost(
						ServicesSettingsFragment::class.java,
						getString(destination.titleRes),
					),
					replaceStack = true,
				)
			}
			else -> {
				val fragmentClass = when (destination.key) {
					"appearance" -> AppearanceSettingsFragment::class.java
					AppSettings.KEY_REMOTE_SOURCES -> SourcesSettingsFragment::class.java
					"reader" -> ReaderSettingsFragment::class.java
					"network" -> StorageAndNetworkSettingsFragment::class.java
					"downloads" -> DownloadsSettingsFragment::class.java
					"tracker" -> TrackerSettingsFragment::class.java
					"userdata" -> BackupsSettingsFragment::class.java
					else -> return
				}
				openRoute(
					SettingsRoute.PreferenceHost(
						fragmentClass,
						getString(destination.titleRes),
					),
					replaceStack = true,
				)
			}
		}
	}

	private fun navigateToPreference(item: SettingsItem) {
		when {
			item.fragmentClass == AboutSettingsFragment::class.java -> openRoute(SettingsRoute.About)
			item.fragmentClass == SuggestionsSettingsFragment::class.java -> openRoute(SettingsRoute.Suggestions)
			else -> openRoute(
				SettingsRoute.PreferenceHost(
					item.fragmentClass,
					item.title.toString(),
					buildBundle(1) { putString(ARG_PREF_KEY, item.key) },
				),
				replaceStack = true,
			)
		}
	}

	private fun openRoute(next: SettingsRoute, replaceStack: Boolean = false) {
		route = next
		backCallback.isEnabled = next !is SettingsRoute.Root
		when (next) {
			is SettingsRoute.PreferenceHost -> {
				nestedPreferenceStack = if (replaceStack) listOf(next) else nestedPreferenceStack + next
				setTitle(next.title)
			}
			SettingsRoute.About -> {
				nestedPreferenceStack = emptyList()
				setTitle(R.string.about)
			}
			SettingsRoute.Suggestions -> {
				nestedPreferenceStack = emptyList()
				setTitle(R.string.suggestions)
			}
			SettingsRoute.Root -> {
				nestedPreferenceStack = emptyList()
				setTitle(R.string.settings)
			}
		}
	}

	private fun resolveDeepLinkRoute(): SettingsRoute? = when (intent?.action) {
		AppRouter.ACTION_READER -> SettingsRoute.PreferenceHost(
			ReaderSettingsFragment::class.java,
			getString(R.string.reader_settings),
		)
		AppRouter.ACTION_SUGGESTIONS -> SettingsRoute.Suggestions
		AppRouter.ACTION_HISTORY -> SettingsRoute.PreferenceHost(
			BackupsSettingsFragment::class.java,
			getString(R.string.backup_restore),
		)
		AppRouter.ACTION_TRACKER -> SettingsRoute.PreferenceHost(
			TrackerSettingsFragment::class.java,
			getString(R.string.check_for_new_chapters),
		)
		AppRouter.ACTION_PERIODIC_BACKUP -> SettingsRoute.PreferenceHost(
			PeriodicalBackupSettingsFragment::class.java,
			getString(R.string.periodic_backups),
		)
		AppRouter.ACTION_SOURCES -> SettingsRoute.PreferenceHost(
			SourcesSettingsFragment::class.java,
			getString(R.string.remote_sources),
		)
		AppRouter.ACTION_MANAGE_DISCORD -> SettingsRoute.PreferenceHost(
			DiscordSettingsFragment::class.java,
			getString(R.string.discord),
		)
		AppRouter.ACTION_PROXY -> SettingsRoute.PreferenceHost(
			ProxySettingsFragment::class.java,
			getString(R.string.proxy),
		)
		AppRouter.ACTION_MANAGE_DOWNLOADS -> SettingsRoute.PreferenceHost(
			DownloadsSettingsFragment::class.java,
			getString(R.string.downloads),
		)
		AppRouter.ACTION_SOURCE -> {
			val fragment = SourceSettingsFragment.newInstance(
				MangaSource(intent.getStringExtra(AppRouter.KEY_SOURCE)),
			)
			SettingsRoute.PreferenceHost(
				SourceSettingsFragment::class.java,
				getString(R.string.remote_sources),
				fragment.arguments,
			)
		}
		AppRouter.ACTION_MANAGE_SOURCES -> SettingsRoute.PreferenceHost(
			SourcesManageFragment::class.java,
			getString(R.string.manage_sources),
		)
		Intent.ACTION_VIEW -> when (intent.data?.host) {
			HOST_ABOUT -> SettingsRoute.About
			HOST_SYNC_SETTINGS -> SettingsRoute.PreferenceHost(
				SyncSettingsFragment::class.java,
				getString(R.string.sync_settings),
			)
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
