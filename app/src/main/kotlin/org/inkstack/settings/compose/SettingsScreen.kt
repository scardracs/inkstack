package org.inkstack.settings.compose

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import org.inkstack.BuildConfig
import org.inkstack.R
import org.inkstack.core.prefs.AppSettings
import org.inkstack.core.ui.compose.InkstackScaffold

data class SettingsDestination(
	val key: String,
	@StringRes val titleRes: Int,
	@DrawableRes val iconRes: Int,
	val summary: String,
)

@Composable
fun rememberRootSettingsDestinations(enabledSourcesSummary: String): List<SettingsDestination> {
	val context = LocalContext.current
	return remember(enabledSourcesSummary) {
		fun summary(@StringRes vararg items: Int) =
			items.joinToString { context.getString(it) }
		listOf(
			SettingsDestination(
				key = "appearance",
				titleRes = R.string.appearance,
				iconRes = R.drawable.ic_appearance,
				summary = summary(R.string.theme, R.string.list_mode, R.string.language),
			),
			SettingsDestination(
				key = AppSettings.KEY_REMOTE_SOURCES,
				titleRes = R.string.remote_sources,
				iconRes = R.drawable.ic_manga_source,
				summary = enabledSourcesSummary,
			),
			SettingsDestination(
				key = "reader",
				titleRes = R.string.reader_settings,
				iconRes = R.drawable.ic_book_page,
				summary = summary(R.string.read_mode, R.string.scale_mode, R.string.switch_pages),
			),
			SettingsDestination(
				key = "network",
				titleRes = R.string.storage_and_network,
				iconRes = R.drawable.ic_usage,
				summary = summary(R.string.storage_usage, R.string.proxy, R.string.prefetch_content),
			),
			SettingsDestination(
				key = "downloads",
				titleRes = R.string.downloads,
				iconRes = R.drawable.ic_download,
				summary = summary(R.string.manga_save_location, R.string.downloads_wifi_only),
			),
			SettingsDestination(
				key = "tracker",
				titleRes = R.string.check_for_new_chapters,
				iconRes = R.drawable.ic_feed,
				summary = summary(R.string.track_sources, R.string.notifications_settings),
			),
			SettingsDestination(
				key = "services",
				titleRes = R.string.services,
				iconRes = R.drawable.ic_services,
				summary = summary(R.string.suggestions, R.string.sync, R.string.tracking),
			),
			SettingsDestination(
				key = "userdata",
				titleRes = R.string.backup_restore,
				iconRes = R.drawable.ic_backup_restore,
				summary = summary(R.string.create_or_restore_backup, R.string.periodic_backups),
			),
			SettingsDestination(
				key = "about",
				titleRes = R.string.about,
				iconRes = R.drawable.ic_info_outline,
				summary = context.getString(R.string.app_version, BuildConfig.VERSION_NAME),
			),
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
	destinations: List<SettingsDestination>,
	onBack: () -> Unit,
	onOpen: (SettingsDestination) -> Unit,
	modifier: Modifier = Modifier,
) {
	InkstackScaffold(
		modifier = modifier,
		topBar = {
			TopAppBar(
				title = { Text(stringResource(R.string.settings)) },
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
					}
				},
				windowInsets = WindowInsets.statusBars,
				colors = TopAppBarDefaults.topAppBarColors(),
			)
		},
	) { padding ->
		LazyColumn(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding),
		) {
			items(destinations, key = { it.key }) { dest ->
				ListItem(
					headlineContent = { Text(stringResource(dest.titleRes)) },
					supportingContent = { Text(dest.summary) },
					leadingContent = {
						Icon(
							painter = painterResource(dest.iconRes),
							contentDescription = null,
						)
					},
					modifier = Modifier
						.fillMaxWidth()
						.clickable { onOpen(dest) },
				)
			}
		}
	}
}
