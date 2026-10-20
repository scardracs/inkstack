package org.inkstack.settings.compose.sections

import android.accounts.AccountManager
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
import org.inkstack.core.util.ext.getQuantityStringSafe
import org.inkstack.settings.compose.prefs.ListPreferenceItem
import org.inkstack.settings.compose.prefs.NavigationPreferenceItem
import org.inkstack.settings.compose.prefs.rememberSharedPreferences
import org.inkstack.sync.data.SyncSettings
import org.inkstack.sync.domain.SyncController
import javax.inject.Inject

@HiltViewModel
class SyncSettingsComposeViewModel @Inject constructor(
	val syncSettings: SyncSettings,
	val syncController: SyncController,
) : ViewModel()

@Composable
fun SyncSettingsScreen(
	onBack: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: SyncSettingsComposeViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val prefs = rememberSharedPreferences()
	val scope = rememberCoroutineScope()
	val snackbarHostState = remember { SnackbarHostState() }
	val activity = context.findActivity() as? FragmentActivity
	val periods = AppSettings.SYNC_PERIODS
	val periodEntries = remember {
		Array(periods.size) { i ->
			val hours = periods[i]
			if (hours == 0) context.getString(R.string.disabled)
			else context.resources.getQuantityStringSafe(R.plurals.hours, hours, hours)
		}
	}
	val periodValues = remember { Array(periods.size) { periods[it].toString() } }
	var period by remember {
		mutableStateOf(prefs.getString(AppSettings.KEY_SYNC_PERIOD, "6") ?: "6")
	}
	var syncSummary by remember { mutableStateOf(context.getString(R.string.sync_login)) }
	var host by remember { mutableStateOf(viewModel.syncSettings.syncUrl) }
	var accountPresent by remember { mutableStateOf(false) }
	var syncEnabled by remember { mutableStateOf(false) }
	var showHostDialog by remember { mutableStateOf(false) }
	var hostDraft by remember { mutableStateOf(host) }

	fun refresh() {
		scope.launch {
			val account = withContext(Dispatchers.Default) {
				val type = context.getString(R.string.account_type_sync)
				AccountManager.get(context).getAccountsByType(type).firstOrNull()
			}
			accountPresent = account != null
			if (account == null) {
				syncSummary = context.getString(R.string.sync_login)
				syncEnabled = false
			} else {
				val enabled = viewModel.syncController.isEnabled(account)
				syncEnabled = enabled
				syncSummary = when {
					enabled -> {
						val parts = buildList {
							if (viewModel.syncController.isFavouritesEnabled(account)) add(context.getString(R.string.favourites))
							if (viewModel.syncController.isHistoryEnabled(account)) add(context.getString(R.string.history))
						}
						account.name + parts.joinToString(", ", " (", ")")
					}
					else -> context.getString(R.string.disabled)
				}
			}
			host = viewModel.syncSettings.syncUrl
		}
	}

	LaunchedEffect(Unit) { refresh() }

	SettingsSectionScaffold(
		titleRes = R.string.sync_settings,
		onBack = onBack,
		modifier = modifier,
		snackbarHostState = snackbarHostState,
	) {
		item {
			NavigationPreferenceItem(
				titleRes = R.string.sync,
				summary = syncSummary,
				iconRes = R.drawable.ic_sync,
				onClick = {
					val am = AccountManager.get(context)
					val accountType = context.getString(R.string.account_type_sync)
					val account = am.getAccountsByType(accountType).firstOrNull()
					if (account == null) {
						viewModel.syncController.addAccount(activity ?: return@NavigationPreferenceItem) { refresh() }
					} else if (activity?.router?.openSystemSyncSettings(account) != true) {
						scope.launch {
							snackbarHostState.showSnackbar(context.getString(R.string.operation_not_supported))
						}
					}
				},
			)
		}
		item {
			ListPreferenceItem(
				titleRes = R.string.frequency_of_check,
				entries = periodEntries,
				entryValues = periodValues,
				value = period,
				iconRes = R.drawable.ic_timer,
				enabled = accountPresent && syncEnabled,
				onValueChange = {
					period = it
					prefs.edit { putString(AppSettings.KEY_SYNC_PERIOD, it) }
				},
			)
		}
		item {
			NavigationPreferenceItem(
				titleRes = R.string.server_address,
				summary = host,
				iconRes = R.drawable.ic_web,
				enabled = accountPresent,
				onClick = {
					hostDraft = host
					showHostDialog = true
				},
			)
		}
		item {
			NavigationPreferenceItem(
				titleRes = R.string.logout,
				iconRes = R.drawable.ic_user,
				enabled = accountPresent,
				onClick = {
					val am = AccountManager.get(context)
					val accountType = context.getString(R.string.account_type_sync)
					val account = am.getAccountsByType(accountType).firstOrNull() ?: return@NavigationPreferenceItem
					viewModel.syncController.removeAccount(activity ?: return@NavigationPreferenceItem, account) {
						refresh()
					}
				},
			)
		}
	}

	if (showHostDialog) {
		AlertDialog(
			onDismissRequest = { showHostDialog = false },
			title = { Text(stringResource(R.string.server_address)) },
			text = {
				OutlinedTextField(
					value = hostDraft,
					onValueChange = { hostDraft = it },
					modifier = Modifier,
					singleLine = true,
				)
			},
			confirmButton = {
				TextButton(
					onClick = {
						viewModel.syncSettings.syncUrl = hostDraft
						host = viewModel.syncSettings.syncUrl
						showHostDialog = false
					},
				) {
					Text(stringResource(android.R.string.ok))
				}
			},
			dismissButton = {
				TextButton(onClick = { showHostDialog = false }) {
					Text(stringResource(android.R.string.cancel))
				}
			},
		)
	}
}
