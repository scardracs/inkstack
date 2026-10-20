package org.inkstack.main.ui.welcome

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.R
import org.inkstack.core.model.titleResId
import org.inkstack.core.nav.router
import org.inkstack.core.ui.compose.sheet.ComposeAdaptiveSheet
import org.inkstack.core.util.ext.getDisplayName
import org.inkstack.core.util.ext.tryLaunch
import org.inkstack.sync.domain.SyncController
import javax.inject.Inject

@AndroidEntryPoint
class WelcomeSheet : ComposeAdaptiveSheet() {

	private val viewModel by viewModels<WelcomeViewModel>()

	@Inject
	lateinit var syncController: SyncController

	@Composable
	override fun sheetTitle(): String = stringResource(R.string.welcome)

	@OptIn(ExperimentalLayoutApi::class)
	@Composable
	override fun SheetContent(onDismissRequest: () -> Unit) {
		val context = LocalContext.current
		val isTablet = booleanResource(R.bool.is_tablet)
		val locales by viewModel.locales.collectAsState()
		val types by viewModel.types.collectAsState()
		val backupSelect = rememberLauncherForActivityResult(
			ActivityResultContracts.OpenDocument(),
		) { uri: Uri? ->
			if (uri != null) router.showBackupRestoreDialog(uri)
		}

		Column(
			modifier = Modifier
				.fillMaxWidth()
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 16.dp, vertical = 8.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp),
		) {
			if (!isTablet) {
				Text(
					text = stringResource(R.string.welcome),
					style = MaterialTheme.typography.headlineSmall,
				)
			}
			FlowRow(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp),
			) {
				FilterChip(
					selected = false,
					onClick = {
						if (!backupSelect.tryLaunch(arrayOf("*/*"))) {
							Snackbar.make(
								requireView(),
								R.string.operation_not_supported,
								Snackbar.LENGTH_SHORT,
							).show()
						}
					},
					label = { Text(stringResource(R.string.restore_backup)) },
				)
				FilterChip(
					selected = false,
					onClick = { syncController.addAccount(requireActivity()) {} },
					label = { Text(stringResource(R.string.sync_auth)) },
				)
				FilterChip(
					selected = false,
					onClick = { router.openDirectoriesSettings() },
					label = { Text(stringResource(R.string.local_manga_directories)) },
				)
			}
			Text(
				text = stringResource(R.string.welcome_text),
				style = MaterialTheme.typography.bodyMedium,
			)
			Text(
				text = stringResource(R.string.languages),
				style = MaterialTheme.typography.titleSmall,
			)
			FlowRow(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp),
			) {
				locales.availableItems.forEach { locale ->
					FilterChip(
						selected = locale in locales.selectedItems,
						onClick = {
							viewModel.setLocaleChecked(locale, locale !in locales.selectedItems)
						},
						label = { Text(locale.getDisplayName(context)) },
					)
				}
			}
			Text(
				text = stringResource(R.string.type),
				style = MaterialTheme.typography.titleSmall,
			)
			FlowRow(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp),
			) {
				types.availableItems.forEach { type ->
					FilterChip(
						selected = type in types.selectedItems,
						onClick = {
							viewModel.setTypeChecked(type, type !in types.selectedItems)
						},
						label = { Text(stringResource(type.titleResId)) },
					)
				}
			}
		}
	}
}
