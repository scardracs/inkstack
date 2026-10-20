package org.inkstack.settings.compose.sections

import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.inkstack.R
import org.inkstack.core.os.AppShortcutManager
import org.inkstack.core.prefs.AppSettings
import org.inkstack.core.prefs.ColorScheme
import org.inkstack.core.prefs.ListMode
import org.inkstack.core.prefs.ProgressIndicatorMode
import org.inkstack.core.prefs.ScreenshotsPolicy
import org.inkstack.core.prefs.SearchSuggestionType
import org.inkstack.core.ui.util.ActivityRecreationHandle
import org.inkstack.core.util.LocaleComparator
import org.inkstack.core.util.ext.getLocalesConfig
import org.inkstack.core.util.ext.sortedWithSafe
import org.inkstack.core.util.ext.toList
import org.inkstack.parsers.util.mapToSet
import org.inkstack.parsers.util.names
import org.inkstack.parsers.util.toTitleCase
import org.inkstack.settings.compose.prefs.ListPreferenceItem
import org.inkstack.settings.compose.prefs.MultiSelectPreferenceItem
import org.inkstack.settings.compose.prefs.NavigationPreferenceItem
import org.inkstack.settings.compose.prefs.PreferenceCategoryHeader
import org.inkstack.settings.compose.prefs.SliderPreferenceItem
import org.inkstack.settings.compose.prefs.SwitchPreferenceItem
import org.inkstack.settings.compose.prefs.rememberSharedPreferences
import org.inkstack.settings.protect.ProtectSetupActivity
import javax.inject.Inject

@HiltViewModel
class AppearanceSettingsComposeViewModel @Inject constructor(
	val settings: AppSettings,
	private val activityRecreationHandle: ActivityRecreationHandle,
	val appShortcutManager: AppShortcutManager,
) : ViewModel() {
	fun recreateAll() {
		viewModelScope.launch {
			delay(400)
			activityRecreationHandle.recreateAll()
		}
	}
}

@Composable
fun AppearanceSettingsScreen(
	onBack: () -> Unit,
	onOpenNavConfig: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: AppearanceSettingsComposeViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val prefs = rememberSharedPreferences()
	val settings = viewModel.settings
	val themes = stringArrayResource(R.array.themes)
	val themeValues = stringArrayResource(R.array.values_theme)
	val listModes = stringArrayResource(R.array.list_modes)
	val progressIndicators = stringArrayResource(R.array.progress_indicators)
	val listBadges = stringArrayResource(R.array.list_badges)
	val listBadgeValues = stringArrayResource(R.array.values_list_badges)
	val detailsTabs = stringArrayResource(R.array.details_tabs)
	val detailsTabValues = stringArrayResource(R.array.details_tabs_values)
	val screenshots = stringArrayResource(R.array.screenshots_policy)

	var colorTheme by remember {
		mutableStateOf(prefs.getString(AppSettings.KEY_COLOR_THEME, ColorScheme.default.name) ?: ColorScheme.default.name)
	}
	var theme by remember { mutableStateOf(prefs.getString(AppSettings.KEY_THEME, "-1") ?: "-1") }
	var amoled by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_THEME_AMOLED, false)) }
	var listMode by remember {
		mutableStateOf(prefs.getString(AppSettings.KEY_LIST_MODE, ListMode.GRID.name) ?: ListMode.GRID.name)
	}
	var gridSize by remember { mutableStateOf(prefs.getInt(AppSettings.KEY_GRID_SIZE, 100)) }
	var quickFilter by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_QUICK_FILTER, true)) }
	var progressMode by remember {
		mutableStateOf(
			prefs.getString(AppSettings.KEY_PROGRESS_INDICATORS, ProgressIndicatorMode.PERCENT_READ.name)
				?: ProgressIndicatorMode.PERCENT_READ.name,
		)
	}
	var badges by remember {
		mutableStateOf(prefs.getStringSet(AppSettings.KEY_MANGA_LIST_BADGES, listBadgeValues.toSet())?.toSet().orEmpty())
	}
	var descriptionCollapse by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_COLLAPSE_DESCRIPTION, true)) }
	var pagesTab by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_PAGES_TAB, true)) }
	var detailsTab by remember { mutableStateOf(prefs.getString(AppSettings.KEY_DETAILS_TAB, "-1") ?: "-1") }
	var searchSuggest by remember {
		mutableStateOf(settings.searchSuggestionTypes.mapToSet { it.name })
	}
	var mainFab by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_MAIN_FAB, true)) }
	var navLabels by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_NAV_LABELS, true)) }
	var floatingNav by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_FLOATING_NAV, false)) }
	var navPinned by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_NAV_PINNED, false)) }
	var exitConfirm by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_EXIT_CONFIRM, false)) }
	var shortcuts by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_SHORTCUTS, true)) }
	var shortcutsNoNsfw by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_SHORTCUTS_NO_NSFW, true)) }
	var protectApp by remember { mutableStateOf(!settings.appPassword.isNullOrEmpty()) }
	var screenshotsPolicy by remember {
		mutableStateOf(
			prefs.getString(AppSettings.KEY_SCREENSHOTS_POLICY, ScreenshotsPolicy.ALLOW.name)
				?: ScreenshotsPolicy.ALLOW.name,
		)
	}
	var locale by remember { mutableStateOf(prefs.getString(AppSettings.KEY_APP_LOCALE, "").orEmpty()) }
	var navSummary by remember {
		mutableStateOf(settings.mainNavItems.joinToString { context.getString(it.title) })
	}

	LaunchedEffect(Unit) {
		navSummary = settings.mainNavItems.joinToString { context.getString(it.title) }
	}

	val locales = remember {
		context.getLocalesConfig().toList().sortedWithSafe(LocaleComparator())
	}
	val localeEntries = remember(locales) {
		arrayOf(context.getString(R.string.follow_system)) + locales.map {
			it.getDisplayName(it).toTitleCase(it)
		}
	}
	val localeValues = remember(locales) {
		arrayOf("") + locales.map { it.toLanguageTag() }
	}
	val suggestEntries = remember {
		SearchSuggestionType.entries.map { context.getString(it.titleResId) }.toTypedArray()
	}
	val suggestValues = remember { SearchSuggestionType.entries.names() }
	val listModeValues = remember { ListMode.entries.names() }
	val progressValues = remember { ProgressIndicatorMode.entries.names() }
	val screenshotValues = remember { ScreenshotsPolicy.entries.names() }

	SettingsSectionScaffold(
		titleRes = R.string.appearance,
		onBack = onBack,
		modifier = modifier,
	) {
		item {
			Text(
				text = stringResource(R.string.color_theme),
				modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
			)
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.horizontalScroll(rememberScrollState())
					.padding(horizontal = 12.dp),
			) {
				ColorScheme.getAvailableList().forEach { scheme ->
					FilterChip(
						selected = colorTheme == scheme.name,
						onClick = {
							colorTheme = scheme.name
							prefs.edit { putString(AppSettings.KEY_COLOR_THEME, scheme.name) }
							viewModel.recreateAll()
						},
						label = { Text(stringResource(scheme.titleResId)) },
						modifier = Modifier.padding(horizontal = 4.dp),
					)
				}
			}
		}
		item {
			ListPreferenceItem(
				titleRes = R.string.theme,
				entries = themes,
				entryValues = themeValues,
				value = theme,
				iconRes = R.drawable.ic_theme_mode,
				onValueChange = {
					theme = it
					prefs.edit { putString(AppSettings.KEY_THEME, it) }
					AppCompatDelegate.setDefaultNightMode(settings.theme)
				},
			)
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.black_dark_theme,
				summaryRes = R.string.black_dark_theme_summary,
				iconRes = R.drawable.ic_dark_mode,
				checked = amoled,
				onCheckedChange = {
					amoled = it
					prefs.edit { putBoolean(AppSettings.KEY_THEME_AMOLED, it) }
					viewModel.recreateAll()
				},
			)
		}
		item {
			val localeSummary = AppCompatDelegate.getApplicationLocales().get(0)?.let { lc ->
				lc.getDisplayName(lc).toTitleCase(lc)
			} ?: stringResource(R.string.follow_system)
			ListPreferenceItem(
				titleRes = R.string.language,
				entries = localeEntries,
				entryValues = localeValues,
				value = locale,
				iconRes = R.drawable.ic_language,
				summaryOverride = localeSummary,
				onValueChange = {
					locale = it
					prefs.edit { putString(AppSettings.KEY_APP_LOCALE, it) }
					AppCompatDelegate.setApplicationLocales(settings.appLocales)
				},
			)
		}
		item { PreferenceCategoryHeader(R.string.manga_list) }
		item {
			ListPreferenceItem(
				titleRes = R.string.list_mode,
				entries = listModes,
				entryValues = listModeValues,
				value = listMode,
				iconRes = R.drawable.ic_list,
				onValueChange = {
					listMode = it
					prefs.edit { putString(AppSettings.KEY_LIST_MODE, it) }
				},
			)
		}
		item {
			SliderPreferenceItem(
				titleRes = R.string.grid_size,
				value = gridSize,
				valueFrom = 50,
				valueTo = 150,
				step = 5,
				iconRes = R.drawable.ic_grid,
				summary = "$gridSize%",
				onValueChange = {
					gridSize = it
					prefs.edit { putInt(AppSettings.KEY_GRID_SIZE, it) }
				},
			)
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.show_quick_filters,
				summaryRes = R.string.show_quick_filters_summary,
				iconRes = R.drawable.ic_filter_menu,
				checked = quickFilter,
				onCheckedChange = {
					quickFilter = it
					prefs.edit { putBoolean(AppSettings.KEY_QUICK_FILTER, it) }
				},
			)
		}
		item {
			ListPreferenceItem(
				titleRes = R.string.show_reading_indicators,
				entries = progressIndicators,
				entryValues = progressValues,
				value = progressMode,
				iconRes = R.drawable.ic_timelapse,
				onValueChange = {
					progressMode = it
					prefs.edit { putString(AppSettings.KEY_PROGRESS_INDICATORS, it) }
				},
			)
		}
		item {
			MultiSelectPreferenceItem(
				titleRes = R.string.badges_in_lists,
				entries = listBadges,
				entryValues = listBadgeValues,
				values = badges,
				iconRes = R.drawable.ic_new,
				emptySummaryRes = R.string.none,
				onValuesChange = {
					badges = it
					prefs.edit { putStringSet(AppSettings.KEY_MANGA_LIST_BADGES, it) }
				},
			)
		}
		item { PreferenceCategoryHeader(R.string.details) }
		item {
			SwitchPreferenceItem(
				titleRes = R.string.collapse_long_description,
				iconRes = R.drawable.ic_expand_more,
				checked = descriptionCollapse,
				onCheckedChange = {
					descriptionCollapse = it
					prefs.edit { putBoolean(AppSettings.KEY_COLLAPSE_DESCRIPTION, it) }
				},
			)
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.show_pages_thumbs,
				summaryRes = R.string.show_pages_thumbs_summary,
				iconRes = R.drawable.ic_images,
				checked = pagesTab,
				onCheckedChange = {
					pagesTab = it
					prefs.edit { putBoolean(AppSettings.KEY_PAGES_TAB, it) }
				},
			)
		}
		if (pagesTab) {
			item {
				ListPreferenceItem(
					titleRes = R.string.default_tab,
					entries = detailsTabs,
					entryValues = detailsTabValues,
					value = detailsTab,
					iconRes = R.drawable.ic_list_detailed,
					onValueChange = {
						detailsTab = it
						prefs.edit { putString(AppSettings.KEY_DETAILS_TAB, it) }
					},
				)
			}
		}
		item { PreferenceCategoryHeader(R.string.main_screen) }
		item {
			MultiSelectPreferenceItem(
				titleRes = R.string.search_suggestions,
				entries = suggestEntries,
				entryValues = suggestValues,
				values = searchSuggest,
				iconRes = R.drawable.ic_suggestion,
				emptySummaryRes = R.string.none,
				onValuesChange = {
					searchSuggest = it
					prefs.edit { putStringSet(AppSettings.KEY_SEARCH_SUGGESTION_TYPES, it) }
				},
			)
		}
		item {
			NavigationPreferenceItem(
				titleRes = R.string.main_screen_sections,
				summary = navSummary,
				iconRes = R.drawable.ic_drawer_menu,
				onClick = onOpenNavConfig,
			)
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.main_screen_fab,
				summaryRes = R.string.main_screen_fab_summary,
				iconRes = R.drawable.ic_add,
				checked = mainFab,
				onCheckedChange = {
					mainFab = it
					prefs.edit { putBoolean(AppSettings.KEY_MAIN_FAB, it) }
				},
			)
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.show_labels_in_navbar,
				iconRes = R.drawable.ic_tag,
				checked = navLabels,
				onCheckedChange = {
					navLabels = it
					prefs.edit { putBoolean(AppSettings.KEY_NAV_LABELS, it) }
				},
			)
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.floating_nav_bar,
				summaryRes = R.string.floating_nav_bar_summary,
				iconRes = R.drawable.ic_nav_bar,
				checked = floatingNav,
				onCheckedChange = {
					floatingNav = it
					prefs.edit { putBoolean(AppSettings.KEY_FLOATING_NAV, it) }
				},
			)
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.pin_navigation_ui,
				summaryRes = R.string.pin_navigation_ui_summary,
				iconRes = R.drawable.ic_pin,
				checked = navPinned,
				onCheckedChange = {
					navPinned = it
					prefs.edit { putBoolean(AppSettings.KEY_NAV_PINNED, it) }
				},
			)
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.exit_confirmation,
				summaryRes = R.string.exit_confirmation_summary,
				iconRes = R.drawable.ic_alert_outline,
				checked = exitConfirm,
				onCheckedChange = {
					exitConfirm = it
					prefs.edit { putBoolean(AppSettings.KEY_EXIT_CONFIRM, it) }
				},
			)
		}
		if (viewModel.appShortcutManager.isDynamicShortcutsAvailable()) {
			item {
				SwitchPreferenceItem(
					titleRes = R.string.history_shortcuts,
					summaryRes = R.string.history_shortcuts_summary,
					iconRes = R.drawable.ic_shortcut,
					checked = shortcuts,
					onCheckedChange = {
						shortcuts = it
						prefs.edit { putBoolean(AppSettings.KEY_SHORTCUTS, it) }
					},
				)
			}
			if (shortcuts) {
				item {
					SwitchPreferenceItem(
						titleRes = R.string.exclude_nsfw_from_shortcuts,
						summaryRes = R.string.exclude_nsfw_from_shortcuts_summary,
						iconRes = R.drawable.ic_sfw,
						checked = shortcutsNoNsfw,
						onCheckedChange = {
							shortcutsNoNsfw = it
							prefs.edit { putBoolean(AppSettings.KEY_SHORTCUTS_NO_NSFW, it) }
						},
					)
				}
			}
		}
		item { PreferenceCategoryHeader(R.string.privacy) }
		item {
			SwitchPreferenceItem(
				titleRes = R.string.protect_application,
				summaryRes = R.string.protect_application_summary,
				iconRes = R.drawable.ic_lock,
				checked = protectApp,
				onCheckedChange = { checked ->
					if (checked) {
						context.startActivity(Intent(context, ProtectSetupActivity::class.java))
					} else {
						settings.appPassword = null
						protectApp = false
					}
				},
			)
		}
		item {
			ListPreferenceItem(
				titleRes = R.string.screenshots_policy,
				entries = screenshots,
				entryValues = screenshotValues,
				value = screenshotsPolicy,
				iconRes = R.drawable.ic_data_privacy,
				onValueChange = {
					screenshotsPolicy = it
					prefs.edit { putString(AppSettings.KEY_SCREENSHOTS_POLICY, it) }
				},
			)
		}
	}
}
