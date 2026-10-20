package org.inkstack.alternatives.ui.compose

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.ImageLoader
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest
import org.inkstack.R
import org.inkstack.alternatives.ui.MangaAlternativeModel
import org.inkstack.alternatives.ui.SourceReplacementEmptyState
import org.inkstack.alternatives.ui.SourceReplacementModel
import org.inkstack.alternatives.ui.SourceReplacementViewModel
import org.inkstack.core.model.getTitle
import org.inkstack.core.ui.compose.InkstackScaffold
import org.inkstack.core.util.ext.getDisplayMessage
import org.inkstack.list.ui.model.ButtonFooter
import org.inkstack.list.ui.model.EmptyState
import org.inkstack.list.ui.model.LoadingState
import org.inkstack.parsers.model.Manga

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceReplacementScreen(
	onBack: () -> Unit,
	onOpenDetails: (Manga) -> Unit,
	imageLoader: ImageLoader,
	modifier: Modifier = Modifier,
	viewModel: SourceReplacementViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val list by viewModel.list.collectAsState()
	val current by viewModel.currentManga.collectAsState()
	val queuePos by viewModel.queuePosition.collectAsState()
	val queueSize by viewModel.queueSize.collectAsState()
	val isPlanning by viewModel.isPlanning.collectAsState()
	val snackbarHostState = remember { SnackbarHostState() }
	var searchOpen by remember { mutableStateOf(false) }
	var query by remember { mutableStateOf("") }
	var menuOpen by remember { mutableStateOf(false) }
	var migrateTarget by remember { mutableStateOf<Manga?>(null) }

	DisposableEffect(viewModel) {
		onDispose {
			viewModel.cancelPendingChecks()
		}
	}

	LaunchedEffect(viewModel) {
		viewModel.onError.collectLatest { event ->
			event?.consume { error ->
				snackbarHostState.showSnackbar(error.getDisplayMessage(context.resources))
			}
		}
	}
	LaunchedEffect(viewModel) {
		viewModel.onMigrated.collectLatest { event ->
			event?.consume { result ->
				if (result.remaining > 0) {
					Toast.makeText(context, R.string.migration_completed, Toast.LENGTH_SHORT).show()
				}
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
							onValueChange = { query = it },
							modifier = Modifier.fillMaxWidth(),
							singleLine = true,
						)
					} else {
						Column {
							Text(current.title.ifEmpty { stringResource(R.string.replace_source) })
							if (queueSize > 0) {
								Text(
									"${queuePos + 1} / $queueSize",
									style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
								)
							}
						}
					}
				},
				navigationIcon = {
					IconButton(onClick = {
						if (searchOpen) searchOpen = false else onBack()
					}) {
						Icon(
							if (searchOpen) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
							contentDescription = null,
						)
					}
				},
				actions = {
					if (searchOpen) {
						TextButton(onClick = {
							viewModel.search(query)
							searchOpen = false
						}) { Text(stringResource(android.R.string.ok)) }
					} else {
						IconButton(onClick = {
							query = viewModel.manga.title
							searchOpen = true
						}) {
							Icon(Icons.Default.Search, contentDescription = null)
						}
						IconButton(onClick = { menuOpen = true }) {
							Icon(Icons.Default.MoreVert, contentDescription = null)
						}
						DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
							DropdownMenuItem(
								text = { Text(stringResource(R.string.skip)) },
								onClick = {
									viewModel.skipCurrent()
									menuOpen = false
								},
							)
							DropdownMenuItem(
								text = { Text(stringResource(R.string.next)) },
								onClick = {
									viewModel.selectNextSource()
									menuOpen = false
								},
							)
							DropdownMenuItem(
								text = { Text(stringResource(R.string.choose_source)) },
								onClick = {
									viewModel.showSourceComparison()
									menuOpen = false
								},
							)
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
			if (isPlanning) {
				LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
			}
			LazyColumn(modifier = Modifier.fillMaxSize()) {
				items(
					items = list,
					key = {
						when (it) {
							is MangaAlternativeModel -> it.manga.id
							is SourceReplacementModel -> "src:${it.source?.name ?: "manual"}"
							is LoadingState -> "loading"
							is EmptyState -> "empty"
							is SourceReplacementEmptyState -> "src-empty"
							is ButtonFooter -> "footer:${it.textResId}"
							else -> it.hashCode()
						}
					},
				) { item ->
					when (item) {
						is SourceReplacementModel -> {
							ListItem(
								headlineContent = {
									Text(
										item.source?.getTitle(context)
											?: stringResource(R.string.handle_remaining_manually),
									)
								},
								supportingContent = {
									Text(
										if (item.source == null) {
											stringResource(R.string.handle_remaining_manually_summary)
										} else {
											stringResource(
												R.string.source_match_rate_summary,
												item.matched,
												item.total,
											)
										},
									)
								},
								trailingContent = {
									if (item.source != null) {
										Text("${item.matchPercent}%")
									}
								},
								modifier = Modifier
									.fillMaxWidth()
									.clickable { viewModel.selectSource(item.source) },
							)
						}
						is MangaAlternativeModel -> {
							ListItem(
								headlineContent = { Text(item.manga.title) },
								supportingContent = {
									Text(
										"${item.manga.source.getTitle(context)} • " +
											pluralStringResource(
												R.plurals.chapters,
												item.chaptersCount,
												item.chaptersCount,
											),
									)
								},
								leadingContent = {
									AsyncImage(
										model = item.manga.coverUrl,
										contentDescription = null,
										imageLoader = imageLoader,
									)
								},
								trailingContent = {
									TextButton(onClick = { migrateTarget = item.manga }) {
										Text(stringResource(R.string.migrate))
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
								trailingContent = {
									TextButton(onClick = viewModel::retry) {
										Text(stringResource(R.string.try_again))
									}
								},
							)
						}
						is SourceReplacementEmptyState -> {
							ListItem(
								headlineContent = { Text(stringResource(R.string.nothing_found)) },
								supportingContent = {
									Text(stringResource(R.string.source_replacement_empty_summary))
								},
								trailingContent = {
									Row {
										TextButton(onClick = { searchOpen = true }) {
											Text(stringResource(R.string.edit))
										}
										TextButton(onClick = viewModel::skipCurrent) {
											Text(stringResource(R.string.skip))
										}
									}
								},
							)
						}
						is ButtonFooter -> {
							ListItem(
								headlineContent = {
									TextButton(onClick = {
										when (item.textResId) {
											R.string.skip -> viewModel.skipCurrent()
											R.string.choose_source -> viewModel.retry()
											else -> viewModel.retry()
										}
									}) {
										Text(stringResource(item.textResId))
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
			title = { Text(stringResource(R.string.replace_source)) },
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
				TextButton(onClick = {
					viewModel.migrate(target)
					migrateTarget = null
				}) { Text(stringResource(R.string.migrate)) }
			},
			dismissButton = {
				TextButton(onClick = { migrateTarget = null }) {
					Text(stringResource(android.R.string.cancel))
				}
			},
		)
	}
}
