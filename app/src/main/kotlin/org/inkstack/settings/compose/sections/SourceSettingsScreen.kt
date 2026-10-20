package org.inkstack.settings.compose.sections

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.material3.SnackbarHostState
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
import androidx.core.content.edit
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import okhttp3.HttpUrl
import org.inkstack.R
import org.inkstack.core.model.MangaSource
import org.inkstack.core.model.getTitle
import org.inkstack.core.nav.router
import org.inkstack.core.network.cookies.MutableCookieJar
import org.inkstack.core.parser.CachingMangaRepository
import org.inkstack.core.parser.EmptyMangaRepository
import org.inkstack.core.parser.MangaRepository
import org.inkstack.core.parser.ParserMangaRepository
import org.inkstack.core.prefs.AppSettings
import org.inkstack.core.prefs.SourceSettings
import org.inkstack.core.ui.BaseViewModel
import org.inkstack.core.ui.util.ReversibleAction
import org.inkstack.core.util.ext.MutableEventFlow
import org.inkstack.core.util.ext.call
import org.inkstack.core.util.ext.findActivity
import org.inkstack.core.util.ext.getDisplayMessage
import org.inkstack.explore.data.MangaSourcesRepository
import org.inkstack.parsers.MangaParserAuthProvider
import org.inkstack.parsers.config.ConfigKey
import org.inkstack.parsers.exception.AuthRequiredException
import org.inkstack.parsers.network.UserAgents
import org.inkstack.parsers.util.mapToArray
import org.inkstack.settings.compose.prefs.EditTextPreferenceItem
import org.inkstack.settings.compose.prefs.ListPreferenceItem
import org.inkstack.settings.compose.prefs.NavigationPreferenceItem
import org.inkstack.settings.compose.prefs.SwitchPreferenceItem
import java.io.File

@HiltViewModel(assistedFactory = SourceSettingsComposeViewModel.Factory::class)
class SourceSettingsComposeViewModel @AssistedInject constructor(
	@Assisted sourceName: String,
	@ApplicationContext appContext: Context,
	mangaRepositoryFactory: MangaRepository.Factory,
	private val cookieJar: MutableCookieJar,
	private val mangaSourcesRepository: MangaSourcesRepository,
	val settings: AppSettings,
) : BaseViewModel(), SharedPreferences.OnSharedPreferenceChangeListener {

	@AssistedFactory
	interface Factory {
		fun create(sourceName: String): SourceSettingsComposeViewModel
	}

	val source = MangaSource(sourceName)
	val repository = mangaRepositoryFactory.create(source)
	val prefs: SharedPreferences = appContext.getSharedPreferences(
		source.name.replace(File.separatorChar, '$'),
		Context.MODE_PRIVATE,
	)

	val onActionDone = MutableEventFlow<ReversibleAction>()
	val username = MutableStateFlow<String?>(null)
	val isAuthorized = MutableStateFlow<Boolean?>(null)
	val browserUrl = MutableStateFlow<String?>(null)
	val isEnabled = mangaSourcesRepository.observeIsEnabled(source)
	private var usernameLoadJob: Job? = null

	init {
		when (repository) {
			is ParserMangaRepository -> {
				browserUrl.value = "https://${repository.domain}"
				repository.getConfig().subscribe(this)
				loadUsername(repository.getAuthProvider())
			}
		}
	}

	override fun onCleared() {
		when (repository) {
			is ParserMangaRepository -> repository.getConfig().unsubscribe(this)
		}
		super.onCleared()
	}

	override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
		if (repository is CachingMangaRepository) {
			if (key != SourceSettings.KEY_SLOWDOWN && key != SourceSettings.KEY_SORT_ORDER) {
				repository.invalidateCache()
			}
		}
		if (repository is ParserMangaRepository && key == SourceSettings.KEY_DOMAIN) {
			browserUrl.value = "https://${repository.domain}"
		}
	}

	fun onResume() {
		if (usernameLoadJob?.isActive != true && repository is ParserMangaRepository) {
			loadUsername(repository.getAuthProvider())
		}
	}

	fun clearCookies() {
		if (repository !is ParserMangaRepository) return
		launchLoadingJob(Dispatchers.Default) {
			val url = HttpUrl.Builder().scheme("https").host(repository.domain).build()
			cookieJar.removeCookies(url, null)
			onActionDone.call(ReversibleAction(R.string.cookies_cleared, null))
			loadUsername(repository.getAuthProvider())
		}
	}

	fun setEnabled(value: Boolean) {
		launchJob(Dispatchers.Default) {
			mangaSourcesRepository.setSourcesEnabled(setOf(source), value)
		}
	}

	private fun loadUsername(authProvider: MangaParserAuthProvider?) {
		usernameLoadJob?.cancel()
		usernameLoadJob = launchLoadingJob(Dispatchers.Default) {
			try {
				username.value = null
				isAuthorized.value = null
				isAuthorized.value = authProvider?.isAuthorized()
				username.value = authProvider?.getUsername()
			} catch (_: AuthRequiredException) {
			}
		}
	}
}

@Composable
fun SourceSettingsScreen(
	sourceName: String,
	onBack: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: SourceSettingsComposeViewModel = hiltViewModel(
		key = sourceName,
		creationCallback = { factory: SourceSettingsComposeViewModel.Factory -> factory.create(sourceName) },
	),
) {
	val context = LocalContext.current
	val snackbarHostState = remember { SnackbarHostState() }
	val activity = context.findActivity() as? FragmentActivity
	val prefs = viewModel.prefs
	val title = remember(sourceName) { MangaSource(sourceName).getTitle(context) }
	val isValid = viewModel.repository !is EmptyMangaRepository
	val enabled by viewModel.isEnabled.collectAsState(initial = false)
	val authorized by viewModel.isAuthorized.collectAsState()
	val username by viewModel.username.collectAsState()
	val browserUrl by viewModel.browserUrl.collectAsState()
	val isLoading by viewModel.isLoading.collectAsState()
	val allEnabled = viewModel.settings.isAllSourcesEnabled
	var slowdown by remember { mutableStateOf(prefs.getBoolean(SourceSettings.KEY_SLOWDOWN, false)) }
	var noCaptcha by remember { mutableStateOf(prefs.getBoolean(SourceSettings.KEY_NO_CAPTCHA, false)) }
	var noAutoCaptcha by remember { mutableStateOf(prefs.getBoolean(SourceSettings.KEY_NO_AUTO_CAPTCHA, false)) }

	LaunchedEffect(Unit) { viewModel.onResume() }
	LaunchedEffect(viewModel) {
		viewModel.onError.collectLatest { event ->
			event?.consume { error ->
				snackbarHostState.showSnackbar(error.getDisplayMessage(context.resources))
			}
		}
	}
	LaunchedEffect(viewModel) {
		viewModel.onActionDone.collectLatest { event ->
			event?.consume { action ->
				snackbarHostState.showSnackbar(context.getString(action.stringResId))
			}
		}
	}

	val configKeys = remember(viewModel.repository) {
		(viewModel.repository as? ParserMangaRepository)?.getConfigKeys().orEmpty()
	}

	SettingsSectionScaffold(
		titleRes = R.string.remote_sources,
		title = title,
		onBack = onBack,
		modifier = modifier,
		snackbarHostState = snackbarHostState,
	) {
		if (isValid && !allEnabled) {
			item {
				SwitchPreferenceItem(
					titleRes = R.string.enable_source,
					iconRes = R.drawable.ic_check,
					checked = enabled,
					onCheckedChange = { viewModel.setEnabled(it) },
				)
			}
		}
		configKeys.forEach { key ->
			when (key) {
				is ConfigKey.Domain -> item(key = key.key) {
					var value by remember {
						mutableStateOf(prefs.getString(key.key, key.defaultValue).orEmpty())
					}
					EditTextPreferenceItem(
						titleRes = R.string.domain,
						value = value,
						summary = value.ifEmpty { key.defaultValue },
						neutralLabelRes = R.string.reset,
						onNeutral = {
							value = ""
							prefs.edit { putString(key.key, "") }
						},
						onValueChange = {
							value = it
							prefs.edit { putString(key.key, it) }
						},
					)
				}
				is ConfigKey.UserAgent -> item(key = key.key) {
					var value by remember {
						mutableStateOf(prefs.getString(key.key, key.defaultValue).orEmpty())
					}
					val presets = arrayOf(
						UserAgents.FIREFOX_MOBILE,
						UserAgents.CHROME_MOBILE,
						UserAgents.FIREFOX_DESKTOP,
						UserAgents.CHROME_DESKTOP,
					)
					ListPreferenceItem(
						titleRes = R.string.user_agent,
						entries = presets,
						entryValues = presets,
						value = value.ifEmpty { key.defaultValue },
						onValueChange = {
							value = it
							prefs.edit { putString(key.key, it) }
						},
					)
				}
				is ConfigKey.ShowSuspiciousContent -> item(key = key.key) {
					var checked by remember { mutableStateOf(prefs.getBoolean(key.key, key.defaultValue)) }
					SwitchPreferenceItem(
						titleRes = R.string.show_suspicious_content,
						checked = checked,
						onCheckedChange = {
							checked = it
							prefs.edit { putBoolean(key.key, it) }
						},
					)
				}
				is ConfigKey.SplitByTranslations -> item(key = key.key) {
					var checked by remember { mutableStateOf(prefs.getBoolean(key.key, key.defaultValue)) }
					SwitchPreferenceItem(
						titleRes = R.string.split_by_translations,
						summaryRes = R.string.split_by_translations_summary,
						checked = checked,
						onCheckedChange = {
							checked = it
							prefs.edit { putBoolean(key.key, it) }
						},
					)
				}
				is ConfigKey.PreferredImageServer -> item(key = key.key) {
					val entries = key.presetValues.values.mapToArray {
						it ?: context.getString(R.string.automatic)
					}
					val values = key.presetValues.keys.mapToArray { it.orEmpty() }
					var value by remember {
						mutableStateOf(prefs.getString(key.key, key.defaultValue.orEmpty()).orEmpty())
					}
					ListPreferenceItem(
						titleRes = R.string.image_server,
						entries = entries,
						entryValues = values,
						value = value,
						onValueChange = {
							value = it
							prefs.edit { putString(key.key, it) }
						},
					)
				}
				is ConfigKey.DisableUpdateChecking, is ConfigKey.InterceptCloudflare -> Unit
			}
		}
		if ((viewModel.repository as? ParserMangaRepository)?.getAuthProvider() != null) {
			item {
				NavigationPreferenceItem(
					titleRes = R.string.sign_in,
					summary = username?.let { stringResource(R.string.logged_in_as, it) },
					iconRes = R.drawable.ic_user,
					enabled = authorized != true && !isLoading,
					onClick = { activity?.router?.openSourceAuth(viewModel.source) },
				)
			}
		}
		item {
			NavigationPreferenceItem(
				titleRes = R.string.clear_cookies,
				summaryRes = R.string.clear_source_cookies_summary,
				iconRes = R.drawable.ic_data_privacy,
				onClick = { viewModel.clearCookies() },
			)
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.disable_captcha_auto_solve,
				summaryRes = R.string.disable_captcha_auto_solve_summary,
				iconRes = R.drawable.ic_disable,
				checked = noAutoCaptcha,
				onCheckedChange = {
					noAutoCaptcha = it
					prefs.edit { putBoolean(SourceSettings.KEY_NO_AUTO_CAPTCHA, it) }
				},
			)
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.disable_captcha_notifications,
				summaryRes = R.string.disable_captcha_notifications_summary,
				iconRes = R.drawable.ic_notification,
				checked = noCaptcha,
				onCheckedChange = {
					noCaptcha = it
					prefs.edit { putBoolean(SourceSettings.KEY_NO_CAPTCHA, it) }
				},
			)
		}
		if (isValid) {
			item {
				SwitchPreferenceItem(
					titleRes = R.string.download_slowdown,
					summaryRes = R.string.download_slowdown_summary,
					iconRes = R.drawable.ic_timelapse,
					checked = slowdown,
					onCheckedChange = {
						slowdown = it
						prefs.edit { putBoolean(SourceSettings.KEY_SLOWDOWN, it) }
					},
				)
			}
		}
		if (browserUrl != null) {
			item {
				NavigationPreferenceItem(
					titleRes = R.string.open_in_browser,
					summary = browserUrl,
					iconRes = R.drawable.ic_open_external,
					onClick = {
						activity?.router?.openBrowser(
							url = browserUrl ?: return@NavigationPreferenceItem,
							source = viewModel.source,
							title = title,
						)
					},
				)
			}
		}
	}
}
