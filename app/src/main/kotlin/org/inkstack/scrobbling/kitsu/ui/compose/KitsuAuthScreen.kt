package org.inkstack.scrobbling.kitsu.ui.compose

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.core.net.toUri
import org.inkstack.R
import org.inkstack.core.ui.compose.InkstackScaffold
import org.inkstack.parsers.util.urlEncoded

@Composable
fun KitsuAuthScreen(
	onCancel: () -> Unit,
	onContinue: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val context = LocalContext.current
	var email by remember { mutableStateOf("") }
	var password by remember { mutableStateOf("") }
	val regexEmail = remember { Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", RegexOption.IGNORE_CASE) }
	val canContinue = email.isNotBlank() && password.length >= 3 && regexEmail.matches(email.trim())

	@SuppressLint("UnsafeImplicitIntentLaunch")
	fun continueAuth() {
		val url = "inkstack://kitsu-auth?code=" + "${email.trim()};${password.trim()}".urlEncoded()
		context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
		onContinue()
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
				painter = painterResource(R.drawable.ic_kitsu),
				contentDescription = null,
				tint = MaterialTheme.colorScheme.primary,
			)
			Text(stringResource(R.string.kitsu), style = MaterialTheme.typography.headlineSmall)
			Text(
				stringResource(R.string.email_password_enter_hint),
				style = MaterialTheme.typography.titleMedium,
				textAlign = TextAlign.Center,
				modifier = Modifier.padding(top = 12.dp),
			)
			OutlinedTextField(
				value = email,
				onValueChange = { email = it },
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 24.dp),
				label = { Text(stringResource(R.string.email)) },
				keyboardOptions = KeyboardOptions(
					keyboardType = KeyboardType.Email,
					imeAction = ImeAction.Next,
				),
				singleLine = true,
			)
			OutlinedTextField(
				value = password,
				onValueChange = { password = it },
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 12.dp),
				label = { Text(stringResource(R.string.password)) },
				visualTransformation = PasswordVisualTransformation(),
				keyboardOptions = KeyboardOptions(
					keyboardType = KeyboardType.Password,
					imeAction = ImeAction.Done,
				),
				keyboardActions = KeyboardActions(onDone = { if (canContinue) continueAuth() }),
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
				Button(onClick = ::continueAuth, enabled = canContinue) {
					Text(stringResource(R.string.done))
				}
			}
		}
	}
}
