package org.inkstack.settings.protect.compose

import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import org.inkstack.R
import org.inkstack.core.ui.compose.InkstackScaffold
import org.inkstack.settings.protect.ProtectSetupViewModel

private const val MIN_PASSWORD_LENGTH = 4

@Composable
fun ProtectSetupScreen(
	onCancel: () -> Unit,
	onDone: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: ProtectSetupViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val isSecondStep by viewModel.isSecondStep.collectAsState()
	var password by remember { mutableStateOf("") }
	var error by remember { mutableStateOf<String?>(null) }
	var biometric by remember { mutableStateOf(viewModel.isBiometricEnabled) }
	val biometricAvailable = remember {
		context.packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
	}

	LaunchedEffect(viewModel) {
		viewModel.onPasswordSet.collectLatest { event ->
			event?.consume { onDone() }
		}
	}
	LaunchedEffect(viewModel) {
		viewModel.onPasswordMismatch.collectLatest { event ->
			event?.consume { error = context.getString(R.string.passwords_mismatch) }
		}
	}
	LaunchedEffect(viewModel) {
		viewModel.onClearText.collectLatest { event ->
			event?.consume { password = "" }
		}
	}

	InkstackScaffold(modifier = modifier) { padding ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding)
				.padding(24.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			Icon(
				painter = painterResource(R.drawable.ic_lock),
				contentDescription = null,
				tint = MaterialTheme.colorScheme.primary,
			)
			Spacer(Modifier.height(8.dp))
			Text(stringResource(R.string.protect_application), style = MaterialTheme.typography.headlineSmall)
			Text(
				stringResource(R.string.protect_application_subtitle),
				style = MaterialTheme.typography.titleMedium,
				textAlign = TextAlign.Center,
				modifier = Modifier.padding(top = 12.dp),
			)
			OutlinedTextField(
				value = password,
				onValueChange = {
					password = it
					error = null
				},
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 30.dp),
				visualTransformation = PasswordVisualTransformation(),
				keyboardOptions = KeyboardOptions(
					keyboardType = KeyboardType.Password,
					imeAction = ImeAction.Done,
				),
				keyboardActions = KeyboardActions(
					onDone = {
						if (password.length >= MIN_PASSWORD_LENGTH) viewModel.onNextClick(password)
					},
				),
				supportingText = {
					Text(
						error ?: stringResource(
							if (isSecondStep) R.string.repeat_password else R.string.password_length_hint,
						),
					)
				},
				isError = error != null,
				singleLine = true,
			)
			if (isSecondStep && biometricAvailable) {
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(top = 16.dp),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically,
				) {
					Text(stringResource(R.string.use_fingerprint))
					Switch(
						checked = biometric,
						onCheckedChange = {
							biometric = it
							viewModel.setBiometricEnabled(it)
						},
					)
				}
			}
			Spacer(Modifier.weight(1f))
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
			) {
				if (!isSecondStep) {
					OutlinedButton(onClick = onCancel) {
						Text(stringResource(android.R.string.cancel))
					}
				} else {
					Spacer(Modifier.weight(1f))
				}
				Button(
					onClick = { viewModel.onNextClick(password) },
					enabled = password.length >= MIN_PASSWORD_LENGTH,
				) {
					Text(stringResource(if (isSecondStep) R.string.confirm else R.string.next))
				}
			}
		}
	}
}
