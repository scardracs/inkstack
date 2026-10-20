package org.inkstack.settings.compose.sections

import android.content.Intent
import android.text.format.DateUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.inkstack.R
import org.inkstack.backups.ui.periodical.PeriodicalBackupSettingsViewModel
import org.inkstack.backups.ui.periodical.TelegramBackupUploader
import org.inkstack.core.nav.router
import org.inkstack.core.prefs.AppSettings
import org.inkstack.core.util.ext.findActivity
import org.inkstack.core.util.ext.getDisplayMessage
import org.inkstack.settings.compose.prefs.EditTextPreferenceItem
import org.inkstack.settings.compose.prefs.ListPreferenceItem
import org.inkstack.settings.compose.prefs.NavigationPreferenceItem
import org.inkstack.settings.compose.prefs.PreferenceCategoryHeader
import org.inkstack.settings.compose.prefs.SliderPreferenceItem
import org.inkstack.settings.compose.prefs.SwitchPreferenceItem
import org.inkstack.settings.compose.prefs.rememberSharedPreferences

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PeriodicalBackupEntryPoint {
	fun telegramBackupUploader(): TelegramBackupUploader
	fun appSettings(): AppSettings
}

@Composable
fun PeriodicalBackupSettingsScreen(
	onBack: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: PeriodicalBackupSettingsViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val prefs = rememberSharedPreferences()
	val scope = rememberCoroutineScope()
	val snackbarHostState = remember { SnackbarHostState() }
	val activity = context.findActivity() as? FragmentActivity
	val entryPoint = remember {
		EntryPointAccessors.fromApplication(context, PeriodicalBackupEntryPoint::class.java)
	}
	val uploader = remember { entryPoint.telegramBackupUploader() }
	val settings = remember { entryPoint.appSettings() }
	val lastBackup by viewModel.lastBackupDate.collectAsState()
	val outputPath by viewModel.backupsDirectory.collectAsState()
	val tgLoading by viewModel.isTelegramCheckLoading.collectAsState()
	val freqEntries = stringArrayResource(R.array.backup_frequency)
	val freqValues = stringArrayResource(R.array.values_backup_frequency)

	var enabled by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_BACKUP_PERIODICAL_ENABLED, false)) }
	var frequency by remember {
		mutableStateOf(prefs.getString(AppSettings.KEY_BACKUP_PERIODICAL_FREQUENCY, "7") ?: "7")
	}
	var trim by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_BACKUP_PERIODICAL_TRIM, true)) }
	var count by remember { mutableStateOf(prefs.getInt(AppSettings.KEY_BACKUP_PERIODICAL_COUNT, 10)) }
	var tgEnabled by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_BACKUP_TG_ENABLED, false)) }
	var tgChat by remember { mutableStateOf(prefs.getString(AppSettings.KEY_BACKUP_TG_CHAT, "").orEmpty()) }

	val pickOutput = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
		if (uri != null) {
			val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
			context.contentResolver.takePersistableUriPermission(uri, takeFlags)
			settings.periodicalBackupDirectory = uri
			viewModel.updateSummaryData()
		}
	}

	LaunchedEffect(viewModel) {
		viewModel.onError.collectLatest { event ->
			event?.consume { error ->
				snackbarHostState.showSnackbar(error.getDisplayMessage(context.resources))
			}
		}
	}

	SettingsSectionScaffold(
		titleRes = R.string.periodic_backups,
		onBack = onBack,
		modifier = modifier,
		snackbarHostState = snackbarHostState,
	) {
		item {
			SwitchPreferenceItem(
				titleRes = R.string.periodic_backups_enable,
				iconRes = R.drawable.ic_timer_run,
				checked = enabled,
				onCheckedChange = {
					enabled = it
					prefs.edit { putBoolean(AppSettings.KEY_BACKUP_PERIODICAL_ENABLED, it) }
				},
			)
		}
		if (enabled) {
			item {
				NavigationPreferenceItem(
					titleRes = R.string.backups_output_directory,
					summary = when (outputPath) {
						null -> stringResource(R.string.invalid_value_message)
						"" -> null
						else -> outputPath
					},
					iconRes = R.drawable.ic_folder_file,
					onClick = {
						try {
							pickOutput.launch(null)
						} catch (_: Exception) {
							scope.launch {
								snackbarHostState.showSnackbar(context.getString(R.string.operation_not_supported))
							}
						}
					},
				)
			}
			item {
				ListPreferenceItem(
					titleRes = R.string.backup_frequency,
					entries = freqEntries,
					entryValues = freqValues,
					value = frequency,
					iconRes = R.drawable.ic_timer,
					onValueChange = {
						frequency = it
						prefs.edit { putString(AppSettings.KEY_BACKUP_PERIODICAL_FREQUENCY, it) }
					},
				)
			}
			item {
				SwitchPreferenceItem(
					titleRes = R.string.delete_old_backups,
					summaryRes = R.string.delete_old_backups_summary,
					iconRes = R.drawable.ic_delete_all,
					checked = trim,
					onCheckedChange = {
						trim = it
						prefs.edit { putBoolean(AppSettings.KEY_BACKUP_PERIODICAL_TRIM, it) }
					},
				)
			}
			if (trim) {
				item {
					SliderPreferenceItem(
						titleRes = R.string.max_backups_count,
						value = count,
						valueFrom = 1,
						valueTo = 32,
						step = 1,
						iconRes = R.drawable.ic_tag,
						summary = count.toString(),
						onValueChange = {
							count = it
							prefs.edit { putInt(AppSettings.KEY_BACKUP_PERIODICAL_COUNT, it) }
						},
					)
				}
			}
			lastBackup?.let { date ->
				item {
					NavigationPreferenceItem(
						title = stringResource(
							R.string.last_successful_backup,
							DateUtils.getRelativeTimeSpanString(date.time),
						),
						iconRes = R.drawable.ic_info_outline,
						enabled = false,
						onClick = {},
					)
				}
			}
			if (viewModel.isTelegramAvailable) {
				item { PreferenceCategoryHeader(R.string.telegram_integration) }
				item {
					SwitchPreferenceItem(
						titleRes = R.string.send_backups_telegram,
						iconRes = R.drawable.ic_notification,
						checked = tgEnabled,
						onCheckedChange = {
							tgEnabled = it
							prefs.edit { putBoolean(AppSettings.KEY_BACKUP_TG_ENABLED, it) }
						},
					)
				}
				if (tgEnabled) {
					item {
						EditTextPreferenceItem(
							titleRes = R.string.telegram_chat_id,
							value = tgChat,
							summary = tgChat.ifEmpty { stringResource(R.string.telegram_chat_id_summary) },
							iconRes = R.drawable.ic_tag,
							onValueChange = {
								tgChat = it
								prefs.edit { putString(AppSettings.KEY_BACKUP_TG_CHAT, it) }
							},
						)
					}
					item {
						NavigationPreferenceItem(
							titleRes = R.string.open_telegram_bot,
							summaryRes = R.string.open_telegram_bot_summary,
							iconRes = R.drawable.ic_open_external,
							onClick = {
								val router = activity?.router ?: return@NavigationPreferenceItem
								if (!uploader.openBotInApp(router)) {
									scope.launch {
										snackbarHostState.showSnackbar(context.getString(R.string.operation_not_supported))
									}
								}
							},
						)
					}
					item {
						NavigationPreferenceItem(
							titleRes = R.string.test_connection,
							iconRes = R.drawable.ic_retry,
							enabled = !tgLoading,
							onClick = { viewModel.checkTelegram() },
						)
					}
				}
			}
		}
	}
}
