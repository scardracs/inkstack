package org.inkstack.settings.compose.sections

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHostState
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
import androidx.core.content.edit
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import org.inkstack.R
import org.inkstack.core.prefs.AppSettings
import org.inkstack.core.util.FileSize
import org.inkstack.core.util.ext.getDisplayMessage
import org.inkstack.core.util.ext.getQuantityStringSafe
import org.inkstack.local.data.CacheDir
import org.inkstack.settings.compose.prefs.NavigationPreferenceItem
import org.inkstack.settings.compose.prefs.SwitchPreferenceItem
import org.inkstack.settings.compose.prefs.rememberSharedPreferences
import org.inkstack.settings.userdata.storage.DataCleanupSettingsViewModel

@Composable
fun DataCleanupSettingsScreen(
	onBack: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: DataCleanupSettingsViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val prefs = rememberSharedPreferences()
	val snackbarHostState = remember { SnackbarHostState() }
	val pagesSize by checkNotNull(viewModel.cacheSizes[CacheDir.PAGES]).collectAsState()
	val thumbsSize by checkNotNull(viewModel.cacheSizes[CacheDir.THUMBS]).collectAsState()
	val httpSize by viewModel.httpCacheSize.collectAsState()
	val searchCount by viewModel.searchHistoryCount.collectAsState()
	val feedCount by viewModel.feedItemsCount.collectAsState()
	val loadingKeys by viewModel.loadingKeys.collectAsState()
	var autoClear by remember { mutableStateOf(prefs.getBoolean(AppSettings.KEY_CHAPTERS_CLEAR_AUTO, false)) }
	var confirm by remember { mutableStateOf<ConfirmAction?>(null) }

	fun formatSize(size: Long) = if (size < 0) {
		context.getString(R.string.computing_)
	} else {
		FileSize.BYTES.format(context, size)
	}

	fun formatCount(count: Int) = if (count < 0) {
		context.getString(R.string.loading_)
	} else {
		context.resources.getQuantityStringSafe(R.plurals.items, count, count)
	}

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
	LaunchedEffect(viewModel) {
		viewModel.onChaptersCleanedUp.collectLatest { event ->
			event?.consume { result ->
				val text = if (result.first == 0 && result.second == 0L) {
					context.getString(R.string.no_chapters_deleted)
				} else {
					context.getString(
						R.string.chapters_deleted_pattern,
						context.resources.getQuantityStringSafe(R.plurals.chapters, result.first, result.first),
						FileSize.BYTES.format(context, result.second),
					)
				}
				snackbarHostState.showSnackbar(text)
			}
		}
	}

	SettingsSectionScaffold(
		titleRes = R.string.data_removal,
		onBack = onBack,
		modifier = modifier,
		snackbarHostState = snackbarHostState,
	) {
		item {
			NavigationPreferenceItem(
				titleRes = R.string.clear_search_history,
				summary = formatCount(searchCount),
				iconRes = R.drawable.ic_history,
				enabled = AppSettings.KEY_SEARCH_HISTORY_CLEAR !in loadingKeys,
				onClick = { confirm = ConfirmAction.SearchHistory },
			)
		}
		item {
			NavigationPreferenceItem(
				titleRes = R.string.clear_updates_feed,
				summary = formatCount(feedCount),
				iconRes = R.drawable.ic_feed,
				enabled = AppSettings.KEY_UPDATES_FEED_CLEAR !in loadingKeys,
				onClick = { viewModel.clearUpdatesFeed() },
			)
		}
		item {
			NavigationPreferenceItem(
				titleRes = R.string.clear_thumbs_cache,
				summary = formatSize(thumbsSize),
				iconRes = R.drawable.ic_images,
				enabled = AppSettings.KEY_THUMBS_CACHE_CLEAR !in loadingKeys,
				onClick = {
					viewModel.clearCache(AppSettings.KEY_THUMBS_CACHE_CLEAR, CacheDir.THUMBS, CacheDir.FAVICONS)
				},
			)
		}
		item {
			NavigationPreferenceItem(
				titleRes = R.string.clear_pages_cache,
				summary = formatSize(pagesSize),
				iconRes = R.drawable.ic_book_page,
				enabled = AppSettings.KEY_PAGES_CACHE_CLEAR !in loadingKeys,
				onClick = { viewModel.clearCache(AppSettings.KEY_PAGES_CACHE_CLEAR, CacheDir.PAGES) },
			)
		}
		item {
			NavigationPreferenceItem(
				titleRes = R.string.clear_network_cache,
				summary = formatSize(httpSize),
				iconRes = R.drawable.ic_web,
				enabled = AppSettings.KEY_HTTP_CACHE_CLEAR !in loadingKeys,
				onClick = { viewModel.clearHttpCache() },
			)
		}
		item {
			NavigationPreferenceItem(
				titleRes = R.string.clear_database,
				summaryRes = R.string.clear_database_summary,
				iconRes = R.drawable.ic_storage,
				enabled = AppSettings.KEY_CLEAR_MANGA_DATA !in loadingKeys,
				onClick = { viewModel.clearMangaData() },
			)
		}
		item {
			NavigationPreferenceItem(
				titleRes = R.string.clear_cookies,
				summaryRes = R.string.clear_cookies_summary,
				iconRes = R.drawable.ic_data_privacy,
				enabled = AppSettings.KEY_COOKIES_CLEAR !in loadingKeys,
				onClick = { confirm = ConfirmAction.Cookies },
			)
		}
		if (viewModel.isBrowserDataCleanupEnabled) {
			item {
				NavigationPreferenceItem(
					titleRes = R.string.clear_browser_data,
					summaryRes = R.string.clear_browser_data_summary,
					iconRes = R.drawable.ic_web,
					enabled = AppSettings.KEY_WEBVIEW_CLEAR !in loadingKeys,
					onClick = { viewModel.clearBrowserData() },
				)
			}
		}
		item {
			NavigationPreferenceItem(
				titleRes = R.string.delete_read_chapters,
				summaryRes = R.string.delete_read_chapters_summary,
				iconRes = R.drawable.ic_delete,
				enabled = AppSettings.KEY_CHAPTERS_CLEAR !in loadingKeys,
				onClick = { confirm = ConfirmAction.Chapters },
			)
		}
		item {
			SwitchPreferenceItem(
				titleRes = R.string.delete_read_chapters_auto,
				summaryRes = R.string.runs_on_app_start,
				iconRes = R.drawable.ic_delete_all,
				checked = autoClear,
				onCheckedChange = {
					autoClear = it
					prefs.edit { putBoolean(AppSettings.KEY_CHAPTERS_CLEAR_AUTO, it) }
				},
			)
		}
	}

	confirm?.let { action ->
		val (title, message, positive) = when (action) {
			ConfirmAction.SearchHistory -> Triple(
				R.string.clear_search_history,
				R.string.text_clear_search_history_prompt,
				R.string.clear,
			)
			ConfirmAction.Cookies -> Triple(
				R.string.clear_cookies,
				R.string.text_clear_cookies_prompt,
				R.string.clear,
			)
			ConfirmAction.Chapters -> Triple(
				R.string.delete_read_chapters,
				R.string.delete_read_chapters_prompt,
				R.string.delete,
			)
		}
		AlertDialog(
			onDismissRequest = { confirm = null },
			title = { Text(stringResource(title)) },
			text = { Text(stringResource(message)) },
			confirmButton = {
				TextButton(
					onClick = {
						when (action) {
							ConfirmAction.SearchHistory -> viewModel.clearSearchHistory()
							ConfirmAction.Cookies -> viewModel.clearCookies()
							ConfirmAction.Chapters -> viewModel.cleanupChapters()
						}
						confirm = null
					},
				) { Text(stringResource(positive)) }
			},
			dismissButton = {
				TextButton(onClick = { confirm = null }) {
					Text(stringResource(android.R.string.cancel))
				}
			},
		)
	}
}

private enum class ConfirmAction { SearchHistory, Cookies, Chapters }
