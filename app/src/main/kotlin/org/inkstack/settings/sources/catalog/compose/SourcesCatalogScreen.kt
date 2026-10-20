package org.inkstack.settings.sources.catalog.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import org.inkstack.R
import org.inkstack.core.model.getSummary
import org.inkstack.core.model.getTitle
import org.inkstack.core.model.titleResId
import org.inkstack.core.ui.compose.InkstackScaffold
import org.inkstack.core.util.LocaleComparator
import org.inkstack.core.util.ext.getDisplayName
import org.inkstack.core.util.ext.toLocale
import org.inkstack.list.ui.model.LoadingState
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.settings.sources.catalog.SourceCatalogItem
import org.inkstack.settings.sources.catalog.SourcesCatalogViewModel

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SourcesCatalogScreen(
	onBack: () -> Unit,
	onOpenSource: (MangaParserSource) -> Unit,
	modifier: Modifier = Modifier,
	viewModel: SourcesCatalogViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val content by viewModel.content.collectAsState()
	val appliedFilter by viewModel.appliedFilter.collectAsState()
	val hasNewSources by viewModel.hasNewSources.collectAsState()
	val contentTypes by viewModel.contentTypes.collectAsState()
	val snackbarHostState = remember { SnackbarHostState() }
	var query by remember { mutableStateOf("") }
	var searchOpen by remember { mutableStateOf(false) }
	var localeMenuOpen by remember { mutableStateOf(false) }

	LaunchedEffect(viewModel) {
		viewModel.onActionDone.collectLatest { event ->
			event?.consume { action ->
				snackbarHostState.showSnackbar(context.getString(action.stringResId))
			}
		}
	}

	InkstackScaffold(
		modifier = modifier,
		snackbarHostState = snackbarHostState,
		topBar = {
			TopAppBar(
				title = {
					if (searchOpen) {
						OutlinedTextField(
							value = query,
							onValueChange = {
								query = it
								viewModel.performSearch(it)
							},
							modifier = Modifier.fillMaxWidth(),
							singleLine = true,
							placeholder = { Text(stringResource(R.string.search)) },
						)
					} else {
						Text(stringResource(R.string.sources_catalog))
					}
				},
				navigationIcon = {
					IconButton(
						onClick = {
							if (searchOpen) {
								searchOpen = false
								query = ""
								viewModel.performSearch(null)
							} else {
								onBack()
							}
						},
					) {
						Icon(
							if (searchOpen) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
							contentDescription = null,
						)
					}
				},
				actions = {
					if (!searchOpen) {
						IconButton(onClick = { searchOpen = true }) {
							Icon(Icons.Default.Search, contentDescription = null)
						}
					}
				},
				windowInsets = WindowInsets.statusBars,
				colors = TopAppBarDefaults.topAppBarColors(),
			)
		},
	) { padding ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding),
		) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.horizontalScroll(rememberScrollState())
					.padding(horizontal = 16.dp, vertical = 8.dp),
				horizontalArrangement = Arrangement.spacedBy(8.dp),
			) {
				FilterChip(
					selected = false,
					onClick = { localeMenuOpen = true },
					label = {
						Text(appliedFilter.locale?.toLocale().getDisplayName(context))
					},
				)
				DropdownMenu(expanded = localeMenuOpen, onDismissRequest = { localeMenuOpen = false }) {
					val locales = viewModel.locales.map { it to it?.toLocale() }
						.sortedWith(compareBy(nullsFirst(LocaleComparator())) { it.second })
					locales.forEach { (code, locale) ->
						DropdownMenuItem(
							text = { Text(locale.getDisplayName(context)) },
							onClick = {
								viewModel.setLocale(code)
								localeMenuOpen = false
							},
						)
					}
				}
				if (hasNewSources) {
					FilterChip(
						selected = appliedFilter.isNewOnly,
						onClick = { viewModel.setNewOnly(!appliedFilter.isNewOnly) },
						label = { Text(stringResource(R.string._new)) },
					)
				}
				contentTypes.forEach { type ->
					FilterChip(
						selected = type in appliedFilter.types,
						onClick = { viewModel.setContentType(type, type !in appliedFilter.types) },
						label = { Text(stringResource(type.titleResId)) },
					)
				}
			}
			LazyColumn(modifier = Modifier.fillMaxSize()) {
				items(
					items = content,
					key = { item ->
						when (item) {
							is SourceCatalogItem.Source -> item.source.name
							is SourceCatalogItem.Hint -> "hint:${item.title}"
							is LoadingState -> "loading"
							else -> item.toString()
						}
					},
				) { item ->
					when (item) {
						is SourceCatalogItem.Source -> {
							ListItem(
								headlineContent = {
									Text(
										buildString {
											if (item.isHot) append("🔥 ")
											append(item.source.getTitle(context))
										},
									)
								},
								supportingContent = {
									Text(item.source.getSummary(context).orEmpty())
								},
								trailingContent = {
									IconButton(
										onClick = {
											if (viewModel.isPresetMode) {
												viewModel.togglePresetSource(item.source)
											} else {
												viewModel.addSource(item.source)
											}
										},
									) {
										Icon(
											if (item.isInPreset) Icons.Default.Check else Icons.Default.Add,
											contentDescription = null,
										)
									}
								},
								modifier = Modifier
									.fillMaxWidth()
									.combinedClickable(
										onClick = {
											if (viewModel.isPresetMode) {
												viewModel.togglePresetSource(item.source)
											} else {
												onOpenSource(item.source)
											}
										},
										onLongClick = {
											if (viewModel.isPresetMode) {
												viewModel.togglePresetSource(item.source)
											} else {
												viewModel.addSource(item.source)
											}
										},
									),
							)
						}
						is SourceCatalogItem.Hint -> {
							ListItem(
								headlineContent = { Text(stringResource(item.title)) },
								supportingContent = { Text(stringResource(item.text)) },
							)
						}
						is LoadingState -> {
							ListItem(headlineContent = { Text(stringResource(R.string.loading_)) })
						}
					}
				}
			}
		}
	}
}
