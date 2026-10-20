package org.inkstack.sync.ui.compose

import android.accounts.Account
import android.accounts.AccountManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import org.inkstack.R
import org.inkstack.core.ui.compose.InkstackScaffold
import org.inkstack.core.util.ext.getDisplayMessage
import org.inkstack.sync.data.SyncSettings
import org.inkstack.sync.domain.SyncAuthResult
import org.inkstack.sync.ui.SyncAuthViewModel

private const val PAGE_EMAIL = 0
private const val PAGE_PASSWORD = 1
private const val PASSWORD_MIN_LENGTH = 4

@Composable
fun SyncAuthScreen(
	onCancel: () -> Unit,
	onSuccess: (Bundle) -> Unit,
	onAccountExists: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: SyncAuthViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val isLoading by viewModel.isLoading.collectAsState()
	val syncUrl by viewModel.syncURL.collectAsState()
	var page by remember { mutableIntStateOf(PAGE_EMAIL) }
	var email by remember { mutableStateOf("") }
	var password by remember { mutableStateOf("") }
	var errorMessage by remember { mutableStateOf<String?>(null) }
	var resetSent by remember { mutableStateOf(false) }
	var showHostDialog by remember { mutableStateOf(false) }
	val regexEmail = remember { Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", RegexOption.IGNORE_CASE) }

	BackHandler(enabled = page == PAGE_PASSWORD && !isLoading) {
		page = PAGE_EMAIL
	}

	LaunchedEffect(viewModel) {
		viewModel.onTokenObtained.collectLatest { event ->
			event?.consume { auth -> onSuccess(buildAccountBundle(context, auth)) }
		}
	}
	LaunchedEffect(viewModel) {
		viewModel.onError.collectLatest { event ->
			event?.consume { error -> errorMessage = error.getDisplayMessage(context.resources) }
		}
	}
	LaunchedEffect(viewModel) {
		viewModel.onPasswordReset.collectLatest { event ->
			event?.consume { resetSent = true }
		}
	}
	LaunchedEffect(viewModel) {
		viewModel.onAccountAlreadyExists.collectLatest { event ->
			event?.consume {
				Toast.makeText(context, R.string.account_already_exists, Toast.LENGTH_SHORT).show()
				onAccountExists()
			}
		}
	}

	val emailOk = email.isNotBlank() && regexEmail.matches(email.trim())
	val passwordOk = password.length >= PASSWORD_MIN_LENGTH

	InkstackScaffold(modifier = modifier) { padding ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding)
				.padding(24.dp),
		) {
			if (isLoading) {
				LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
				CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp))
			}
			Text(stringResource(R.string.sync), style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
			Spacer(Modifier.padding(12.dp))
			if (page == PAGE_EMAIL) {
				OutlinedTextField(
					value = email,
					onValueChange = { email = it },
					modifier = Modifier.fillMaxWidth(),
					label = { Text(stringResource(R.string.email)) },
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
					enabled = !isLoading,
					singleLine = true,
				)
			} else {
				OutlinedTextField(
					value = password,
					onValueChange = { password = it },
					modifier = Modifier.fillMaxWidth(),
					label = { Text(stringResource(R.string.password)) },
					visualTransformation = PasswordVisualTransformation(),
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
					enabled = !isLoading,
					singleLine = true,
				)
				TextButton(
					onClick = { viewModel.forgotPassword(email.trim()) },
					enabled = !isLoading && emailOk,
				) { Text(stringResource(R.string.forgot_password)) }
			}
			Spacer(Modifier.weight(1f))
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
			) {
				when (page) {
					PAGE_EMAIL -> {
						OutlinedButton(onClick = onCancel) {
							Text(stringResource(android.R.string.cancel))
						}
						TextButton(onClick = { showHostDialog = true }) {
							Text(stringResource(R.string.settings))
						}
						Button(
							onClick = { page = PAGE_PASSWORD },
							enabled = !isLoading && emailOk,
						) { Text(stringResource(R.string.next)) }
					}
					else -> {
						OutlinedButton(onClick = { page = PAGE_EMAIL }, enabled = !isLoading) {
							Text(stringResource(R.string.back))
						}
						Button(
							onClick = { viewModel.obtainToken(email.trim(), password) },
							enabled = !isLoading && passwordOk,
						) { Text(stringResource(R.string.done)) }
					}
				}
			}
		}
	}

	errorMessage?.let { msg ->
		AlertDialog(
			onDismissRequest = { errorMessage = null },
			title = { Text(stringResource(R.string.error)) },
			text = { Text(msg) },
			confirmButton = {
				TextButton(onClick = { errorMessage = null }) {
					Text(stringResource(R.string.close))
				}
			},
		)
	}
	if (resetSent) {
		AlertDialog(
			onDismissRequest = { resetSent = false },
			title = { Text(stringResource(R.string.forgot_password)) },
			text = { Text(stringResource(R.string.reset_email_sent)) },
			confirmButton = {
				TextButton(onClick = { resetSent = false }) {
					Text(stringResource(R.string.close))
				}
			},
		)
	}
	if (showHostDialog) {
		SyncHostDialog(
			initialUrl = syncUrl,
			onDismissRequest = { showHostDialog = false },
			onConfirm = { url ->
				viewModel.setSyncHost(url)
				showHostDialog = false
			},
		)
	}
}

private fun buildAccountBundle(context: android.content.Context, authResult: SyncAuthResult): Bundle {
	val am = AccountManager.get(context)
	val account = Account(authResult.email, context.getString(R.string.account_type_sync))
	val userdata = Bundle(1)
	userdata.putString(SyncSettings.KEY_SYNC_URL, authResult.syncURL)
	val result = Bundle()
	if (am.addAccountExplicitly(account, authResult.password, userdata)) {
		result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
		result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
		result.putString(AccountManager.KEY_AUTHTOKEN, authResult.token)
		am.setAuthToken(account, account.type, authResult.token)
	} else {
		result.putString(AccountManager.KEY_ERROR_MESSAGE, context.getString(R.string.account_already_exists))
	}
	return result
}
