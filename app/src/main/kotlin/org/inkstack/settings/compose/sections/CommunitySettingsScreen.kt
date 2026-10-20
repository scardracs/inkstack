package org.inkstack.settings.compose.sections

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.inkstack.BuildConfig
import org.inkstack.R
import org.inkstack.core.nav.router
import org.inkstack.core.util.ext.findActivity
import org.inkstack.core.util.ext.printStackTraceDebug
import org.inkstack.parsers.util.runCatchingCancellable
import org.inkstack.settings.compose.prefs.EditTextPreferenceItem
import org.inkstack.settings.compose.prefs.NavigationPreferenceItem
import org.inkstack.settings.compose.prefs.PreferenceCategoryHeader
import org.inkstack.settings.compose.prefs.SwitchPreferenceItem
import org.inkstack.sourcescore.data.CommunityApi
import org.inkstack.sourcescore.data.CommunityDatabase
import org.inkstack.sourcescore.data.CommunitySettings
import org.inkstack.sourcescore.data.nicknameRejection
import org.inkstack.sourcescore.ui.CommunityOnboardingDialog
import org.inkstack.sourcescore.ui.SourceScoreSyncWorker
import org.inkstack.sourcescore.ui.nicknameRejectionMessage
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CommunitySettingsComposeViewModel @Inject constructor(
	val communitySettings: CommunitySettings,
	val api: CommunityApi,
	val database: CommunityDatabase,
	val syncScheduler: SourceScoreSyncWorker.Scheduler,
) : ViewModel()

@Composable
fun CommunitySettingsScreen(
	onBack: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: CommunitySettingsComposeViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val snackbarHostState = remember { SnackbarHostState() }
	val activity = context.findActivity() as? FragmentActivity
	val settings = viewModel.communitySettings
	var enabled by remember { mutableStateOf(settings.isEnabled) }
	var telemetry by remember { mutableStateOf(settings.isTelemetryEnabled) }
	var nickname by remember { mutableStateOf(settings.nickname.orEmpty()) }
	var nicknameSummary by remember { mutableStateOf("") }
	var serverSummary by remember { mutableStateOf(settings.serverUrl) }
	var showServer by remember { mutableStateOf(false) }
	var showRecovery by remember { mutableStateOf(false) }
	var showDelete by remember { mutableStateOf(false) }
	var serverDraft by remember { mutableStateOf(settings.serverUrl) }
	var recoveryDraft by remember { mutableStateOf(settings.peekSecret().orEmpty()) }

	fun refreshNicknameSummary() {
		val name = settings.nickname
		nicknameSummary = when {
			name == null -> context.getString(R.string.community_nickname_summary)
			viewModel.api.isNicknamePending -> context.getString(R.string.community_nickname_pending_summary, name)
			else -> name
		}
		nickname = name.orEmpty()
	}

	LaunchedEffect(Unit) {
		refreshNicknameSummary()
		if (viewModel.api.isNicknamePending) {
			runCatchingCancellable { viewModel.api.pushPendingNickname() }
			refreshNicknameSummary()
		}
		serverSummary = context.getString(R.string.community_server_checking)
		val reachable = withContext(Dispatchers.Default) { viewModel.api.ping() }
		serverSummary = buildString {
			appendLine(settings.serverUrl)
			append(
				context.getString(
					if (reachable) R.string.community_server_ok else R.string.community_server_unreachable,
				),
			)
		}
	}

	SettingsSectionScaffold(
		titleRes = R.string.community,
		onBack = onBack,
		modifier = modifier,
		snackbarHostState = snackbarHostState,
	) {
		item {
			SwitchPreferenceItem(
				titleRes = R.string.community_enabled,
				summaryRes = R.string.community_enabled_summary,
				checked = enabled,
				onCheckedChange = {
					enabled = it
					settings.isEnabled = it
				},
			)
		}
		if (enabled) {
			item {
				SwitchPreferenceItem(
					titleRes = R.string.community_telemetry,
					summaryRes = R.string.community_telemetry_summary,
					checked = telemetry,
					onCheckedChange = {
						telemetry = it
						settings.isTelemetryEnabled = it
					},
				)
			}
			item { PreferenceCategoryHeader(R.string.community) }
			item {
				EditTextPreferenceItem(
					titleRes = R.string.community_nickname,
					value = nickname,
					summary = nicknameSummary,
					onValueChange = { candidate ->
						val trimmed = candidate.trim()
						if (trimmed.isEmpty()) return@EditTextPreferenceItem
						scope.launch {
							val result = runCatchingCancellable {
								withContext(Dispatchers.Default) { viewModel.api.setNickname(trimmed) }
							}
							val rejection = result.exceptionOrNull()?.let(::nicknameRejection)
							if (rejection != null) {
								snackbarHostState.showSnackbar(nicknameRejectionMessage(context, rejection))
							} else {
								result.exceptionOrNull()?.printStackTraceDebug()
								settings.nickname = trimmed
								if (result.isSuccess) {
									settings.syncedNickname = trimmed
								} else {
									snackbarHostState.showSnackbar(context.getString(R.string.community_nickname_pending))
								}
							}
							refreshNicknameSummary()
						}
					},
				)
			}
			item {
				NavigationPreferenceItem(
					titleRes = R.string.community_server,
					summary = serverSummary,
					onClick = {
						serverDraft = settings.serverUrl
						showServer = true
					},
				)
			}
			item {
				NavigationPreferenceItem(
					titleRes = R.string.community_recovery,
					summaryRes = R.string.community_recovery_summary,
					onClick = {
						recoveryDraft = settings.peekSecret().orEmpty()
						showRecovery = true
					},
				)
			}
			item {
				NavigationPreferenceItem(
					titleRes = R.string.community_export,
					summaryRes = R.string.community_export_summary,
					onClick = {
						scope.launch {
							val json = runCatchingCancellable {
								withContext(Dispatchers.Default) { viewModel.api.exportMyData() }
							}.onFailure { it.printStackTraceDebug() }.getOrNull()
							if (json == null) {
								snackbarHostState.showSnackbar(context.getString(R.string.community_export_failed))
								return@launch
							}
							val uri = withContext(Dispatchers.IO) {
								val directory = File(context.cacheDir, "export").apply { mkdirs() }
								val file = File(directory, "inkstack-community-data.json")
								file.writeText(json)
								FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.files", file)
							}
							ShareCompat.IntentBuilder(context)
								.setType("application/json")
								.addStream(uri)
								.setChooserTitle(context.getString(R.string.community_export))
								.startChooser()
						}
					},
				)
			}
			item {
				NavigationPreferenceItem(
					titleRes = R.string.community_delete,
					summaryRes = R.string.community_delete_summary,
					onClick = { showDelete = true },
				)
			}
		}
		item {
			NavigationPreferenceItem(
				titleRes = R.string.community_intro_replay,
				summaryRes = R.string.community_intro_replay_summary,
				onClick = {
					activity?.supportFragmentManager?.let { CommunityOnboardingDialog.show(it) }
				},
			)
		}
		item {
			NavigationPreferenceItem(
				titleRes = R.string.community_sponsor,
				onClick = {
					activity?.router?.openExternalBrowser(
						CommunityOnboardingDialog.SPONSOR_URL,
						context.getString(R.string.community_sponsor),
					)
				},
			)
		}
	}

	if (showServer) {
		AlertDialog(
			onDismissRequest = { showServer = false },
			title = { Text(stringResource(R.string.community_server)) },
			text = {
				OutlinedTextField(
					value = serverDraft,
					onValueChange = { serverDraft = it },
					modifier = Modifier.fillMaxWidth(),
					singleLine = true,
				)
			},
			confirmButton = {
				TextButton(
					onClick = {
						val value = serverDraft.trim()
						if (value.isNotEmpty()) {
							settings.serverUrl = value
							settings.isRegistered = false
							serverSummary = value
						}
						showServer = false
					},
				) { Text(stringResource(android.R.string.ok)) }
			},
			dismissButton = {
				TextButton(onClick = { showServer = false }) {
					Text(stringResource(android.R.string.cancel))
				}
			},
		)
	}

	if (showRecovery) {
		AlertDialog(
			onDismissRequest = { showRecovery = false },
			title = { Text(stringResource(R.string.community_recovery)) },
			text = {
				Column {
					Text(
						stringResource(
							if (settings.peekSecret() == null) R.string.community_recovery_none
							else R.string.community_recovery_explain,
						),
					)
					OutlinedTextField(
						value = recoveryDraft,
						onValueChange = { recoveryDraft = it },
						modifier = Modifier.fillMaxWidth(),
						singleLine = true,
						placeholder = { Text(stringResource(R.string.community_recovery_hint)) },
					)
				}
			},
			confirmButton = {
				TextButton(
					onClick = {
						val value = recoveryDraft.trim()
						if (!settings.isPlausibleSecret(value)) {
							scope.launch {
								snackbarHostState.showSnackbar(context.getString(R.string.community_recovery_invalid))
							}
							return@TextButton
						}
						if (value == settings.peekSecret()) {
							showRecovery = false
							return@TextButton
						}
						val previous = settings.peekSecret()
						if (!settings.adoptSecret(value)) return@TextButton
						scope.launch {
							val result = runCatchingCancellable { viewModel.api.helloWithStatus() }.getOrNull()
							when {
								result == null -> {
									settings.revertSecret(previous)
									snackbarHostState.showSnackbar(context.getString(R.string.community_recover_failed))
								}
								result.created -> {
									settings.revertSecret(previous)
									snackbarHostState.showSnackbar(context.getString(R.string.community_recover_unknown))
								}
								else -> {
									settings.isRegistered = true
									settings.nickname = result.identity.nickname
									settings.syncedNickname = result.identity.nickname
									settings.replyCursor = null
									settings.lastProbeUploadDay = Long.MIN_VALUE
									withContext(Dispatchers.Default) {
										viewModel.database.getSourceScoreDao().clear()
									}
									viewModel.syncScheduler.schedule()
									snackbarHostState.showSnackbar(context.getString(R.string.community_recovery_restored))
									refreshNicknameSummary()
									showRecovery = false
								}
							}
						}
					},
				) { Text(stringResource(R.string.community_recovery_restore)) }
			},
			dismissButton = {
				TextButton(
					onClick = {
						val value = recoveryDraft
						if (value.isNotEmpty()) {
							context.getSystemService(ClipboardManager::class.java)?.setPrimaryClip(
								ClipData.newPlainText(context.getString(R.string.community_recovery), value),
							)
						}
					},
				) { Text(stringResource(android.R.string.copy)) }
			},
		)
	}

	if (showDelete) {
		AlertDialog(
			onDismissRequest = { showDelete = false },
			title = { Text(stringResource(R.string.community_delete)) },
			text = { Text(stringResource(R.string.community_delete_confirm)) },
			confirmButton = {
				TextButton(
					onClick = {
						showDelete = false
						scope.launch {
							val deleted = runCatchingCancellable {
								withContext(Dispatchers.Default) { viewModel.api.deleteIdentity() }
							}.onFailure { it.printStackTraceDebug() }.isSuccess
							if (!deleted) return@launch
							withContext(Dispatchers.Default) {
								viewModel.database.getSourceScoreDao().clear()
								viewModel.database.getSourceStatsDao().clear()
							}
							settings.forget()
							enabled = false
							telemetry = false
						}
					},
				) { Text(stringResource(R.string.community_delete)) }
			},
			dismissButton = {
				TextButton(onClick = { showDelete = false }) {
					Text(stringResource(android.R.string.cancel))
				}
			},
		)
	}
}
