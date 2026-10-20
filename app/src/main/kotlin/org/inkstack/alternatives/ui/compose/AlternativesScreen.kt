package org.inkstack.alternatives.ui.compose

import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.ImageLoader
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest
import org.inkstack.R
import org.inkstack.alternatives.domain.AlternativeSortOrder
import org.inkstack.alternatives.ui.MangaAlternativeModel
import org.inkstack.alternatives.ui.AlternativesViewModel
import org.inkstack.alternatives.ui.SourceScopeMenu
import org.inkstack.core.model.getTitle
import org.inkstack.core.ui.compose.InkstackScaffold
import org.inkstack.core.util.ext.getDisplayMessage
import org.inkstack.core.util.ext.getQuantityStringSafe
import org.inkstack.list.ui.model.EmptyState
import org.inkstack.list.ui.model.LoadingState
import org.inkstack.parsers.model.Manga
import org.inkstack.parsers.model.MangaParserSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlternativesScreen(
	onBack: () -> Unit,
	onOpenDetails: (Manga) -> Unit,
	onOpenSearch: (org.inkstack.parsers.model.MangaSource, String) -> Unit,
	imageLoader: ImageLoader,
	modifier: Modifier = Modifier,
	viewModel: AlternativesViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val list by viewModel.list.collectAsState()
	val options by viewModel.options.collectAsState()
	val scopeOptions by viewModel.scopeOptions.collectAsState()
	val searchStatus by viewModel.searchStatus.collectAsState()
	val reference by viewModel.referenceManga.collectAsState()
	val snackbarHostState = remember { SnackbarHostState() }
	var searchOpen by remember { mutableStateOf(false) }
	var query by remember { mutableStateOf(options.query) }
	var menuOpen by remember { mutableStateOf(false) }
	var migrateTarget by remember { mutableStateOf<Manga?>(null) }
	var scopeMenu by remember { mutableStateOf(false) }
	var sortMenu by remember { mutableStateOf(false) }

	LaunchedEffect(viewModel) {
		viewModel.onError.collectLatest { event ->
			event?.consume { error ->
				snackbarHostState.showSnackbar(error.getDisplayMessage(context.resources))
			}
		}
	}
	LaunchedEffect(viewModel) {
		viewModel.onMigrated.collectLatest { event ->
			event?.consume { manga ->
				Toast.makeText(context, R.string.migration_completed, Toast.LENGTH_SHORT).show()
				onOpenDetails(manga)
				onBack()
			}
		}
	}

	val subtitle = remember(reference) {
		buildList {
			add(reference.manga.source.getTitle(context))
			(reference.manga.source as? MangaParserSource)?.locale?.uppercase()?.let(::add)
			add(
				context.resources.getQuantityStringSafe(
					R.plurals.chapters,
					reference.chaptersCount,
					reference.chaptersCount,
				),
			)
		}.joinToString(" • ")
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
							onValueChange = { query = it },
							modifier = Modifier.fillMaxWidth(),
							singleLine = true,
							placeholder = { Text(stringResource(R.string.search_manga)) },
						)
					} else {
						Column {
							Text(stringResource(R.string.alternatives))
							Text(subtitle, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
						}
					}
				},
				navigationIcon = {
					IconButton(
						onClick = {
							if (searchOpen) {
								searchOpen = false
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
					if (searchOpen) {
						TextButton(
							onClick = {
								viewModel.setQuery(query)
								searchOpen = false
							},
						) { Text(stringResource(android.R.string.ok)) }
					} else {
						IconButton(onClick = {
							query = options.query
							searchOpen = true
						}) {
							Icon(Icons.Default.Search, contentDescription = null)
						}
						IconButton(onClick = { menuOpen = true }) {
							Icon(Icons.Default.MoreVert, contentDescription = null)
						}
						DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
							DropdownMenuItem(
								text = { Text(stringResource(R.string.same_language)) },
								onClick = {
									viewModel.setSameLanguageOnly(!options.sameLanguageOnly)
									menuOpen = false
								},
							)
							DropdownMenuItem(
								text = { Text(stringResource(R.string.same_content_type)) },
								onClick = {
									viewModel.setSameContentTypeOnly(!options.sameContentTypeOnly)
									menuOpen = false
								},
							)
							DropdownMenuItem(
								text = { Text(stringResource(R.string.has_chapters)) },
								onClick = {
									viewModel.setHideNoChapters(!options.hideNoChapters)
									menuOpen = false
								},
							)
							if (viewModel.hasCustomOptions()) {
								DropdownMenuItem(
									text = { Text(stringResource(R.string.reset)) },
									onClick = {
										viewModel.resetOptions()
										menuOpen = false
									},
								)
							}
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
			if (searchStatus.isRunning) {
				LinearProgressIndicator(
					progress = {
						if (searchStatus.totalSources > 0) {
							searchStatus.completedSources.toFloat() / searchStatus.totalSources
						} else {
							0f
						}
					},
					modifier = Modifier.fillMaxWidth(),
				)
			}
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.horizontalScroll(rememberScrollState())
					.padding(horizontal = 12.dp, vertical = 8.dp),
				horizontalArrangement = Arrangement.spacedBy(8.dp),
			) {
				InputChip(
					selected = false,
					onClick = {
						query = options.query
						searchOpen = true
					},
					label = { Text(options.query) },
					trailingIcon = if (options.query != viewModel.manga.title) {
						{
							Icon(
								Icons.Default.Close,
								contentDescription = null,
								modifier = Modifier.clickable { viewModel.setQuery(viewModel.manga.title) },
							)
						}
					} else {
						null
					},
				)
				FilterChip(
					selected = false,
					onClick = { scopeMenu = true },
					label = {
						Text(SourceScopeMenu.title(context, options.sourceScope, scopeOptions))
					},
				)
				DropdownMenu(expanded = scopeMenu, onDismissRequest = { scopeMenu = false }) {
					// Simplified: reset to default scopes via menu actions already covered
					DropdownMenuItem(
						text = { Text(stringResource(R.string.reset)) },
						onClick = {
							viewModel.setSourceScope(viewModel.defaultScopeForReset())
							scopeMenu = false
						},
					)
				}
				FilterChip(
					selected = false,
					onClick = { sortMenu = true },
					label = { Text(stringResource(options.sortOrder.titleRes)) },
				)
				DropdownMenu(expanded = sortMenu, onDismissRequest = { sortMenu = false }) {
					AlternativeSortOrder.entries.forEach { sort ->
						DropdownMenuItem(
							text = { Text(stringResource(sort.titleRes)) },
							onClick = {
								viewModel.setSortOrder(sort)
								sortMenu = false
							},
						)
					}
				}
			}
			LazyColumn(modifier = Modifier.fillMaxSize()) {
				items(
					items = list,
					key = {
						when (it) {
							is MangaAlternativeModel -> it.manga.id
							is LoadingState -> "loading"
							is EmptyState -> "empty"
							else -> it.hashCode()
						}
					},
				) { item ->
					when (item) {
						is MangaAlternativeModel -> {
							ListItem(
								headlineContent = { Text(item.manga.title) },
								supportingContent = {
									Text(
										buildString {
											append(item.manga.source.getTitle(context))
											append(" • ")
											append(
												pluralStringResource(
													R.plurals.chapters,
													item.chaptersCount,
													item.chaptersCount,
												),
											)
										},
									)
								},
								leadingContent = {
									AsyncImage(
										model = item.manga.coverUrl,
										contentDescription = null,
										imageLoader = imageLoader,
										modifier = Modifier.padding(4.dp),
									)
								},
								trailingContent = {
									if (item.isMigrationEnabled && item.chaptersCount > 0) {
										TextButton(onClick = { migrateTarget = item.manga }) {
											Text(stringResource(R.string.migrate))
										}
									}
								},
								modifier = Modifier
									.fillMaxWidth()
									.clickable { onOpenDetails(item.manga) },
							)
						}
						is EmptyState -> {
							ListItem(
								headlineContent = { Text(stringResource(item.textPrimary)) },
								supportingContent = item.textSecondary.takeIf { it != 0 }?.let {
									{ Text(stringResource(it)) }
								},
								trailingContent = {
									if (item.actionStringRes != 0) {
										TextButton(onClick = viewModel::retry) {
											Text(stringResource(item.actionStringRes))
										}
									}
								},
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

	migrateTarget?.let { target ->
		AlertDialog(
			onDismissRequest = { migrateTarget = null },
			title = { Text(stringResource(R.string.manga_migration)) },
			text = {
				Text(
					stringResource(
						R.string.migrate_confirmation,
						viewModel.manga.title,
						viewModel.manga.source.getTitle(context),
						target.title,
						target.source.getTitle(context),
					),
				)
			},
			confirmButton = {
				TextButton(
					onClick = {
						viewModel.migrate(target)
						migrateTarget = null
					},
				) { Text(stringResource(R.string.migrate)) }
			},
			dismissButton = {
				TextButton(onClick = { migrateTarget = null }) {
					Text(stringResource(android.R.string.cancel))
				}
			},
		)
	}
}

private val AlternativeSortOrder.titleRes: Int
	get() = when (this) {
		AlternativeSortOrder.BEST_MATCH -> R.string.best_match
		AlternativeSortOrder.MOST_CHAPTERS -> R.string.most_chapters
		AlternativeSortOrder.CLOSEST_CHAPTER_COUNT -> R.string.closest_chapter_count
		AlternativeSortOrder.SOURCE_PRIORITY -> R.string.source_priority
	}
