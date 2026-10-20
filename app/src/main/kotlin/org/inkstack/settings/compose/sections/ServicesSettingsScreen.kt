package org.inkstack.settings.compose.sections

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.content.edit
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.inkstack.R
import org.inkstack.core.nav.router
import org.inkstack.core.prefs.AppSettings
import org.inkstack.core.util.ext.findActivity
import org.inkstack.core.util.ext.getDisplayMessage
import org.inkstack.core.util.ext.printStackTraceDebug
import org.inkstack.scrobbling.common.domain.model.ScrobblerService
import org.inkstack.scrobbling.common.ui.ScrobblerAuthHelper
import org.inkstack.settings.compose.prefs.NavigationPreferenceItem
import org.inkstack.settings.compose.prefs.PreferenceCategoryHeader
import org.inkstack.settings.compose.prefs.SwitchPreferenceItem
import org.inkstack.settings.compose.prefs.rememberSharedPreferences
import org.inkstack.sync.domain.SyncController
import javax.inject.Inject

@HiltViewModel
class ServicesSettingsComposeViewModel @Inject constructor(
	val settings: AppSettings,
	val syncController: SyncController,
	val scrobblerAuthHelper: ScrobblerAuthHelper,
) : ViewModel()

@Composable
fun ServicesSettingsScreen(
	onBack: () -> Unit,
	onOpenSync: () -> Unit,
	onOpenCommunity: () -> Unit,
	onOpenSuggestions: () -> Unit,
	onOpenDiscord: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: ServicesSettingsComposeViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val prefs = rememberSharedPreferences()
	val scope = rememberCoroutineScope()
	val snackbarHostState = remember { SnackbarHostState() }
	val activity = context.findActivity() as? FragmentActivity
	var related by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_RELATED_MANGA, true)) }
	var stats by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_STATS_ENABLED, false)) }
	var readingTime by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_READING_TIME, true)) }
	var suggestionsSummary by remember {
		mutableStateOf(
			context.getString(if (viewModel.settings.isSuggestionsEnabled) R.string.enabled else R.string.disabled),
		)
	}
	var statsSummary by remember {
		mutableStateOf(context.getString(if (stats) R.string.enabled else R.string.disabled))
	}
	val summaries = remember {
		mutableStateOf(
			mapOf(
				ScrobblerService.ANILIST to context.getString(R.string.loading_),
				ScrobblerService.KITSU to context.getString(R.string.loading_),
				ScrobblerService.MAL to context.getString(R.string.loading_),
				ScrobblerService.SHIKIMORI to context.getString(R.string.loading_),
			),
		)
	}
	var authPrompt by remember { mutableStateOf<ScrobblerService?>(null) }

	fun refreshScrobbler(service: ScrobblerService) {
		scope.launch {
			val helper = viewModel.scrobblerAuthHelper
			val text = if (!helper.isAuthorized(service)) {
				context.getString(R.string.disabled)
			} else {
				helper.getCachedUser(service)?.nickname?.let {
					context.getString(R.string.logged_in_as, it)
				} ?: withContext(Dispatchers.Default) {
					runCatching {
						context.getString(R.string.logged_in_as, helper.getUser(service).nickname)
					}.getOrElse {
						it.printStackTraceDebug()
						it.getDisplayMessage(context.resources)
					}
				}
			}
			summaries.value = summaries.value + (service to text)
		}
	}

	LaunchedEffect(Unit) {
		ScrobblerService.entries.forEach { refreshScrobbler(it) }
	}

	SettingsSectionScaffold(
		titleRes = R.string.services,
		onBack = onBack,
		modifier = modifier,
		snackbarHostState = snackbarHostState,
	) {
		item {
			NavigationPreferenceItem(
				titleRes = R.string.sync,
				iconRes = R.drawable.ic_sync,
				onClick = onOpenSync,
			)
		}
		item {
			NavigationPreferenceItem(
				titleRes = R.string.community,
				summaryRes = R.string.community_summary,
				iconRes = R.drawable.ic_trending_small,
				onClick = onOpenCommunity,
			)
		}
		item {
			NavigationPreferenceItem(
				titleRes = R.string.suggestions,
				summary = suggestionsSummary,
				iconRes = R.drawable.ic_suggestion,
				onClick = onOpenSuggestions,
			)
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.related_manga,
				summaryRes = R.string.related_manga_summary,
				iconRes = R.drawable.ic_list_group,
				checked = related,
				onCheckedChange = {
					related = it
					prefs.edit { putBoolean(AppSettings.KEY_RELATED_MANGA, it) }
				},
			)
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.reading_stats,
				summary = statsSummary,
				iconRes = R.drawable.ic_chart,
				checked = stats,
				onCheckedChange = {
					stats = it
					prefs.edit { putBoolean(AppSettings.KEY_STATS_ENABLED, it) }
					statsSummary = context.getString(if (it) R.string.enabled else R.string.disabled)
					if (it) activity?.router?.openStatistic()
				},
			)
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.reading_time_estimation,
				summaryRes = R.string.reading_time_estimation_summary,
				iconRes = R.drawable.ic_timelapse,
				checked = readingTime,
				onCheckedChange = {
					readingTime = it
					prefs.edit { putBoolean(AppSettings.KEY_READING_TIME, it) }
				},
			)
		}
		item { PreferenceCategoryHeader(R.string.tracking) }
		for (service in listOf(
			ScrobblerService.ANILIST,
			ScrobblerService.KITSU,
			ScrobblerService.MAL,
			ScrobblerService.SHIKIMORI,
		)) {
			item(key = service.name) {
				NavigationPreferenceItem(
					titleRes = service.titleResId,
					summary = summaries.value[service],
					iconRes = service.iconResId,
					onClick = {
						if (!viewModel.scrobblerAuthHelper.isAuthorized(service)) {
							authPrompt = service
						} else {
							activity?.router?.openScrobblerSettings(service)
						}
					},
				)
			}
		}
		item {
			NavigationPreferenceItem(
				titleRes = R.string.discord_rpc,
				summaryRes = R.string.discord_rpc_summary,
				iconRes = R.drawable.ic_discord,
				onClick = onOpenDiscord,
			)
		}
	}

	authPrompt?.let { service ->
		AlertDialog(
			onDismissRequest = { authPrompt = null },
			icon = {
				Icon(painter = painterResource(service.iconResId), contentDescription = null)
			},
			title = { Text(stringResource(service.titleResId)) },
			text = {
				Text(
					stringResource(
						R.string.scrobbler_auth_intro,
						stringResource(service.titleResId),
					),
				)
			},
			confirmButton = {
				TextButton(
					onClick = {
						viewModel.scrobblerAuthHelper.startAuth(context, service).onFailure {
							scope.launch {
								snackbarHostState.showSnackbar(it.getDisplayMessage(context.resources))
							}
						}
						authPrompt = null
					},
				) {
					Text(stringResource(R.string.sign_in))
				}
			},
			dismissButton = {
				TextButton(onClick = { authPrompt = null }) {
					Text(stringResource(android.R.string.cancel))
				}
			},
		)
	}
}
