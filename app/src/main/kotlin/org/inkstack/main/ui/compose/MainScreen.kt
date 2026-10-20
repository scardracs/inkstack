package org.inkstack.main.ui.compose

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.ImageLoader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.inkstack.R
import org.inkstack.core.nav.router
import org.inkstack.core.prefs.AppSettings
import org.inkstack.core.prefs.NavItem
import org.inkstack.core.ui.compose.SelectionAwareTopBar
import org.inkstack.core.ui.dialog.RememberCheckListener
import org.inkstack.core.ui.dialog.RememberSelectionDialogListener
import org.inkstack.core.ui.dialog.buildAlertDialog
import org.inkstack.core.ui.dialog.setCheckbox
import org.inkstack.core.util.ext.getThemeColor
import org.inkstack.explore.ui.compose.ExploreScreen
import org.inkstack.explore.ui.preset.SourcePresetListActivity
import org.inkstack.favourites.ui.compose.FavouritesScreen
import org.inkstack.favourites.ui.list.FavouritesListViewModel
import org.inkstack.history.ui.HistoryListViewModel
import org.inkstack.history.ui.compose.HistoryScreen
import org.inkstack.list.ui.config.ListConfigSection
import org.inkstack.main.ui.MainViewModel
import org.inkstack.main.ui.nav.FloatingNavBar
import org.inkstack.main.ui.nav.FloatingNavBarColors
import org.inkstack.main.ui.nav.FloatingNavBarItem
import org.inkstack.parsers.model.Manga
import org.inkstack.parsers.model.MangaSource
import org.inkstack.suggestions.ui.SuggestionsViewModel
import org.inkstack.suggestions.ui.compose.SuggestionsScreen
import org.inkstack.tracker.ui.feed.FeedViewModel
import org.inkstack.tracker.ui.feed.compose.FeedScreen
import org.inkstack.tracker.ui.updates.UpdatesViewModel
import org.inkstack.tracker.ui.updates.compose.UpdatesScreen
import org.inkstack.explore.ui.ExploreViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import com.google.android.material.R as materialR

@Composable
fun MainScreen(
	settings: AppSettings,
	imageLoader: ImageLoader,
	onOpenDetails: (Manga) -> Unit,
	onOpenReader: (Manga) -> Unit,
	onOpenSettings: () -> Unit,
	onOpenList: (MangaSource) -> Unit,
	onOpenSearch: (String) -> Unit,
	onFinish: () -> Unit,
	modifier: Modifier = Modifier,
	mainViewModel: MainViewModel = hiltViewModel(),
	historyViewModel: HistoryListViewModel = hiltViewModel(),
	favouritesViewModel: FavouritesListViewModel = hiltViewModel(),
	suggestionsViewModel: SuggestionsViewModel = hiltViewModel(),
	updatesViewModel: UpdatesViewModel = hiltViewModel(),
	feedViewModel: FeedViewModel = hiltViewModel(),
	exploreViewModel: ExploreViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val activity = context as FragmentActivity
	val router = activity.router
	val feedCounter by mainViewModel.feedCounter.collectAsState()
	val isResumeEnabled by mainViewModel.isResumeEnabled.collectAsState()
	val isIncognito by mainViewModel.isIncognitoModeEnabled.collectAsState()
	val appUpdate by mainViewModel.appUpdate.collectAsState(initial = null)
	val isStatsEnabled by historyViewModel.isStatsEnabled.collectAsState()
	val isFeedHeaderEnabled by feedViewModel.isHeaderEnabled.collectAsState()
	val snackbarHostState = remember { SnackbarHostState() }
	val scope = rememberCoroutineScope()
	val confirmExit = stringResource(R.string.confirm_exit)
	val suggestionsUpdating = stringResource(R.string.suggestions_updating)

	val navItems = rememberNavItems(settings, feedCounter)
	val initialId = navItems.firstOrNull()?.id ?: NavItem.HISTORY.id
	var selectedId by rememberSaveable { mutableIntStateOf(initialId) }
	LaunchedEffect(navItems) {
		if (navItems.none { it.id == selectedId } && navItems.isNotEmpty()) {
			selectedId = navItems.first().id
		}
	}

	// One global search query for every tab — never filters History/Favourites in place.
	var query by rememberSaveable { mutableStateOf("") }

	val firstId = navItems.firstOrNull()?.id
	var exitArmed by remember { mutableStateOf(false) }
	BackHandler(enabled = true) {
		when {
			selectedId != firstId && firstId != null -> selectedId = firstId
			settings.isExitConfirmationEnabled && !exitArmed -> {
				exitArmed = true
				scope.launch {
					snackbarHostState.showSnackbar(confirmExit)
					delay(2000)
					exitArmed = false
				}
			}
			else -> onFinish()
		}
	}

	val statusBar = WindowInsets.statusBars.asPaddingValues()
	val navBarInsets = WindowInsets.navigationBars.asPaddingValues()
	val labeled = settings.isNavLabelsVisible
	val navColors = remember(context.theme) {
		FloatingNavBarColors(
			container = context.getThemeColor(materialR.attr.colorSurfaceContainer),
			selectedContainer = context.getThemeColor(materialR.attr.colorSecondaryContainer),
			selectedContent = context.getThemeColor(materialR.attr.colorOnSecondaryContainer),
			unselectedContent = context.getThemeColor(materialR.attr.colorOnSurface),
		)
	}

	val listPadding = PaddingValues(
		start = 12.dp,
		end = 12.dp,
		bottom = navBarInsets.calculateBottomPadding() + 96.dp,
	)

	val menuItems = buildMainOverflowItems(
		selectedId = selectedId,
		isIncognito = isIncognito,
		hasAppUpdate = appUpdate != null,
		isStatsEnabled = isStatsEnabled,
		isFeedHeaderEnabled = isFeedHeaderEnabled,
		onOpenSettings = onOpenSettings,
		onToggleIncognito = { mainViewModel.setIncognitoMode(!isIncognito) },
		onAppUpdate = { router.openAppUpdate() },
		onListOptions = { section -> router.showListConfigSheet(section) },
		onClearHistory = {
			showClearHistoryDialog(activity, historyViewModel)
		},
		onStats = { router.openStatistic() },
		onFavouriteCategories = { router.openFavoriteCategories() },
		onUpdateSuggestions = {
			suggestionsViewModel.updateSuggestions()
			scope.launch { snackbarHostState.showSnackbar(suggestionsUpdating) }
		},
		onSuggestionsSettings = { router.openSuggestionsSettings() },
		onManageSources = { router.openSourcesSettings() },
		onSourcePresets = {
			context.startActivity(Intent(context, SourcePresetListActivity::class.java))
		},
		onUpdateFeed = { feedViewModel.update() },
		onToggleFeedHeader = { feedViewModel.setHeaderEnabled(!isFeedHeaderEnabled) },
		onClearFeed = { showClearFeedDialog(activity, feedViewModel) },
		favouritesCategoryId = favouritesViewModel.categoryId,
	)

	Scaffold(
		modifier = modifier.fillMaxSize(),
		contentWindowInsets = WindowInsets(0, 0, 0, 0),
		snackbarHost = { SnackbarHost(snackbarHostState) },
		topBar = {
			Box(Modifier.padding(top = statusBar.calculateTopPadding())) {
				SelectionAwareTopBar {
					MainSearchBar(
						query = query,
						onQueryChange = { query = it },
						onSearch = { text ->
							onOpenSearch(text.trim())
							query = ""
						},
						menuItems = menuItems,
					)
				}
			}
		},
	) { innerPadding ->
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(innerPadding),
		) {
			when (selectedId) {
				NavItem.HISTORY.id -> HistoryScreen(
					onMangaClick = onOpenDetails,
					viewModel = historyViewModel,
					imageLoader = imageLoader,
					contentPadding = listPadding,
					modifier = Modifier.fillMaxSize(),
				)

				NavItem.FAVORITES.id -> FavouritesScreen(
					onMangaClick = onOpenDetails,
					viewModel = favouritesViewModel,
					imageLoader = imageLoader,
					contentPadding = listPadding,
					modifier = Modifier.fillMaxSize(),
				)

				NavItem.SUGGESTIONS.id -> SuggestionsScreen(
					onMangaClick = onOpenDetails,
					viewModel = suggestionsViewModel,
					imageLoader = imageLoader,
					contentPadding = listPadding,
					modifier = Modifier.fillMaxSize(),
				)

				NavItem.EXPLORE.id -> ExploreScreen(
					onMangaClick = onOpenDetails,
					onSourceClick = onOpenList,
					viewModel = exploreViewModel,
					imageLoader = imageLoader,
					contentPadding = listPadding,
					modifier = Modifier.fillMaxSize(),
				)

				NavItem.UPDATED.id -> UpdatesScreen(
					onMangaClick = onOpenDetails,
					viewModel = updatesViewModel,
					imageLoader = imageLoader,
					contentPadding = listPadding,
					modifier = Modifier.fillMaxSize(),
				)

				NavItem.FEED.id -> FeedScreen(
					onMangaClick = onOpenDetails,
					viewModel = feedViewModel,
					imageLoader = imageLoader,
					contentPadding = listPadding,
					modifier = Modifier.fillMaxSize(),
				)

				else -> Box(
					modifier = Modifier
						.fillMaxSize()
						.padding(listPadding),
					contentAlignment = Alignment.Center,
				) {
					Text(
						text = stringResource(
							navItems.firstOrNull { it.id == selectedId }?.titleRes ?: R.string.app_name,
						),
						style = MaterialTheme.typography.titleMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant,
					)
				}
			}

			FloatingNavBar(
				items = navItems,
				selectedId = selectedId,
				showLabels = labeled,
				colors = navColors,
				onItemSelected = { selectedId = it },
				onItemReselected = { },
				showContinue = isResumeEnabled && selectedId == NavItem.HISTORY.id,
				onContinueClick = { mainViewModel.openLastReader() },
				modifier = Modifier
					.align(Alignment.BottomCenter)
					.fillMaxWidth()
					.padding(
						start = 12.dp,
						end = 12.dp,
						bottom = navBarInsets.calculateBottomPadding() + 8.dp,
					),
			)
		}
	}

	LaunchedEffect(mainViewModel) {
		mainViewModel.onOpenReader.collect { event ->
			event?.consume { onOpenReader(it) }
		}
	}
}

private fun buildMainOverflowItems(
	selectedId: Int,
	isIncognito: Boolean,
	hasAppUpdate: Boolean,
	isStatsEnabled: Boolean,
	isFeedHeaderEnabled: Boolean,
	onOpenSettings: () -> Unit,
	onToggleIncognito: () -> Unit,
	onAppUpdate: () -> Unit,
	onListOptions: (ListConfigSection) -> Unit,
	onClearHistory: () -> Unit,
	onStats: () -> Unit,
	onFavouriteCategories: () -> Unit,
	onUpdateSuggestions: () -> Unit,
	onSuggestionsSettings: () -> Unit,
	onManageSources: () -> Unit,
	onSourcePresets: () -> Unit,
	onUpdateFeed: () -> Unit,
	onToggleFeedHeader: () -> Unit,
	onClearFeed: () -> Unit,
	favouritesCategoryId: Long,
): List<MainOverflowItem> {
	val items = mutableListOf<MainOverflowItem>()

	when (selectedId) {
		NavItem.HISTORY.id -> {
			items += MainOverflowItem("list_mode", R.string.list_options) {
				onListOptions(ListConfigSection.History)
			}
			items += MainOverflowItem("clear_history", R.string.clear_history, onClick = onClearHistory)
			items += MainOverflowItem(
				id = "stats",
				titleRes = R.string.statistics,
				visible = isStatsEnabled,
				onClick = onStats,
			)
		}

		NavItem.FAVORITES.id -> {
			items += MainOverflowItem("list_mode", R.string.list_options) {
				onListOptions(ListConfigSection.Favorites(favouritesCategoryId))
			}
			items += MainOverflowItem(
				"categories",
				R.string.favourites_categories,
				onClick = onFavouriteCategories,
			)
		}

		NavItem.SUGGESTIONS.id -> {
			items += MainOverflowItem("list_mode", R.string.list_options) {
				onListOptions(ListConfigSection.Suggestions)
			}
			items += MainOverflowItem("update", R.string.update, onClick = onUpdateSuggestions)
			items += MainOverflowItem(
				"suggestions_settings",
				R.string.settings,
				onClick = onSuggestionsSettings,
			)
		}

		NavItem.EXPLORE.id -> {
			items += MainOverflowItem("presets", R.string.source_presets, onClick = onSourcePresets)
			items += MainOverflowItem("manage", R.string.manage_sources, onClick = onManageSources)
		}

		NavItem.UPDATED.id -> {
			items += MainOverflowItem("list_mode", R.string.list_options) {
				onListOptions(ListConfigSection.Updated)
			}
		}

		NavItem.FEED.id -> {
			items += MainOverflowItem("update", R.string.update, onClick = onUpdateFeed)
			items += MainOverflowItem(
				id = "show_updated",
				titleRes = R.string.show_updated,
				checked = isFeedHeaderEnabled,
				onClick = onToggleFeedHeader,
			)
			items += MainOverflowItem("clear_feed", R.string.clear_feed, onClick = onClearFeed)
		}
	}

	items += MainOverflowItem(
		id = "app_update",
		titleRes = R.string.app_update_available,
		visible = hasAppUpdate,
		onClick = onAppUpdate,
	)
	items += MainOverflowItem(
		id = "incognito",
		titleRes = R.string.incognito_mode,
		checked = isIncognito,
		onClick = onToggleIncognito,
	)
	// Avoid duplicating Settings when the suggestions tab already exposes it.
	if (selectedId != NavItem.SUGGESTIONS.id) {
		items += MainOverflowItem("settings", R.string.settings, onClick = onOpenSettings)
	}

	return items
}

private fun showClearHistoryDialog(
	activity: FragmentActivity,
	viewModel: HistoryListViewModel,
) {
	val selectionListener = RememberSelectionDialogListener(1)
	buildAlertDialog(activity, isCentered = true) {
		setTitle(R.string.clear_history)
		setSingleChoiceItems(
			arrayOf(
				activity.getString(R.string.last_2_hours),
				activity.getString(R.string.today),
				activity.getString(R.string.not_in_favorites),
				activity.getString(R.string.clear_all_history),
			),
			selectionListener.selection,
			selectionListener,
		)
		setIcon(R.drawable.ic_delete_all)
		setNegativeButton(android.R.string.cancel, null)
		setPositiveButton(R.string.clear) { _, _ ->
			when (selectionListener.selection) {
				0 -> viewModel.clearHistory(Instant.now().minus(2, ChronoUnit.HOURS))
				1 -> viewModel.clearHistory(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant())
				2 -> viewModel.removeNotFavorite()
				3 -> viewModel.clearHistory(null)
			}
		}
	}.show()
}

private fun showClearFeedDialog(
	activity: FragmentActivity,
	viewModel: FeedViewModel,
) {
	val checkListener = RememberCheckListener(true)
	buildAlertDialog(activity, isCentered = true) {
		setIcon(R.drawable.ic_clear_all)
		setTitle(R.string.clear_updates_feed)
		setMessage(R.string.text_clear_updates_feed_prompt)
		setNegativeButton(android.R.string.cancel, null)
		setCheckbox(R.string.clear_new_chapters_counters, true, checkListener)
		setPositiveButton(R.string.clear) { _, _ ->
			viewModel.clearFeed(checkListener.isChecked)
		}
	}.show()
}

@Composable
private fun rememberNavItems(settings: AppSettings, feedCounter: Int): List<FloatingNavBarItem> {
	val raw = settings.mainNavItems
	return remember(raw, feedCounter, settings.isSuggestionsEnabled, settings.isTrackerEnabled) {
		raw.asSequence()
			.filter { it.isAvailable(settings) }
			.take(5)
			.map { item ->
				FloatingNavBarItem(
					id = item.id,
					titleRes = item.title,
					icon = item.icon,
					badgeCount = if (item == NavItem.FEED || item == NavItem.UPDATED) feedCounter else 0,
				)
			}
			.toList()
	}
}

