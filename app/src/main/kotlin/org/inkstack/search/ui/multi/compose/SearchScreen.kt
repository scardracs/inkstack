package org.inkstack.search.ui.multi.compose

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.ImageLoader
import org.inkstack.R
import org.inkstack.core.ui.compose.InkstackScaffold
import org.inkstack.list.ui.compose.MangaListPane
import org.inkstack.list.ui.compose.rememberMangaListSelection
import org.inkstack.list.ui.model.ListHeader
import org.inkstack.list.ui.model.ListModel
import org.inkstack.parsers.model.Manga
import org.inkstack.parsers.model.MangaSource
import org.inkstack.search.domain.SearchKind
import org.inkstack.search.ui.multi.SearchResultsListModel
import org.inkstack.search.ui.multi.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
	imageLoader: ImageLoader,
	onBack: () -> Unit,
	onMangaClick: (Manga) -> Unit,
	onSourceClick: (MangaSource, String) -> Unit,
	modifier: Modifier = Modifier,
	viewModel: SearchViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val content by viewModel.list.collectAsState()
	val flatContent = remember(content) {
		buildList<ListModel> {
			content.forEach { item ->
				when (item) {
					is SearchResultsListModel -> {
						add(ListHeader(item.getTitle(context)))
						addAll(item.list)
					}
					else -> add(item)
				}
			}
		}
	}
	val title = when (viewModel.kind) {
		SearchKind.SIMPLE,
		SearchKind.TITLE -> viewModel.query
		SearchKind.AUTHOR -> stringResource(
			R.string.inline_preference_pattern,
			stringResource(R.string.author),
			viewModel.query,
		)
		SearchKind.TAG -> stringResource(
			R.string.inline_preference_pattern,
			stringResource(R.string.genre),
			viewModel.query,
		)
	}

	InkstackScaffold(
		modifier = modifier,
		topBar = {
			TopAppBar(
				title = { Text(title) },
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
		val selection = rememberMangaListSelection(
			content = flatContent,
			menuRes = R.menu.mode_remote,
		)
		MangaListPane(
			content = flatContent,
			onMangaClick = onMangaClick,
			selection = selection,
			onSourceClick = { source -> onSourceClick(source, viewModel.query) },
			onEmptyAction = viewModel::continueSearch,
			onRetry = viewModel::retry,
			imageLoader = imageLoader,
			contentPadding = padding,
			modifier = Modifier.fillMaxSize(),
		)
	}
}
