package org.inkstack.settings.compose.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import org.inkstack.R
import org.inkstack.core.network.DoHProvider
import org.inkstack.core.prefs.AppSettings
import org.inkstack.core.util.FileSize
import org.inkstack.core.util.ext.getDisplayMessage
import org.inkstack.parsers.util.names
import org.inkstack.settings.StorageAndNetworkSettingsViewModel
import org.inkstack.settings.compose.prefs.ListPreferenceItem
import org.inkstack.settings.compose.prefs.NavigationPreferenceItem
import org.inkstack.settings.compose.prefs.PreferenceCategoryHeader
import org.inkstack.settings.compose.prefs.SwitchPreferenceItem
import org.inkstack.settings.compose.prefs.rememberSharedPreferences
import java.net.Proxy

@Composable
fun StorageNetworkSettingsScreen(
	onBack: () -> Unit,
	onOpenProxy: () -> Unit,
	onOpenDataCleanup: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: StorageAndNetworkSettingsViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val prefs = rememberSharedPreferences()
	val snackbarHostState = remember { SnackbarHostState() }
	val usage by viewModel.storageUsage.collectAsState()
	val networkPolicy = stringArrayResource(R.array.network_policy)
	val networkPolicyValues = stringArrayResource(R.array.values_network_policy)
	val imageProxies = stringArrayResource(R.array.image_proxies)
	val imageProxyValues = stringArrayResource(R.array.values_image_proxies)
	val dohEntries = stringArrayResource(R.array.doh_providers)
	val dohValues = remember { DoHProvider.entries.names() }

	var prefetch by remember { mutableStateOf(prefs.getString(AppSettings.KEY_PREFETCH_CONTENT, "0") ?: "0") }
	var pagesPreload by remember { mutableStateOf(prefs.getString(AppSettings.KEY_PAGES_PRELOAD, "2") ?: "2") }
	var doh by remember {
		mutableStateOf(prefs.getString(AppSettings.KEY_DOH, DoHProvider.NONE.name) ?: DoHProvider.NONE.name)
	}
	var imagesProxy by remember { mutableStateOf(prefs.getString(AppSettings.KEY_IMAGES_PROXY, "-1") ?: "-1") }
	var sslBypass by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_SSL_BYPASS, false)) }
	var noOffline by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_OFFLINE_DISABLED, false)) }
	var proxySummary by remember { mutableStateOf(context.getString(R.string.disabled)) }

	fun refreshProxySummary() {
		val typeName = prefs.getString(AppSettings.KEY_PROXY_TYPE, Proxy.Type.DIRECT.name)
		val type = runCatching { Proxy.Type.valueOf(typeName!!) }.getOrDefault(Proxy.Type.DIRECT)
		val address = prefs.getString(AppSettings.KEY_PROXY_ADDRESS, null)
		val port = prefs.getString(AppSettings.KEY_PROXY_PORT, null)?.toIntOrNull() ?: 0
		proxySummary = when {
			type == Proxy.Type.DIRECT -> context.getString(R.string.disabled)
			address.isNullOrEmpty() || port == 0 -> context.getString(R.string.invalid_proxy_configuration)
			else -> "$address:$port"
		}
	}

	LaunchedEffect(Unit) { refreshProxySummary() }
	LaunchedEffect(viewModel) {
		viewModel.onError.collectLatest { event ->
			event?.consume { error ->
				snackbarHostState.showSnackbar(error.getDisplayMessage(context.resources))
			}
		}
	}

	SettingsSectionScaffold(
		titleRes = R.string.storage_and_network,
		onBack = onBack,
		modifier = modifier,
		snackbarHostState = snackbarHostState,
	) {
		item { PreferenceCategoryHeader(R.string.storage_usage) }
		item {
			ListItem(
				headlineContent = { Text(stringResource(R.string.storage_usage)) },
				supportingContent = {
					Column(modifier = Modifier.fillMaxWidth()) {
						val total = usage?.let {
							it.savedManga.percent + it.pagesCache.percent + it.otherCache.percent
						} ?: 0f
						LinearProgressIndicator(
							progress = { total.coerceIn(0f, 1f) },
							modifier = Modifier
								.fillMaxWidth()
								.padding(vertical = 8.dp),
						)
						Text(
							formatUsage(context, usage?.savedManga?.bytes, R.string.saved_manga),
						)
						Text(
							formatUsage(context, usage?.pagesCache?.bytes, R.string.pages_cache),
						)
						Text(
							formatUsage(context, usage?.otherCache?.bytes, R.string.other_cache),
						)
						Text(
							formatUsage(context, usage?.available?.bytes, R.string.available, R.string.available),
						)
					}
				},
			)
		}
		item {
			NavigationPreferenceItem(
				titleRes = R.string.data_removal,
				iconRes = R.drawable.ic_clear_all,
				onClick = onOpenDataCleanup,
			)
		}
		item {
			ListPreferenceItem(
				titleRes = R.string.prefetch_content,
				entries = networkPolicy,
				entryValues = networkPolicyValues,
				value = prefetch,
				iconRes = R.drawable.ic_download,
				onValueChange = {
					prefetch = it
					prefs.edit { putString(AppSettings.KEY_PREFETCH_CONTENT, it) }
				},
			)
		}
		item {
			ListPreferenceItem(
				titleRes = R.string.preload_pages,
				entries = networkPolicy,
				entryValues = networkPolicyValues,
				value = pagesPreload,
				iconRes = R.drawable.ic_book_page,
				onValueChange = {
					pagesPreload = it
					prefs.edit { putString(AppSettings.KEY_PAGES_PRELOAD, it) }
				},
			)
		}
		item {
			NavigationPreferenceItem(
				titleRes = R.string.proxy,
				summary = proxySummary,
				iconRes = R.drawable.ic_split_horizontal,
				onClick = onOpenProxy,
			)
		}
		item {
			ListPreferenceItem(
				titleRes = R.string.dns_over_https,
				entries = dohEntries,
				entryValues = dohValues,
				value = doh,
				iconRes = R.drawable.ic_web,
				onValueChange = {
					doh = it
					prefs.edit { putString(AppSettings.KEY_DOH, it) }
				},
			)
		}
		item {
			ListPreferenceItem(
				titleRes = R.string.images_proxy_title,
				entries = imageProxies,
				entryValues = imageProxyValues,
				value = imagesProxy,
				iconRes = R.drawable.ic_images,
				onValueChange = {
					imagesProxy = it
					prefs.edit { putString(AppSettings.KEY_IMAGES_PROXY, it) }
				},
			)
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.ignore_ssl_errors,
				summaryRes = R.string.ignore_ssl_errors_summary,
				iconRes = R.drawable.ic_lock,
				checked = sslBypass,
				onCheckedChange = {
					sslBypass = it
					prefs.edit { putBoolean(AppSettings.KEY_SSL_BYPASS, it) }
				},
			)
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.disable_connectivity_check,
				summaryRes = R.string.disable_connectivity_check_summary,
				iconRes = R.drawable.ic_offline,
				checked = noOffline,
				onCheckedChange = {
					noOffline = it
					prefs.edit { putBoolean(AppSettings.KEY_OFFLINE_DISABLED, it) }
				},
			)
		}
	}
}

private fun formatUsage(
	context: android.content.Context,
	bytes: Long?,
	labelRes: Int,
	emptyRes: Int = R.string.computing_,
): String {
	val pattern = context.getString(R.string.memory_usage_pattern)
	return if (bytes != null) {
		pattern.format(FileSize.BYTES.format(context, bytes), context.getString(labelRes))
	} else {
		context.getString(emptyRes)
	}
}
