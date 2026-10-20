package org.inkstack.local.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.R
import org.inkstack.core.os.OpenDocumentTreeHelper
import org.inkstack.core.ui.compose.ComposeAlertDialogFragment
import org.inkstack.core.util.ext.tryLaunch
import org.inkstack.local.data.LocalStorageManager
import javax.inject.Inject

@AndroidEntryPoint
class ImportDialogFragment : ComposeAlertDialogFragment() {

	@Inject
	lateinit var storageManager: LocalStorageManager

	private val importDirCall = OpenDocumentTreeHelper(this) {
		startImport(listOfNotNull(it))
	}

	@Composable
	override fun DialogContent(onDismissRequest: () -> Unit) {
		val context = LocalContext.current
		val importFileLauncher = rememberLauncherForActivityResult(
			ActivityResultContracts.OpenMultipleDocuments(),
		) { uris -> startImport(uris) }

		AlertDialog(
			onDismissRequest = onDismissRequest,
			title = { Text(stringResource(R.string._import)) },
			text = {
				Column(modifier = Modifier.fillMaxWidth()) {
					ImportOptionRow(
						icon = R.drawable.ic_file_zip,
						title = stringResource(R.string.comics_archive),
						subtitle = stringResource(R.string.comics_archive_import_description),
						onClick = {
							if (!importFileLauncher.tryLaunch(arrayOf("*/*"))) {
								Toast.makeText(context, R.string.operation_not_supported, Toast.LENGTH_SHORT).show()
							}
						},
					)
					ImportOptionRow(
						icon = R.drawable.ic_folder_file,
						title = stringResource(R.string.folder_with_images),
						subtitle = stringResource(R.string.folder_with_images_import_description),
						onClick = {
							if (!importDirCall.tryLaunch(null)) {
								Toast.makeText(context, R.string.operation_not_supported, Toast.LENGTH_SHORT).show()
							}
						},
					)
				}
			},
			confirmButton = {},
			dismissButton = {
				TextButton(onClick = onDismissRequest) {
					Text(stringResource(android.R.string.cancel))
				}
			},
		)
	}

	private fun startImport(uris: Collection<Uri>) {
		if (uris.isEmpty()) return
		uris.forEach { storageManager.takePermissions(it) }
		val ctx = requireContext()
		val msg = if (ImportService.start(ctx, uris)) {
			R.string.import_will_start_soon
		} else {
			R.string.error_occurred
		}
		Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show()
		dismissAllowingStateLoss()
	}
}

@Composable
private fun ImportOptionRow(
	icon: Int,
	title: String,
	subtitle: String,
	onClick: () -> Unit,
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onClick)
			.padding(vertical = 10.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Icon(
			painter = painterResource(icon),
			contentDescription = null,
			modifier = Modifier
				.size(40.dp)
				.padding(end = 12.dp),
		)
		Column {
			Text(title, style = MaterialTheme.typography.bodyLarge)
			Text(
				subtitle,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
			)
		}
	}
}
