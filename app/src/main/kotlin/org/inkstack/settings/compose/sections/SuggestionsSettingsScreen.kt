package org.inkstack.settings.compose.sections

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.inkstack.R
import org.inkstack.core.prefs.AppSettings
import org.inkstack.core.ui.compose.InkstackScaffold
import org.inkstack.settings.compose.prefs.InfoPreferenceItem
import org.inkstack.settings.compose.prefs.SwitchPreferenceItem
import org.inkstack.suggestions.ui.SuggestionsWorker
import javax.inject.Inject

@HiltViewModel
class SuggestionsSettingsComposeViewModel @Inject constructor(
	private val suggestionsScheduler: SuggestionsWorker.Scheduler,
	val settings: AppSettings,
) : ViewModel() {
	fun refreshSuggestions() {
		if (!settings.isSuggestionsEnabled) return
		viewModelScope.launch(Dispatchers.Default) {
			suggestionsScheduler.startNow()
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionsSettingsScreen(
	onBack: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: SuggestionsSettingsComposeViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val prefs = remember { PreferenceManager.getDefaultSharedPreferences(context) }
	val settings = viewModel.settings
	var enabled by remember { mutableStateOf(settings.isSuggestionsEnabled) }
	var wifiOnly by remember { mutableStateOf(settings.isSuggestionsWiFiOnly) }
	var includeDisabled by remember { mutableStateOf(settings.isSuggestionsIncludeDisabledSources) }
	var notifications by remember { mutableStateOf(settings.isSuggestionsNotificationAvailable) }
	var excludeNsfw by remember { mutableStateOf(settings.isSuggestionsExcludeNsfw) }
	var excludeTags by remember {
		mutableStateOf(prefs.getString(AppSettings.KEY_SUGGESTIONS_EXCLUDE_TAGS, "").orEmpty())
	}
	val refresh by rememberUpdatedState(viewModel::refreshSuggestions)

	fun bump() = refresh()

	InkstackScaffold(
		modifier = modifier,
		topBar = {
			TopAppBar(
				title = { Text(stringResource(R.string.suggestions)) },
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
					}
				},
				windowInsets = WindowInsets.statusBars,
				colors = TopAppBarDefaults.topAppBarColors(),
			)
		},
	) { padding ->
		LazyColumn(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding),
		) {
			item {
				SwitchPreferenceItem(
					titleRes = R.string.suggestions_enable,
					iconRes = R.drawable.ic_suggestion,
					checked = enabled,
					onCheckedChange = {
						enabled = it
						settings.isSuggestionsEnabled = it
						bump()
					},
				)
			}
			if (enabled) {
				item {
					SwitchPreferenceItem(
						titleRes = R.string.only_using_wifi,
						summaryRes = R.string.suggestions_wifi_only_summary,
						iconRes = R.drawable.ic_wifi,
						checked = wifiOnly,
						onCheckedChange = {
							wifiOnly = it
							prefs.edit { putBoolean(AppSettings.KEY_SUGGESTIONS_WIFI_ONLY, it) }
							bump()
						},
					)
				}
				item {
					SwitchPreferenceItem(
						titleRes = R.string.include_disabled_sources,
						summaryRes = R.string.suggestions_disabled_sources_summary,
						iconRes = R.drawable.ic_manga_source,
						checked = includeDisabled,
						onCheckedChange = {
							includeDisabled = it
							prefs.edit { putBoolean(AppSettings.KEY_SUGGESTIONS_DISABLED_SOURCES, it) }
							bump()
						},
					)
				}
				item {
					SwitchPreferenceItem(
						titleRes = R.string.notifications_enable,
						summaryRes = R.string.suggestions_notifications_summary,
						iconRes = R.drawable.ic_notification,
						checked = notifications,
						onCheckedChange = {
							notifications = it
							prefs.edit { putBoolean(AppSettings.KEY_SUGGESTIONS_NOTIFICATIONS, it) }
						},
					)
				}
				item {
					OutlinedTextField(
						value = excludeTags,
						onValueChange = {
							excludeTags = it
							prefs.edit { putString(AppSettings.KEY_SUGGESTIONS_EXCLUDE_TAGS, it) }
							bump()
						},
						modifier = Modifier
							.fillMaxWidth()
							.padding(horizontal = 16.dp, vertical = 8.dp),
						label = { Text(stringResource(R.string.suggestions_excluded_genres)) },
						supportingText = {
							Text(stringResource(R.string.suggestions_excluded_genres_summary))
						},
					)
				}
				item {
					SwitchPreferenceItem(
						titleRes = R.string.exclude_nsfw_from_suggestions,
						summaryRes = R.string.exclude_nsfw_from_suggestions_summary,
						iconRes = R.drawable.ic_sfw,
						checked = excludeNsfw,
						onCheckedChange = {
							excludeNsfw = it
							prefs.edit { putBoolean(AppSettings.KEY_SUGGESTIONS_EXCLUDE_NSFW, it) }
							bump()
						},
					)
				}
				item {
					InfoPreferenceItem(
						summary = stringResource(R.string.suggestions_info),
						iconRes = R.drawable.ic_info_outline,
					)
				}
			}
		}
	}
}
