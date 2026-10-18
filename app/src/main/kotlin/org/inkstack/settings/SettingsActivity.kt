package org.inkstack.settings

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import androidx.hilt.navigation.compose.hiltViewModel
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
import org.inkstack.settings.compose.SettingsDestination
import org.inkstack.settings.compose.SettingsScreen
import org.inkstack.settings.compose.rememberRootSettingsDestinations
import org.inkstack.settings.discord.DiscordSettingsFragment
import org.inkstack.settings.search.SettingsItem
import org.inkstack.settings.search.SettingsSearchViewModel
import org.inkstack.settings.sources.SourceSettingsFragment
import org.inkstack.settings.sources.SourcesSettingsFragment
import org.inkstack.settings.sources.manage.SourcesManageFragment
import org.inkstack.settings.tracker.TrackerSettingsFragment
import org.inkstack.settings.userdata.BackupsSettingsFragment

@AndroidEntryPoint
class SettingsActivity :
	BaseComposeActivity(),
	PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

	private val searchViewModel: SettingsSearchViewModel by viewModels()

	private var showRoot by mutableStateOf(true)
	private var sectionTitle by mutableStateOf("")
	private var pendingDeepLink: Fragment? = null

	private val backToRootCallback = object : OnBackPressedCallback(false) {
		override fun handleOnBackPressed() {
			if (supportFragmentManager.backStackEntryCount > 0) {
				supportFragmentManager.popBackStack()
			} else {
				clearSection()
			}
		}
	}

	@OptIn(ExperimentalMaterial3Api::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		sectionTitle = getString(R.string.settings)
		onBackPressedDispatcher.addCallback(this, backToRootCallback)
		supportFragmentManager.addOnBackStackChangedListener { syncRootVisibility() }

		if (savedInstanceState == null) {
			pendingDeepLink = resolveDeepLinkFragment()
			if (pendingDeepLink != null) {
				showRoot = false
				backToRootCallback.isEnabled = true
			}
		} else {
			showRoot = supportFragmentManager.findFragmentById(R.id.container) == null
			backToRootCallback.isEnabled = !showRoot
			sectionTitle = title?.toString() ?: getString(R.string.settings)
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

			// One scaffold so section fragments sit below a real TopAppBar (edge-to-edge safe).
			// Root keeps its own SettingsScreen scaffold; content insets are zeroed to avoid double padding.
			InkstackScaffold(
				modifier = Modifier.fillMaxSize(),
				topBar = {
					if (!showRoot) {
						TopAppBar(
							title = { Text(sectionTitle) },
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
					}
				},
				// TopAppBar owns status-bar insets; content only needs nav-bar insets.
				contentWindowInsets = if (showRoot) {
					WindowInsets(0, 0, 0, 0)
				} else {
					WindowInsets.navigationBars
				},
			) { padding ->
				Box(modifier = Modifier.fillMaxSize()) {
					AndroidView(
						factory = { context ->
							FrameLayout(context).apply {
								id = R.id.container
								layoutParams = FrameLayout.LayoutParams(
									FrameLayout.LayoutParams.MATCH_PARENT,
									FrameLayout.LayoutParams.MATCH_PARENT,
								)
							}
						},
						modifier = Modifier
							.fillMaxSize()
							.padding(if (showRoot) PaddingValues() else padding),
					)
					LaunchedEffect(Unit) {
						val deepLink = pendingDeepLink
						if (deepLink != null) {
							pendingDeepLink = null
							supportFragmentManager.commit {
								setReorderingAllowed(true)
								replace(R.id.container, deepLink)
							}
						}
					}
					if (showRoot) {
						SettingsScreen(
							destinations = destinations,
							onBack = { dispatchNavigateUp() },
							onOpen = ::openDestination,
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
		openFragment(
			fragmentClass = FragmentFactory.loadFragmentClass(classLoader, fragmentName),
			args = pref.peekExtras(),
			isFromRoot = false,
		)
		return true
	}

	fun setSectionTitle(title: CharSequence?) {
		val resolved = title?.toString() ?: getString(R.string.settings)
		sectionTitle = resolved
		setTitle(resolved)
	}

	fun openFragment(fragmentClass: Class<out Fragment>, args: Bundle?, isFromRoot: Boolean) {
		searchViewModel.discardSearch()
		showRoot = false
		backToRootCallback.isEnabled = true
		val hasFragment = supportFragmentManager.findFragmentById(R.id.container) != null
		supportFragmentManager.commit {
			setReorderingAllowed(true)
			replace(R.id.container, fragmentClass, args)
			setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
			if (!isFromRoot && hasFragment) {
				addToBackStack(null)
			}
		}
	}

	private fun openDestination(destination: SettingsDestination) {
		val fragmentClass = when (destination.key) {
			"appearance" -> AppearanceSettingsFragment::class.java
			AppSettings.KEY_REMOTE_SOURCES -> SourcesSettingsFragment::class.java
			"reader" -> ReaderSettingsFragment::class.java
			"network" -> StorageAndNetworkSettingsFragment::class.java
			"downloads" -> DownloadsSettingsFragment::class.java
			"tracker" -> TrackerSettingsFragment::class.java
			"services" -> ServicesSettingsFragment::class.java
			"userdata" -> BackupsSettingsFragment::class.java
			"about" -> AboutSettingsFragment::class.java
			else -> return
		}
		setSectionTitle(getString(destination.titleRes))
		openFragment(fragmentClass, null, isFromRoot = true)
	}

	private fun navigateToPreference(item: SettingsItem) {
		val args = buildBundle(1) {
			putString(ARG_PREF_KEY, item.key)
		}
		openFragment(item.fragmentClass, args, true)
	}

	private fun clearSection() {
		val current = supportFragmentManager.findFragmentById(R.id.container) ?: run {
			showRoot = true
			backToRootCallback.isEnabled = false
			return
		}
		supportFragmentManager.commit {
			setReorderingAllowed(true)
			remove(current)
			setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
		}
		showRoot = true
		backToRootCallback.isEnabled = false
		setSectionTitle(getString(R.string.settings))
	}

	private fun syncRootVisibility() {
		val hasFragment = supportFragmentManager.findFragmentById(R.id.container) != null
		if (!hasFragment && supportFragmentManager.backStackEntryCount == 0) {
			showRoot = true
			backToRootCallback.isEnabled = false
			setSectionTitle(getString(R.string.settings))
		} else {
			showRoot = false
			backToRootCallback.isEnabled = true
		}
	}

	private fun resolveDeepLinkFragment(): Fragment? = when (intent?.action) {
		AppRouter.ACTION_READER -> ReaderSettingsFragment()
		AppRouter.ACTION_SUGGESTIONS -> SuggestionsSettingsFragment()
		AppRouter.ACTION_HISTORY -> BackupsSettingsFragment()
		AppRouter.ACTION_TRACKER -> TrackerSettingsFragment()
		AppRouter.ACTION_PERIODIC_BACKUP -> PeriodicalBackupSettingsFragment()
		AppRouter.ACTION_SOURCES -> SourcesSettingsFragment()
		AppRouter.ACTION_MANAGE_DISCORD -> DiscordSettingsFragment()
		AppRouter.ACTION_PROXY -> ProxySettingsFragment()
		AppRouter.ACTION_MANAGE_DOWNLOADS -> DownloadsSettingsFragment()
		AppRouter.ACTION_SOURCE -> SourceSettingsFragment.newInstance(
			MangaSource(intent.getStringExtra(AppRouter.KEY_SOURCE)),
		)
		AppRouter.ACTION_MANAGE_SOURCES -> SourcesManageFragment()
		Intent.ACTION_VIEW -> when (intent.data?.host) {
			HOST_ABOUT -> AboutSettingsFragment()
			HOST_SYNC_SETTINGS -> SyncSettingsFragment()
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
