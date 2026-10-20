package org.inkstack.settings.compose.sections

import android.content.Context
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.inkstack.R
import org.inkstack.core.nav.router
import org.inkstack.core.prefs.AppSettings
import org.inkstack.core.prefs.DownloadFormat
import org.inkstack.core.prefs.TriStateOption
import org.inkstack.core.util.ext.findActivity
import org.inkstack.core.util.ext.getQuantityStringSafe
import org.inkstack.core.util.ext.printStackTraceDebug
import org.inkstack.core.util.ext.resolveFile
import org.inkstack.download.ui.worker.DownloadWorker
import org.inkstack.local.data.LocalStorageManager
import org.inkstack.parsers.util.names
import org.inkstack.settings.compose.prefs.InfoPreferenceItem
import org.inkstack.settings.compose.prefs.ListPreferenceItem
import org.inkstack.settings.compose.prefs.NavigationPreferenceItem
import org.inkstack.settings.compose.prefs.PreferenceCategoryHeader
import org.inkstack.settings.compose.prefs.SwitchPreferenceItem
import org.inkstack.settings.compose.prefs.rememberSharedPreferences
import javax.inject.Inject

@HiltViewModel
class DownloadsSettingsComposeViewModel @Inject constructor(
	val settings: AppSettings,
	val storageManager: LocalStorageManager,
	val downloadsScheduler: DownloadWorker.Scheduler,
) : ViewModel() {
	fun updateConstraints() {
		viewModelScope.launch(Dispatchers.Default) {
			try {
				val option = when (settings.allowDownloadOnMeteredNetwork) {
					TriStateOption.ENABLED -> true
					TriStateOption.ASK -> return@launch
					TriStateOption.DISABLED -> false
				}
				downloadsScheduler.updateConstraints(option)
			} catch (e: Exception) {
				e.printStackTraceDebug()
			}
		}
	}
}

@Composable
fun DownloadsSettingsScreen(
	onBack: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: DownloadsSettingsComposeViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val prefs = rememberSharedPreferences()
	val scope = rememberCoroutineScope()
	val snackbarHostState = remember { SnackbarHostState() }
	val formats = stringArrayResource(R.array.download_formats)
	val metered = stringArrayResource(R.array.metered_network_options)
	val formatValues = remember { DownloadFormat.entries.names() }
	val meteredValues = remember { TriStateOption.entries.names() }

	var format by remember {
		mutableStateOf(
			prefs.getString(AppSettings.KEY_DOWNLOADS_FORMAT, DownloadFormat.AUTOMATIC.name)
				?: DownloadFormat.AUTOMATIC.name,
		)
	}
	var meteredNetwork by remember {
		mutableStateOf(
			prefs.getString(AppSettings.KEY_DOWNLOADS_METERED_NETWORK, TriStateOption.ASK.name)
				?: TriStateOption.ASK.name,
		)
	}
	var pagesAsk by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_PAGES_SAVE_ASK, true)) }
	var storageSummary by remember { mutableStateOf(context.getString(R.string.loading_)) }
	var dirsSummary by remember { mutableStateOf(context.getString(R.string.loading_)) }
	var pagesSummary by remember { mutableStateOf(context.getString(androidx.preference.R.string.not_set)) }
	var showDoze by remember { mutableStateOf(false) }

	val pickTree = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
		if (uri == null) return@rememberLauncherForActivityResult
		viewModel.storageManager.takePermissions(uri)
		val doc = DocumentFile.fromTreeUri(context, uri)?.takeIf { it.canWrite() }
		viewModel.settings.setPagesSaveDir(doc?.uri)
		pagesSummary = doc?.getDisplayPath(context) ?: context.getString(androidx.preference.R.string.not_set)
	}

	LaunchedEffect(Unit) {
		val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
		showDoze = !pm.isIgnoringBatteryOptimizations(context.packageName)
		storageSummary = withContext(Dispatchers.Default) {
			val storage = viewModel.storageManager.getDefaultWriteableDir()
			if (storage != null) {
				viewModel.storageManager.getDirectoryDisplayName(storage, isFullPath = true)
			} else {
				context.getString(R.string.not_available)
			}
		}
		dirsSummary = withContext(Dispatchers.Default) {
			val dirs = viewModel.storageManager.getReadableDirs().size
			context.resources.getQuantityStringSafe(R.plurals.items, dirs, dirs)
		}
		pagesSummary = withContext(Dispatchers.IO) {
			viewModel.settings.getPagesSaveDir(context)?.getDisplayPath(context)
				?: context.getString(androidx.preference.R.string.not_set)
		}
	}

	val activity = context.findActivity() as? FragmentActivity

	SettingsSectionScaffold(
		titleRes = R.string.downloads,
		onBack = onBack,
		modifier = modifier,
		snackbarHostState = snackbarHostState,
	) {
		item {
			NavigationPreferenceItem(
				titleRes = R.string.local_manga_directories,
				summary = dirsSummary,
				iconRes = R.drawable.ic_folder_file,
				onClick = { activity?.router?.openDirectoriesSettings() },
			)
		}
		item {
			NavigationPreferenceItem(
				titleRes = R.string.manga_save_location,
				summary = storageSummary,
				iconRes = R.drawable.ic_storage,
				onClick = { activity?.router?.showDirectorySelectDialog() },
			)
		}
		item {
			ListPreferenceItem(
				titleRes = R.string.preferred_download_format,
				entries = formats,
				entryValues = formatValues,
				value = format,
				iconRes = R.drawable.ic_file_zip,
				onValueChange = {
					format = it
					prefs.edit { putString(AppSettings.KEY_DOWNLOADS_FORMAT, it) }
				},
			)
		}
		item {
			ListPreferenceItem(
				titleRes = R.string.download_over_cellular,
				entries = metered,
				entryValues = meteredValues,
				value = meteredNetwork,
				iconRes = R.drawable.ic_network_cellular,
				onValueChange = {
					meteredNetwork = it
					prefs.edit { putString(AppSettings.KEY_DOWNLOADS_METERED_NETWORK, it) }
					viewModel.updateConstraints()
				},
			)
		}
		item {
			InfoPreferenceItem(
				summary = stringResource(R.string.downloads_settings_info),
				iconRes = R.drawable.ic_info_outline,
			)
		}
		if (showDoze) {
			item {
				NavigationPreferenceItem(
					titleRes = R.string.disable_battery_optimization,
					summaryRes = R.string.disable_battery_optimization_summary_downloads,
					iconRes = R.drawable.ic_battery_outline,
					onClick = {
						try {
							context.startActivity(
								android.content.Intent(
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
		item { PreferenceCategoryHeader(R.string.pages_saving) }
		item {
			NavigationPreferenceItem(
				titleRes = R.string.default_page_save_dir,
				summary = pagesSummary,
				iconRes = R.drawable.ic_folder_file,
				onClick = {
					try {
						pickTree.launch(viewModel.settings.getPagesSaveDir(context)?.uri)
					} catch (_: Exception) {
						Toast.makeText(context, R.string.operation_not_supported, Toast.LENGTH_SHORT).show()
					}
				},
			)
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.ask_for_dest_dir_every_time,
				iconRes = R.drawable.ic_edit,
				checked = pagesAsk,
				onCheckedChange = {
					pagesAsk = it
					prefs.edit { putBoolean(AppSettings.KEY_PAGES_SAVE_ASK, it) }
				},
			)
		}
	}
}

private fun DocumentFile.getDisplayPath(context: Context): String {
	return uri.resolveFile(context)?.path ?: uri.toString()
}
