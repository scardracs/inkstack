package org.inkstack.main.ui.compose

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
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.ImageLoader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.inkstack.R
import org.inkstack.core.prefs.AppSettings
import org.inkstack.core.prefs.NavItem
import org.inkstack.core.util.ext.getThemeColor
import org.inkstack.explore.ui.compose.ExploreScreen
import org.inkstack.favourites.ui.compose.FavouritesScreen
import org.inkstack.history.ui.compose.HistoryScreen
import org.inkstack.main.ui.MainViewModel
import org.inkstack.main.ui.nav.FloatingNavBar
import org.inkstack.main.ui.nav.FloatingNavBarColors
import org.inkstack.main.ui.nav.FloatingNavBarItem
import org.inkstack.parsers.model.Manga
import org.inkstack.parsers.model.MangaSource
import org.inkstack.search.domain.ScreenSearchQuery
import org.inkstack.search.ui.suggestion.SearchSuggestionScope
import org.inkstack.suggestions.ui.compose.SuggestionsScreen
import org.inkstack.tracker.ui.updates.compose.UpdatesScreen
import com.google.android.material.R as materialR

@Composable
fun MainScreen(
	settings: AppSettings,
	screenSearchQuery: ScreenSearchQuery,
	imageLoader: ImageLoader,
	onOpenDetails: (Manga) -> Unit,
	onOpenReader: (Manga) -> Unit,
	onOpenSettings: () -> Unit,
	onOpenList: (MangaSource) -> Unit,
	onOpenSearch: (String) -> Unit,
	onFinish: () -> Unit,
	modifier: Modifier = Modifier,
	mainViewModel: MainViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val feedCounter by mainViewModel.feedCounter.collectAsState()
	val isResumeEnabled by mainViewModel.isResumeEnabled.collectAsState()
	val snackbarHostState = remember { SnackbarHostState() }
	val scope = rememberCoroutineScope()
	val confirmExit = stringResource(R.string.confirm_exit)

	val navItems = rememberNavItems(settings, feedCounter)
	val initialId = navItems.firstOrNull()?.id ?: NavItem.HISTORY.id
	var selectedId by rememberSaveable { mutableIntStateOf(initialId) }
	LaunchedEffect(navItems) {
		if (navItems.none { it.id == selectedId } && navItems.isNotEmpty()) {
			selectedId = navItems.first().id
		}
	}

	val searchScope = selectedId.toSearchScope()
	LaunchedEffect(searchScope) {
		screenSearchQuery.setActiveScope(searchScope)
	}
	val query by screenSearchQuery.query(searchScope).collectAsState()

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

	Scaffold(
		modifier = modifier.fillMaxSize(),
		contentWindowInsets = WindowInsets(0, 0, 0, 0),
		snackbarHost = { SnackbarHost(snackbarHostState) },
		topBar = {
			Box(Modifier.padding(top = statusBar.calculateTopPadding())) {
				MainSearchBar(
					query = query,
					onQueryChange = { screenSearchQuery.set(searchScope, it) },
					onSearch = { text ->
						if (searchScope == SearchSuggestionScope.ALL) {
							onOpenSearch(text)
						} else {
							screenSearchQuery.set(searchScope, text)
						}
					},
					onOpenSettings = onOpenSettings,
				)
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
					imageLoader = imageLoader,
					contentPadding = listPadding,
					modifier = Modifier.fillMaxSize(),
				)

				NavItem.FAVORITES.id -> FavouritesScreen(
					onMangaClick = onOpenDetails,
					imageLoader = imageLoader,
					contentPadding = listPadding,
					modifier = Modifier.fillMaxSize(),
				)

				NavItem.SUGGESTIONS.id -> SuggestionsScreen(
					onMangaClick = onOpenDetails,
					imageLoader = imageLoader,
					contentPadding = listPadding,
					modifier = Modifier.fillMaxSize(),
				)

				NavItem.EXPLORE.id -> ExploreScreen(
					onMangaClick = onOpenDetails,
					onSourceClick = onOpenList,
					imageLoader = imageLoader,
					contentPadding = listPadding,
					modifier = Modifier.fillMaxSize(),
				)

				NavItem.UPDATED.id,
				NavItem.FEED.id -> UpdatesScreen(
					onMangaClick = onOpenDetails,
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

private fun Int.toSearchScope(): SearchSuggestionScope = when (this) {
	NavItem.HISTORY.id -> SearchSuggestionScope.HISTORY
	NavItem.FAVORITES.id -> SearchSuggestionScope.FAVOURITES
	else -> SearchSuggestionScope.ALL
}
