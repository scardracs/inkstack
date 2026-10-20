package org.inkstack.settings.compose.sections

import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import org.inkstack.R
import org.inkstack.core.nav.router
import org.inkstack.core.prefs.AppSettings
import org.inkstack.core.prefs.TrackerDownloadStrategy
import org.inkstack.core.util.ext.findActivity
import org.inkstack.parsers.util.names
import org.inkstack.settings.compose.prefs.InfoPreferenceItem
import org.inkstack.settings.compose.prefs.ListPreferenceItem
import org.inkstack.settings.compose.prefs.MultiSelectPreferenceItem
import org.inkstack.settings.compose.prefs.NavigationPreferenceItem
import org.inkstack.settings.compose.prefs.PreferenceCategoryHeader
import org.inkstack.settings.compose.prefs.SwitchPreferenceItem
import org.inkstack.settings.compose.prefs.rememberSharedPreferences
import org.inkstack.settings.tracker.TrackerSettingsViewModel
import org.inkstack.tracker.ui.debug.TrackerDebugActivity
import org.inkstack.tracker.work.TrackerNotificationHelper
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface TrackerNotificationEntryPoint {
	fun trackerNotificationHelper(): TrackerNotificationHelper
}

@Composable
fun TrackerSettingsScreen(
	onBack: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: TrackerSettingsViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val prefs = rememberSharedPreferences()
	val scope = rememberCoroutineScope()
	val snackbarHostState = remember { SnackbarHostState() }
	val activity = context.findActivity() as? FragmentActivity
	val notificationHelper = remember {
		EntryPointAccessors.fromApplication(context, TrackerNotificationEntryPoint::class.java)
			.trackerNotificationHelper()
	}
	val categoriesCount by viewModel.categoriesCount.collectAsState()
	val trackSourcesEntries = stringArrayResource(R.array.track_sources)
	val trackSourcesValues = stringArrayResource(R.array.values_track_sources)
	val trackSourcesDefault = stringArrayResource(R.array.values_track_sources_default)
	val frequencyEntries = stringArrayResource(R.array.tracker_frequency)
	val frequencyValues = stringArrayResource(R.array.values_tracker_frequency)
	val downloadEntries = stringArrayResource(R.array.tracker_download_strategies)
	val downloadValues = remember { TrackerDownloadStrategy.entries.names() }

	var enabled by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_TRACKER_ENABLED, true)) }
	var wifiOnly by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_TRACKER_WIFI_ONLY, false)) }
	var frequency by remember { mutableStateOf(prefs.getString(AppSettings.KEY_TRACKER_FREQUENCY, "1") ?: "1") }
	var trackSources by remember {
		mutableStateOf(
			prefs.getStringSet(AppSettings.KEY_TRACK_SOURCES, trackSourcesDefault.toSet())?.toSet()
				?: trackSourcesDefault.toSet(),
		)
	}
	var noNsfw by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_TRACKER_NO_NSFW, false)) }
	var downloadStrategy by remember {
		mutableStateOf(
			prefs.getString(AppSettings.KEY_TRACKER_DOWNLOAD, TrackerDownloadStrategy.DISABLED.name)
				?: TrackerDownloadStrategy.DISABLED.name,
		)
	}
	var notifSummary by remember { mutableStateOf("") }
	var showDoze by remember { mutableStateOf(false) }
	val categoriesEnabled = enabled && AppSettings.TRACK_FAVOURITES in trackSources
	val categoriesSummary = categoriesCount?.let {
		stringResource(R.string.enabled_d_of_d, it[0], it[1])
	}

	LaunchedEffect(Unit) {
		notifSummary = context.getString(
			if (notificationHelper.getAreNotificationsEnabled()) {
				R.string.show_notification_new_chapters_on
			} else {
				R.string.show_notification_new_chapters_off
			},
		)
		val pm = context.getSystemService(android.content.Context.POWER_SERVICE) as PowerManager
		showDoze = !pm.isIgnoringBatteryOptimizations(context.packageName)
	}

	SettingsSectionScaffold(
		titleRes = R.string.check_for_new_chapters,
		onBack = onBack,
		modifier = modifier,
		snackbarHostState = snackbarHostState,
	) {
		item {
			SwitchPreferenceItem(
				titleRes = R.string.check_new_chapters_title,
				iconRes = R.drawable.ic_feed,
				checked = enabled,
				onCheckedChange = {
					enabled = it
					prefs.edit { putBoolean(AppSettings.KEY_TRACKER_ENABLED, it) }
				},
			)
		}
		if (enabled) {
			item {
				SwitchPreferenceItem(
					titleRes = R.string.only_using_wifi,
					summaryRes = R.string.tracker_wifi_only_summary,
					iconRes = R.drawable.ic_wifi,
					checked = wifiOnly,
					onCheckedChange = {
						wifiOnly = it
						prefs.edit { putBoolean(AppSettings.KEY_TRACKER_WIFI_ONLY, it) }
					},
				)
			}
			item {
				ListPreferenceItem(
					titleRes = R.string.frequency_of_check,
					entries = frequencyEntries,
					entryValues = frequencyValues,
					value = frequency,
					iconRes = R.drawable.ic_timer,
					onValueChange = {
						frequency = it
						prefs.edit { putString(AppSettings.KEY_TRACKER_FREQUENCY, it) }
					},
				)
			}
			item {
				MultiSelectPreferenceItem(
					titleRes = R.string.track_sources,
					entries = trackSourcesEntries,
					entryValues = trackSourcesValues,
					values = trackSources,
					iconRes = R.drawable.ic_manga_source,
					emptySummaryRes = R.string.dont_check,
					onValuesChange = {
						trackSources = it
						prefs.edit { putStringSet(AppSettings.KEY_TRACK_SOURCES, it) }
					},
				)
			}
			item {
				NavigationPreferenceItem(
					titleRes = R.string.favourites_categories,
					summary = categoriesSummary,
					iconRes = R.drawable.ic_heart,
					enabled = categoriesEnabled,
					onClick = { activity?.router?.showTrackerCategoriesConfigSheet() },
				)
			}
			item {
				NavigationPreferenceItem(
					titleRes = R.string.notifications_settings,
					summary = notifSummary,
					iconRes = R.drawable.ic_notification,
					onClick = {
						try {
							context.startActivity(
								Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
									.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName),
							)
						} catch (_: Exception) {
							scope.launch {
								snackbarHostState.showSnackbar(context.getString(R.string.operation_not_supported))
							}
						}
					},
				)
			}
			item {
				SwitchPreferenceItem(
					titleRes = R.string.disable_nsfw_notifications,
					summaryRes = R.string.disable_nsfw_notifications_summary,
					iconRes = R.drawable.ic_sfw,
					checked = noNsfw,
					onCheckedChange = {
						noNsfw = it
						prefs.edit { putBoolean(AppSettings.KEY_TRACKER_NO_NSFW, it) }
					},
				)
			}
			item {
				ListPreferenceItem(
					titleRes = R.string.download_new_chapters,
					entries = downloadEntries,
					entryValues = downloadValues,
					value = downloadStrategy,
					iconRes = R.drawable.ic_download,
					onValueChange = {
						downloadStrategy = it
						prefs.edit { putString(AppSettings.KEY_TRACKER_DOWNLOAD, it) }
					},
				)
			}
			item { PreferenceCategoryHeader(R.string.debug) }
			item {
				NavigationPreferenceItem(
					titleRes = R.string.tracker_debug_info,
					summaryRes = R.string.tracker_debug_info_summary,
					iconRes = R.drawable.ic_script,
					onClick = {
						context.startActivity(Intent(context, TrackerDebugActivity::class.java))
					},
				)
			}
			if (showDoze) {
				item {
					NavigationPreferenceItem(
						titleRes = R.string.disable_battery_optimization,
						summaryRes = R.string.disable_battery_optimization_summary,
						iconRes = R.drawable.ic_battery_outline,
						onClick = {
							try {
								context.startActivity(
									Intent(
										Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
										"package:${context.packageName}".toUri(),
									),
								)
							} catch (_: Exception) {
								scope.launch {
									snackbarHostState.showSnackbar(context.getString(R.string.operation_not_supported))
								}
							}
						},
					)
				}
			}
			item {
				InfoPreferenceItem(
					summary = stringResource(R.string.tracker_warning) + " https://dontkillmyapp.com/",
					iconRes = R.drawable.ic_info_outline,
				)
			}
		}
	}
}
