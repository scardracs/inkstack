package org.inkstack.details.ui.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil3.ImageLoader
import org.inkstack.core.prefs.AppSettings
import org.inkstack.details.ui.DetailsViewModel
import org.inkstack.parsers.model.Manga
import org.inkstack.parsers.model.MangaChapter

/**
 * Single Compose details scaffold: manga info + inline chapters/pages/bookmarks pager.
 * No bottom peek / Fragment sheet.
 */
@Composable
fun DetailsHost(
	imageLoader: ImageLoader,
	settings: AppSettings,
	viewModel: DetailsViewModel,
	menuVisibility: DetailsMenuVisibility,
	onBack: () -> Unit,
	onRead: (Manga, MangaChapter?, incognito: Boolean) -> Unit,
	onFavorite: (Manga) -> Unit,
	onTagClick: (org.inkstack.parsers.model.MangaTag) -> Unit,
	onSourceClick: (Manga) -> Unit,
	onShare: () -> Unit,
	onDownload: () -> Unit,
	onDelete: () -> Unit,
	onEditOverride: () -> Unit,
	onScrobbling: () -> Unit,
	onStats: () -> Unit,
	onFindSimilar: () -> Unit,
	onAlternatives: () -> Unit,
	onReplaceSource: () -> Unit,
	onOnline: () -> Unit,
	onBrowser: () -> Unit,
	onShortcut: () -> Unit,
) {
	DetailsScreen(
		imageLoader = imageLoader,
		settings = settings,
		viewModel = viewModel,
		onBack = onBack,
		onRead = onRead,
		onFavorite = onFavorite,
		onTagClick = onTagClick,
		onSourceClick = onSourceClick,
		menuVisibility = menuVisibility.copy(chaptersSheet = false),
		pullRefreshEnabled = true,
		onShare = onShare,
		onDownload = onDownload,
		onDelete = onDelete,
		onEditOverride = onEditOverride,
		onScrobbling = onScrobbling,
		onStats = onStats,
		onFindSimilar = onFindSimilar,
		onAlternatives = onAlternatives,
		onReplaceSource = onReplaceSource,
		onOnline = onOnline,
		onBrowser = onBrowser,
		onShortcut = onShortcut,
		onChaptersSheet = {},
		modifier = Modifier.fillMaxSize(),
	)
}
