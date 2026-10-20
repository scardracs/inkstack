package org.inkstack.settings.compose.sections

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.core.content.edit
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.inkstack.R
import org.inkstack.core.network.BaseHttpClient
import org.inkstack.core.prefs.AppSettings
import org.inkstack.core.util.ext.getDisplayMessage
import org.inkstack.core.util.ext.printStackTraceDebug
import org.inkstack.parsers.util.await
import org.inkstack.settings.compose.prefs.EditTextPreferenceItem
import org.inkstack.settings.compose.prefs.ListPreferenceItem
import org.inkstack.settings.compose.prefs.NavigationPreferenceItem
import org.inkstack.settings.compose.prefs.PreferenceCategoryHeader
import org.inkstack.settings.compose.prefs.rememberSharedPreferences
import java.net.Proxy
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class ProxySettingsComposeViewModel @Inject constructor(
	@BaseHttpClient val okHttpClient: OkHttpClient,
) : ViewModel() {
	private var testJob: Job? = null

	fun testConnection(onResult: (Throwable?) -> Unit, onBusy: (Boolean) -> Unit) {
		testJob?.cancel()
		testJob = viewModelScope.launch {
			onBusy(true)
			try {
				withContext(Dispatchers.Default) {
					val request = Request.Builder().get().url("http://neverssl.com").build()
					okHttpClient.newCall(request).await().use { response ->
						check(response.isSuccessful) { response.message }
					}
				}
				onResult(null)
			} catch (e: CancellationException) {
				throw e
			} catch (e: Throwable) {
				e.printStackTraceDebug()
				onResult(e)
			} finally {
				onBusy(false)
			}
		}
	}
}

@Composable
fun ProxySettingsScreen(
	onBack: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: ProxySettingsComposeViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val prefs = rememberSharedPreferences()
	val types = stringArrayResource(R.array.proxy_types)
	val typeValues = stringArrayResource(R.array.values_proxy_types)
	var type by remember {
		mutableStateOf(prefs.getString(AppSettings.KEY_PROXY_TYPE, Proxy.Type.DIRECT.name) ?: Proxy.Type.DIRECT.name)
	}
	var address by remember { mutableStateOf(prefs.getString(AppSettings.KEY_PROXY_ADDRESS, "").orEmpty()) }
	var port by remember { mutableStateOf(prefs.getString(AppSettings.KEY_PROXY_PORT, "").orEmpty()) }
	var login by remember { mutableStateOf(prefs.getString(AppSettings.KEY_PROXY_LOGIN, "").orEmpty()) }
	var password by remember { mutableStateOf(prefs.getString(AppSettings.KEY_PROXY_PASSWORD, "").orEmpty()) }
	var testing by remember { mutableStateOf(false) }
	var testMessage by remember { mutableStateOf<String?>(null) }
	val enabled = type != Proxy.Type.DIRECT.name
	val passwordSummary = if (password.isEmpty()) null else "••••••••"

	SettingsSectionScaffold(titleRes = R.string.proxy, onBack = onBack, modifier = modifier) {
		item {
			ListPreferenceItem(
				titleRes = R.string.type,
				entries = types,
				entryValues = typeValues,
				value = type,
				iconRes = R.drawable.ic_split_horizontal,
				onValueChange = {
					type = it
					prefs.edit { putString(AppSettings.KEY_PROXY_TYPE, it) }
				},
			)
		}
		item {
			EditTextPreferenceItem(
				titleRes = R.string.address,
				value = address,
				iconRes = R.drawable.ic_web,
				enabled = enabled,
				onValueChange = {
					address = it
					prefs.edit { putString(AppSettings.KEY_PROXY_ADDRESS, it) }
				},
			)
		}
		item {
			EditTextPreferenceItem(
				titleRes = R.string.port,
				value = port,
				iconRes = R.drawable.ic_tag,
				enabled = enabled,
				onValueChange = {
					port = it
					prefs.edit { putString(AppSettings.KEY_PROXY_PORT, it) }
				},
			)
		}
		item { PreferenceCategoryHeader(R.string.authorization_optional) }
		item {
			EditTextPreferenceItem(
				titleRes = R.string.username,
				value = login,
				iconRes = R.drawable.ic_user,
				enabled = enabled,
				onValueChange = {
					login = it
					prefs.edit { putString(AppSettings.KEY_PROXY_LOGIN, it) }
				},
			)
		}
		item {
			EditTextPreferenceItem(
				titleRes = R.string.password,
				value = password,
				summary = passwordSummary,
				iconRes = R.drawable.ic_lock,
				enabled = enabled,
				onValueChange = {
					password = it
					prefs.edit { putString(AppSettings.KEY_PROXY_PASSWORD, it) }
				},
			)
		}
		item {
			NavigationPreferenceItem(
				titleRes = R.string.test_connection,
				summary = if (testing) stringResource(R.string.loading_) else null,
				iconRes = R.drawable.ic_retry,
				enabled = enabled && !testing,
				onClick = {
					viewModel.testConnection(
						onResult = { error ->
							testMessage = error?.getDisplayMessage(context.resources)
								?: context.getString(R.string.connection_ok)
						},
						onBusy = { testing = it },
					)
				},
			)
		}
	}
	testMessage?.let { message ->
		AlertDialog(
			onDismissRequest = { testMessage = null },
			title = { Text(stringResource(R.string.proxy)) },
			text = { Text(message) },
			confirmButton = {
				TextButton(onClick = { testMessage = null }) {
					Text(stringResource(android.R.string.ok))
				}
			},
		)
	}
}
