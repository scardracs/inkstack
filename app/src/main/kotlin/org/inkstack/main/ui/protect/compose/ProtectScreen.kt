package org.inkstack.main.ui.protect.compose

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import org.inkstack.core.util.ext.getDisplayMessage
import org.inkstack.main.ui.protect.ProtectViewModel

@Composable
fun ProtectScreen(
	onCancel: () -> Unit,
	onUnlocked: () -> Unit,
	onBiometric: () -> Boolean,
	showBiometric: Boolean,
	modifier: Modifier = Modifier,
	viewModel: ProtectViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val isLoading by viewModel.isLoading.collectAsState()
	var password by remember { mutableStateOf("") }
	var error by remember { mutableStateOf<String?>(null) }

	LaunchedEffect(viewModel) {
		viewModel.onError.collectLatest { event ->
			event?.consume { e -> error = e.getDisplayMessage(context.resources) }
		}
	}
	LaunchedEffect(viewModel) {
		viewModel.onUnlockSuccess.collectLatest { event ->
			event?.consume { onUnlocked() }
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
			Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall)
			Text(
				stringResource(R.string.enter_password),
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
					keyboardType = if (viewModel.isNumericPassword) KeyboardType.NumberPassword else KeyboardType.Password,
					imeAction = ImeAction.Done,
				),
				keyboardActions = KeyboardActions(
					onDone = { if (password.isNotEmpty()) viewModel.tryUnlock(password) },
				),
				isError = error != null,
				supportingText = error?.let { { Text(it) } },
				enabled = !isLoading,
				singleLine = true,
			)
			if (showBiometric && password.isEmpty()) {
				TextButton(onClick = { onBiometric() }) {
					Text(stringResource(androidx.biometric.R.string.use_biometric_label))
				}
			}
			Spacer(Modifier.weight(1f))
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
			) {
				OutlinedButton(onClick = onCancel) {
					Text(stringResource(android.R.string.cancel))
				}
				Button(
					onClick = { viewModel.tryUnlock(password) },
					enabled = password.isNotEmpty() && !isLoading,
				) {
					Text(stringResource(R.string.next))
				}
			}
		}
	}
}
