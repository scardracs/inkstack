package org.inkstack.settings.compose.sections

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.core.content.edit
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.inkstack.R
import org.inkstack.core.nav.router
import org.inkstack.core.prefs.AppSettings
import org.inkstack.core.prefs.TriStateOption
import org.inkstack.core.util.ext.findActivity
import org.inkstack.core.util.ext.getQuantityStringSafe
import org.inkstack.explore.data.SourcesSortOrder
import org.inkstack.parsers.util.names
import org.inkstack.settings.compose.prefs.ListPreferenceItem
import org.inkstack.settings.compose.prefs.NavigationPreferenceItem
import org.inkstack.settings.compose.prefs.SwitchPreferenceItem
import org.inkstack.settings.compose.prefs.rememberSharedPreferences
import org.inkstack.settings.sources.SourcesSettingsViewModel

@Composable
fun SourcesSettingsScreen(
	onBack: () -> Unit,
	onOpenManage: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: SourcesSettingsViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val prefs = rememberSharedPreferences()
	val activity = context.findActivity() as? FragmentActivity
	val enabledCount by viewModel.enabledSourcesCount.collectAsState()
	val availableCount by viewModel.availableSourcesCount.collectAsState()
	val linksEnabled by viewModel.isLinksEnabled.collectAsState()
	val sortEntries = remember {
		SourcesSortOrder.entries.map { context.getString(it.titleResId) }.toTypedArray()
	}
	val sortValues = remember { SourcesSortOrder.entries.names() }
	val incognitoEntries = stringArrayResource(R.array.incognito_nsfw_options)
	val incognitoValues = remember { TriStateOption.entries.names() }

	var sortOrder by remember {
		mutableStateOf(
			prefs.getString(AppSettings.KEY_SOURCES_ORDER, SourcesSortOrder.SCORE.name)
				?: SourcesSortOrder.SCORE.name,
		)
	}
	var grid by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_SOURCES_GRID, true)) }
	var enableAll by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_SOURCES_ENABLED_ALL, false)) }
	var noNsfw by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_DISABLE_NSFW, false)) }
	var incognito by remember {
		mutableStateOf(
			prefs.getString(AppSettings.KEY_INCOGNITO_NSFW, TriStateOption.ASK.name)
				?: TriStateOption.ASK.name,
		)
	}
	var mirror by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_MIRROR_SWITCHING, false)) }

	val manageSummary = if (enabledCount >= 0) {
		context.resources.getQuantityStringSafe(R.plurals.items, enabledCount, enabledCount)
	} else null
	val catalogSummary = when {
		availableCount == 0 -> stringResource(R.string.all_sources_enabled)
		availableCount > 0 -> stringResource(R.string.available_d, availableCount)
		else -> null
	}

	SettingsSectionScaffold(titleRes = R.string.remote_sources, onBack = onBack, modifier = modifier) {
		item {
			ListPreferenceItem(
				titleRes = R.string.sort_order,
				entries = sortEntries,
				entryValues = sortValues,
				value = sortOrder,
				iconRes = R.drawable.ic_sort_asc,
				onValueChange = {
					sortOrder = it
					prefs.edit { putString(AppSettings.KEY_SOURCES_ORDER, it) }
				},
			)
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.show_in_grid_view,
				iconRes = R.drawable.ic_grid,
				checked = grid,
				onCheckedChange = {
					grid = it
					prefs.edit { putBoolean(AppSettings.KEY_SOURCES_GRID, it) }
				},
			)
		}
		item {
			NavigationPreferenceItem(
				titleRes = R.string.manage_sources,
				summary = manageSummary,
				iconRes = R.drawable.ic_manga_source,
				onClick = onOpenManage,
			)
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.enable_all_sources,
				summaryRes = R.string.enable_all_sources_summary,
				iconRes = R.drawable.ic_check,
				checked = enableAll,
				onCheckedChange = {
					enableAll = it
					prefs.edit { putBoolean(AppSettings.KEY_SOURCES_ENABLED_ALL, it) }
				},
			)
		}
		item {
			NavigationPreferenceItem(
				titleRes = R.string.sources_catalog,
				summary = catalogSummary,
				iconRes = R.drawable.ic_list,
				enabled = !enableAll,
				onClick = { activity?.router?.openSourcesCatalog() },
			)
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.disable_nsfw,
				summaryRes = R.string.disable_nsfw_summary,
				iconRes = R.drawable.ic_sfw,
				checked = noNsfw,
				onCheckedChange = {
					noNsfw = it
					prefs.edit { putBoolean(AppSettings.KEY_DISABLE_NSFW, it) }
				},
			)
		}
		item {
			ListPreferenceItem(
				titleRes = R.string.incognito_for_nsfw,
				entries = incognitoEntries,
				entryValues = incognitoValues,
				value = incognito,
				iconRes = R.drawable.ic_incognito,
				onValueChange = {
					incognito = it
					prefs.edit { putString(AppSettings.KEY_INCOGNITO_NSFW, it) }
				},
			)
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.mirror_switching,
				summaryRes = R.string.mirror_switching_summary,
				iconRes = R.drawable.ic_replace,
				checked = mirror,
				onCheckedChange = {
					mirror = it
					prefs.edit { putBoolean(AppSettings.KEY_MIRROR_SWITCHING, it) }
				},
			)
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.handle_links,
				summaryRes = R.string.handle_links_summary,
				iconRes = R.drawable.ic_open_external,
				checked = linksEnabled,
				onCheckedChange = { viewModel.setLinksEnabled(it) },
			)
		}
	}
}
