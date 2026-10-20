package org.inkstack.sync.ui.compose

import android.widget.Toast
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import org.inkstack.R
import org.inkstack.core.ui.compose.InkstackScaffold
import org.inkstack.core.util.ext.getDisplayMessage
import org.inkstack.sync.ui.SyncAuthResetViewModel

private const val PASSWORD_MIN_LENGTH = 4

@Composable
fun SyncAuthResetScreen(
	baseUrl: String?,
	token: String?,
	onCancel: () -> Unit,
	onSuccess: () -> Unit,
	onInvalid: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: SyncAuthResetViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val isLoading by viewModel.isLoading.collectAsState()
	var password by remember { mutableStateOf("") }
	var confirm by remember { mutableStateOf("") }
	var errorMessage by remember { mutableStateOf<String?>(null) }

	LaunchedEffect(baseUrl, token) {
		if (token.isNullOrEmpty() || baseUrl.isNullOrEmpty()) {
			onInvalid()
		} else {
			viewModel.syncURL.value = baseUrl
			viewModel.resetToken.value = token
		}
	}

	LaunchedEffect(viewModel) {
		viewModel.onPasswordResetSucceeded.collectLatest { event ->
			event?.consume {
				Toast.makeText(context, R.string.password_reset, Toast.LENGTH_SHORT).show()
				onSuccess()
			}
		}
	}
	LaunchedEffect(viewModel) {
		viewModel.onError.collectLatest { event ->
			event?.consume { error -> errorMessage = error.getDisplayMessage(context.resources) }
		}
	}

	val canSubmit = !isLoading && password.length >= PASSWORD_MIN_LENGTH && password == confirm

	InkstackScaffold(modifier = modifier) { padding ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding)
				.padding(24.dp),
		) {
			if (isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
			Text(
				stringResource(R.string.forgot_password),
				style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
			)
			Spacer(Modifier.padding(12.dp))
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
			OutlinedTextField(
				value = confirm,
				onValueChange = { confirm = it },
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 12.dp),
				label = { Text(stringResource(R.string.repeat_password)) },
				visualTransformation = PasswordVisualTransformation(),
				keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
				enabled = !isLoading,
				singleLine = true,
			)
			Spacer(Modifier.weight(1f))
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
			) {
				OutlinedButton(onClick = onCancel) {
					Text(stringResource(android.R.string.cancel))
				}
				Button(
					onClick = { viewModel.resetPassword(password) },
					enabled = canSubmit,
				) { Text(stringResource(R.string.done)) }
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
}
