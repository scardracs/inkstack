package org.inkstack.settings.compose.sections

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import org.inkstack.R
import org.inkstack.backups.domain.BackupUtils
import org.inkstack.backups.ui.backup.BackupService
import org.inkstack.core.nav.router
import org.inkstack.core.prefs.AppSettings
import org.inkstack.core.util.ext.findActivity
import org.inkstack.core.util.ext.getDisplayMessage
import org.inkstack.settings.compose.prefs.NavigationPreferenceItem
import org.inkstack.settings.userdata.BackupsSettingsViewModel

@Composable
fun BackupsSettingsScreen(
	onBack: () -> Unit,
	onOpenPeriodic: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: BackupsSettingsViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val snackbarHostState = remember { SnackbarHostState() }
	val activity = context.findActivity() as? FragmentActivity
	val frequency by viewModel.periodicalBackupFrequency.collectAsState(initial = 0f)
	val entries = stringArrayResource(R.array.backup_frequency)
	val entryValues = stringArrayResource(R.array.values_backup_frequency)
	val freqValue = (frequency as Number).toFloat()
	val periodicSummary = if (freqValue == 0f) {
		stringResource(R.string.disabled)
	} else {
		val asInt = freqValue.toInt().toString()
		val index = entryValues.indexOf(asInt).takeIf { it >= 0 }
			?: entryValues.indexOf(freqValue.toString())
		entries.getOrNull(index) ?: stringResource(R.string.periodic_backups)
	}

	val createBackup = rememberLauncherForActivityResult(
		ActivityResultContracts.CreateDocument("application/zip"),
	) { uri ->
		if (uri != null && !BackupService.start(context, uri)) {
			Toast.makeText(context, R.string.operation_not_supported, Toast.LENGTH_SHORT).show()
		}
	}
	val openBackup = rememberLauncherForActivityResult(
		ActivityResultContracts.OpenDocument(),
	) { uri: Uri? ->
		if (uri != null) {
			activity?.router?.showBackupRestoreDialog(uri)
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
		titleRes = R.string.backup_restore,
		onBack = onBack,
		modifier = modifier,
		snackbarHostState = snackbarHostState,
	) {
		item {
			NavigationPreferenceItem(
				titleRes = R.string.create_backup,
				summaryRes = R.string.backup_information,
				iconRes = R.drawable.ic_save,
				onClick = {
					try {
						createBackup.launch(BackupUtils.generateFileName(context))
					} catch (_: Exception) {
						scope.launch {
							snackbarHostState.showSnackbar(context.getString(R.string.operation_not_supported))
						}
					}
				},
			)
		}
		item {
			NavigationPreferenceItem(
				titleRes = R.string.restore_backup,
				summaryRes = R.string.restore_summary,
				iconRes = R.drawable.ic_backup_restore,
				onClick = {
					try {
						openBackup.launch(arrayOf("*/*"))
					} catch (_: Exception) {
						scope.launch {
							snackbarHostState.showSnackbar(context.getString(R.string.operation_not_supported))
						}
					}
				},
			)
		}
		item {
			NavigationPreferenceItem(
				titleRes = R.string.periodic_backups,
				summary = periodicSummary,
				iconRes = R.drawable.ic_timer_run,
				onClick = onOpenPeriodic,
			)
		}
	}
}
