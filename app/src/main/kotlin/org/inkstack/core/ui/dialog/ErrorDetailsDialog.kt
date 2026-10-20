package org.inkstack.core.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.R
import org.inkstack.core.github.AppUpdateRepository
import org.inkstack.core.nav.AppRouter
import org.inkstack.core.nav.router
import org.inkstack.core.ui.compose.ComposeAlertDialogFragment
import org.inkstack.core.util.ext.copyToClipboard
import org.inkstack.core.util.ext.getCauseUrl
import org.inkstack.core.util.ext.isHttpUrl
import org.inkstack.core.util.ext.isReportable
import org.inkstack.core.util.ext.report
import org.inkstack.core.util.ext.requireSerializable
import javax.inject.Inject

@AndroidEntryPoint
class ErrorDetailsDialog : ComposeAlertDialogFragment() {

	@Inject
	lateinit var appUpdateRepository: AppUpdateRepository

	@Composable
	override fun DialogContent(onDismissRequest: () -> Unit) {
		val exception = remember {
			requireArguments().requireSerializable<Throwable>(AppRouter.KEY_ERROR)
		}
		val isUrlAvailable = exception.getCauseUrl()?.isHttpUrl() == true
		val updateAvailable = appUpdateRepository.isUpdateAvailable
		val descriptionRes = when {
			updateAvailable -> R.string.error_disclaimer_app_outdated
			exception.isReportable() -> R.string.error_disclaimer_report
			else -> 0
		}

		AlertDialog(
			onDismissRequest = onDismissRequest,
			title = { Text(stringResource(R.string.error_details)) },
			text = {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.verticalScroll(rememberScrollState()),
				) {
					Text(
						text = exception.message.orEmpty(),
						style = MaterialTheme.typography.bodyMedium,
					)
					if (isUrlAvailable) {
						Text(
							text = stringResource(R.string.error_disclaimer_manga),
							style = MaterialTheme.typography.bodySmall,
							modifier = Modifier.padding(top = 8.dp),
						)
						Button(
							onClick = {
								router.openBrowser(
									url = exception.getCauseUrl() ?: return@Button,
									source = null,
									title = null,
								)
							},
							modifier = Modifier
								.align(Alignment.CenterHorizontally)
								.padding(top = 10.dp),
						) {
							Text(stringResource(R.string.open_in_browser))
						}
					}
					if (descriptionRes != 0) {
						Text(
							text = stringResource(descriptionRes),
							style = MaterialTheme.typography.bodySmall,
							modifier = Modifier.padding(top = 8.dp),
						)
					}
				}
			},
			confirmButton = {
				when {
					updateAvailable -> TextButton(
						onClick = {
							router.openAppUpdate()
							onDismissRequest()
						},
					) {
						Text(stringResource(R.string.update))
					}
					exception.isReportable() -> TextButton(
						onClick = {
							exception.report(silent = true)
							onDismissRequest()
						},
					) {
						Text(stringResource(R.string.report))
					}
					else -> TextButton(onClick = onDismissRequest) {
						Text(stringResource(R.string.close))
					}
				}
			},
			dismissButton = {
				val context = LocalContext.current
				TextButton(
					onClick = {
						context.copyToClipboard(
							context.getString(R.string.error),
							exception.stackTraceToString(),
						)
					},
				) {
					Text(stringResource(androidx.preference.R.string.copy))
				}
			},
		)
	}
}
