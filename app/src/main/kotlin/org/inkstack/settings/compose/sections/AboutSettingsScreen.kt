package org.inkstack.settings.compose.sections

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.edit
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.preference.PreferenceManager
import org.inkstack.BuildConfig
import org.inkstack.R
import org.inkstack.core.github.VersionId
import org.inkstack.core.github.isStable
import androidx.fragment.app.FragmentActivity
import org.inkstack.core.nav.router
import org.inkstack.core.prefs.AppSettings
import org.inkstack.core.ui.compose.InkstackScaffold
import org.inkstack.core.util.ext.findActivity
import org.inkstack.settings.about.AboutSettingsViewModel
import org.inkstack.settings.about.AppUpdateActivity
import org.inkstack.settings.compose.prefs.NavigationPreferenceItem
import org.inkstack.settings.compose.prefs.SwitchPreferenceItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutSettingsScreen(
	onBack: () -> Unit,
	onOpenChangelog: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: AboutSettingsViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val snackbarHostState = remember { SnackbarHostState() }
	val isUpdateSupported by viewModel.isUpdateSupported.collectAsState()
	val isLoading by viewModel.isLoading.collectAsState()
	val prefs = remember { PreferenceManager.getDefaultSharedPreferences(context) }
	var unstable by remember {
		mutableStateOf(prefs.getBoolean(AppSettings.KEY_UPDATES_UNSTABLE, false))
	}
	val stableBuild = VersionId(BuildConfig.VERSION_NAME).isStable

	LaunchedEffect(viewModel) {
		viewModel.onUpdateAvailable.collect { event ->
			event?.consume { version ->
				if (version == null) {
					snackbarHostState.showSnackbar(context.getString(R.string.no_update_available))
				} else {
					context.startActivity(Intent(context, AppUpdateActivity::class.java))
				}
			}
		}
	}

	InkstackScaffold(
		modifier = modifier,
		snackbarHostState = snackbarHostState,
		topBar = {
			TopAppBar(
				title = { Text(stringResource(R.string.about)) },
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
				NavigationPreferenceItem(
					title = stringResource(R.string.app_version, BuildConfig.VERSION_NAME),
					summaryRes = R.string.check_for_updates,
					iconRes = R.drawable.ic_info_outline,
					enabled = isUpdateSupported && !isLoading,
					onClick = { viewModel.checkForUpdates() },
				)
			}
			if (isUpdateSupported) {
				item {
					SwitchPreferenceItem(
						titleRes = R.string.allow_unstable_updates,
						summaryRes = R.string.allow_unstable_updates_summary,
						iconRes = R.drawable.ic_app_update,
						checked = if (stableBuild) unstable else true,
						enabled = stableBuild,
						onCheckedChange = { checked ->
							unstable = checked
							prefs.edit { putBoolean(AppSettings.KEY_UPDATES_UNSTABLE, checked) }
						},
					)
				}
			}
			item {
				NavigationPreferenceItem(
					titleRes = R.string.changelog,
					summaryRes = R.string.changelog_summary,
					iconRes = R.drawable.ic_new,
					onClick = onOpenChangelog,
				)
			}
			item {
				NavigationPreferenceItem(
					titleRes = R.string.source_code,
					summaryRes = R.string.url_github,
					iconRes = R.drawable.ic_script,
					onClick = {
						val activity = context.findActivity() as? FragmentActivity
						val url = context.getString(R.string.url_github)
						if (activity?.router?.openExternalBrowser(url, context.getString(R.string.source_code)) != true) {
							Toast.makeText(context, R.string.operation_not_supported, Toast.LENGTH_SHORT).show()
						}
					},
				)
			}
		}
	}
}
