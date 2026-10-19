package org.inkstack.search.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.ImageLoader
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.inkstack.R
import org.inkstack.core.exceptions.resolve.ExceptionResolver
import org.inkstack.core.model.getSummary
import org.inkstack.core.model.getTitle
import org.inkstack.core.nav.AppRouter
import org.inkstack.core.ui.compose.InkstackScaffold
import org.inkstack.core.ui.model.titleRes
import org.inkstack.core.ui.util.ReversibleAction
import org.inkstack.core.ui.util.reverseAsync
import org.inkstack.core.util.ShareHelper
import org.inkstack.core.util.ext.findActivity
import org.inkstack.core.util.ext.getCauseUrl
import org.inkstack.core.util.ext.isHttpUrl
import org.inkstack.filter.ui.FilterHeaderProducer
import org.inkstack.list.domain.QuickFilterListener
import org.inkstack.list.ui.compose.MangaListPane
import org.inkstack.list.ui.compose.rememberMangaListSelection
import org.inkstack.list.ui.config.ListConfigSection
import org.inkstack.list.ui.model.ErrorState
import org.inkstack.local.ui.LocalListViewModel
import org.inkstack.parsers.model.MangaListFilter
import org.inkstack.parsers.model.SortOrder
import org.inkstack.remotelist.ui.RemoteListViewModel
import org.inkstack.search.domain.SearchKind

@Composable
fun MangaListScreen(
	isLocal: Boolean,
	imageLoader: ImageLoader,
	filterHeaderProducer: FilterHeaderProducer,
	exceptionResolver: ExceptionResolver,
	initialFilter: MangaListFilter?,
	initialSortOrder: SortOrder?,
	onBack: () -> Unit,
	modifier: Modifier = Modifier,
) {
	if (isLocal) {
		MangaListBody(
			viewModel = hiltViewModel<LocalListViewModel>(),
			isLocal = true,
			imageLoader = imageLoader,
			filterHeaderProducer = filterHeaderProducer,
			exceptionResolver = exceptionResolver,
			initialFilter = initialFilter,
			initialSortOrder = initialSortOrder,
			onBack = onBack,
			modifier = modifier,
		)
	} else {
		MangaListBody(
			viewModel = hiltViewModel<RemoteListViewModel>(),
			isLocal = false,
			imageLoader = imageLoader,
			filterHeaderProducer = filterHeaderProducer,
			exceptionResolver = exceptionResolver,
			initialFilter = initialFilter,
			initialSortOrder = initialSortOrder,
			onBack = onBack,
			modifier = modifier,
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MangaListBody(
	viewModel: RemoteListViewModel,
	isLocal: Boolean,
	imageLoader: ImageLoader,
	filterHeaderProducer: FilterHeaderProducer,
	exceptionResolver: ExceptionResolver,
	initialFilter: MangaListFilter?,
	initialSortOrder: SortOrder?,
	onBack: () -> Unit,
	modifier: Modifier = Modifier,
) {
	val context = LocalContext.current
	val activity = remember(context) { context.findActivity() as? FragmentActivity }
	val router = remember(activity) { activity?.let(::AppRouter) }
	val scope = rememberCoroutineScope()
	val snackbarHostState = remember { SnackbarHostState() }
	val keyboard = LocalSoftwareKeyboardController.current
	val undoLabel = stringResource(R.string.undo)

	val content by viewModel.content.collectAsState()
	val isLoading by viewModel.isLoading.collectAsState()
	val filterSnapshot by viewModel.filterCoordinator.observe().collectAsState(
		initial = viewModel.filterCoordinator.snapshot(),
	)
	val isRandomLoading by viewModel.isRandomLoading.collectAsState()

	var searchOpen by rememberSaveable { mutableStateOf(false) }
	var searchQuery by rememberSaveable { mutableStateOf(filterSnapshot.listFilter.query.orEmpty()) }
	var menuExpanded by remember { mutableStateOf(false) }
	var appliedInitial by rememberSaveable { mutableStateOf(false) }

	val localVm = viewModel as? LocalListViewModel
	val pickTreeLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.OpenDocumentTree(),
	) { uri: Uri? ->
		if (uri != null && localVm != null) {
			runCatching {
				context.contentResolver.takePersistableUriPermission(
					uri,
					Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
				)
			}
			localVm.onCustomDirectoryPicked(uri)
		}
	}

	LaunchedEffect(initialFilter, initialSortOrder, appliedInitial) {
		if (appliedInitial) return@LaunchedEffect
		appliedInitial = true
		initialSortOrder?.let { viewModel.filterCoordinator.setSortOrder(it) }
		initialFilter?.let { viewModel.filterCoordinator.setAdjusted(it) }
	}

	LaunchedEffect(viewModel) {
		viewModel.onOpenManga.collectLatest { event ->
			event?.consume { manga -> router?.openDetails(manga) }
		}
	}
	LaunchedEffect(viewModel) {
		viewModel.onSourceBroken.collectLatest { event ->
			event?.consume {
				snackbarHostState.showSnackbar(
					message = context.getString(R.string.source_broken_warning),
					duration = SnackbarDuration.Indefinite,
					withDismissAction = true,
				)
			}
		}
	}
	LaunchedEffect(viewModel, exceptionResolver) {
		viewModel.onCaptchaRequired.collectLatest { event ->
			event?.consume { e ->
				viewModel.setCaptchaResolving(true)
				try {
					if (exceptionResolver.resolve(e, tryAutoResolve = true)) {
						viewModel.onRetry()
					}
				} finally {
					viewModel.setCaptchaResolving(false)
				}
			}
		}
	}
	LaunchedEffect(viewModel) {
		viewModel.onActionDone.collectLatest { event ->
			event?.consume { action ->
				showReversibleSnackbar(
					host = snackbarHostState,
					message = context.getString(action.stringResId),
					undoLabel = undoLabel,
					action = action,
				)
			}
		}
	}
	if (localVm != null) {
		LaunchedEffect(localVm) {
			localVm.onMangaRemoved.collectLatest { event ->
				event?.consume {
					snackbarHostState.showSnackbar(context.getString(R.string.removal_completed))
				}
			}
		}
	}

	val selection = rememberMangaListSelection(
		content = content,
		menuRes = if (isLocal) R.menu.mode_local else R.menu.mode_remote,
	) { itemId, ids, manga, finish ->
		when {
			isLocal && itemId == R.id.action_remove -> {
				MaterialAlertDialogBuilder(context)
					.setTitle(R.string.delete_manga)
					.setMessage(context.getString(R.string.text_delete_local_manga_batch))
					.setPositiveButton(R.string.delete) { _, _ ->
						localVm?.delete(ids)
						finish()
					}
					.setNegativeButton(android.R.string.cancel, null)
					.show()
				true
			}
			isLocal && itemId == R.id.action_share -> {
				ShareHelper.shareCbz(context, manga.map { it.url.toUri().toFile() })
				finish()
				true
			}
			else -> false
		}
	}

	val title = viewModel.source.getTitle(context)
	val subtitle = remember(filterSnapshot) {
		filterSnapshot.listFilter.getSummary().takeIf { it.isNotEmpty() }
	}
	val filterApplied = !filterSnapshot.listFilter.isEmpty()
	val sortLabel = stringResource(filterSnapshot.sortOrder.titleRes)
	val searchSupported = viewModel.filterCoordinator.capabilities.isSearchSupported

	InkstackScaffold(
		modifier = modifier,
		snackbarHostState = snackbarHostState,
		topBar = {
			if (searchOpen) {
				TopAppBar(
					title = {
						TextField(
							value = searchQuery,
							onValueChange = { searchQuery = it },
							placeholder = { Text(stringResource(R.string.search)) },
							singleLine = true,
							keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
							keyboardActions = KeyboardActions(onSearch = {
								submitSearch(viewModel, searchQuery, snackbarHostState, undoLabel, scope, context)
								keyboard?.hide()
							}),
							colors = TextFieldDefaults.colors(
								focusedContainerColor = Color.Transparent,
								unfocusedContainerColor = Color.Transparent,
								disabledContainerColor = Color.Transparent,
								focusedIndicatorColor = Color.Transparent,
								unfocusedIndicatorColor = Color.Transparent,
							),
							modifier = Modifier.fillMaxWidth(),
						)
					},
					navigationIcon = {
						IconButton(onClick = {
							searchOpen = false
							keyboard?.hide()
						}) {
							Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
						}
					},
					actions = {
						IconButton(onClick = {
							submitSearch(viewModel, searchQuery, snackbarHostState, undoLabel, scope, context)
							keyboard?.hide()
						}) {
							Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.search))
						}
					},
				)
			} else {
				TopAppBar(
					title = {
						Column {
							Text(title)
							if (subtitle != null) {
								Text(
									text = subtitle.toString(),
									style = MaterialTheme.typography.bodySmall,
									color = MaterialTheme.colorScheme.onSurfaceVariant,
								)
							}
						}
					},
					navigationIcon = {
						IconButton(onClick = onBack) {
							Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
						}
					},
					actions = {
						TextButton(onClick = { router?.showFilterSheet() }) {
							BadgedBox(badge = { if (filterApplied) Badge() }) {
								Text(sortLabel)
							}
						}
						if (searchSupported) {
							IconButton(onClick = {
								searchQuery = filterSnapshot.listFilter.query.orEmpty()
								searchOpen = true
							}) {
								Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.search))
							}
						}
						if (!isLocal) {
							IconButton(
								onClick = { viewModel.openRandom() },
								enabled = !isRandomLoading,
							) {
								Icon(
									painter = painterResource(R.drawable.ic_dice),
									contentDescription = stringResource(R.string.random),
								)
							}
						}
						IconButton(onClick = { menuExpanded = true }) {
							Icon(Icons.Filled.MoreVert, contentDescription = null)
						}
						DropdownMenu(
							expanded = menuExpanded,
							onDismissRequest = { menuExpanded = false },
						) {
							DropdownMenuItem(
								text = { Text(stringResource(R.string.filter)) },
								onClick = {
									menuExpanded = false
									router?.showFilterSheet()
								},
							)
							if (viewModel.filterCoordinator.isFilterApplied) {
								DropdownMenuItem(
									text = { Text(stringResource(R.string.reset_filter)) },
									onClick = {
										menuExpanded = false
										viewModel.filterCoordinator.reset()
									},
								)
							}
							DropdownMenuItem(
								text = { Text(stringResource(R.string.list_options)) },
								onClick = {
									menuExpanded = false
									router?.showListConfigSheet(ListConfigSection.General)
								},
							)
							if (isLocal) {
								DropdownMenuItem(
									text = { Text(stringResource(R.string._import)) },
									onClick = {
										menuExpanded = false
										router?.showImportDialog()
									},
								)
								DropdownMenuItem(
									text = { Text(stringResource(R.string.directories)) },
									onClick = {
										menuExpanded = false
										router?.openDirectoriesSettings()
									},
								)
							} else {
								DropdownMenuItem(
									text = { Text(stringResource(R.string.settings)) },
									onClick = {
										menuExpanded = false
										router?.openSourceSettings(viewModel.source)
									},
								)
							}
						}
					},
				)
			}
		},
	) { padding ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding),
		) {
			FilterHeaderBar(
				filterCoordinator = viewModel.filterCoordinator,
				producer = filterHeaderProducer,
				visible = !searchOpen,
			)
			MangaListPane(
				content = content,
				onMangaClick = { manga -> router?.openDetails(manga) },
				selection = selection,
				quickFilterListener = viewModel as? QuickFilterListener,
				onEmptyAction = {
					when {
						viewModel.filterCoordinator.isFilterApplied -> viewModel.filterCoordinator.reset()
						isLocal -> router?.showImportDialog()
						else -> Unit
					}
				},
				onRetry = {
					scope.launch {
						val error = (content.firstOrNull { it is ErrorState } as? ErrorState)?.exception
						if (error != null && ExceptionResolver.canResolve(error)) {
							if (exceptionResolver.resolve(error, tryAutoResolve = false)) {
								viewModel.onRetry()
							}
						} else {
							viewModel.onRetry()
						}
					}
				},
				onSecondaryErrorAction = { error ->
					val url = error.getCauseUrl()
					if (url?.isHttpUrl() == true) {
						router?.openBrowser(
							url = url,
							source = viewModel.source,
							title = viewModel.source.getTitle(context),
						)
					} else {
						Toast.makeText(context, R.string.operation_not_supported, Toast.LENGTH_SHORT).show()
					}
				},
				onFooterClick = {
					val filter = viewModel.filterCoordinator.snapshot().listFilter
					when {
						!filter.query.isNullOrEmpty() -> router?.openSearch(filter.query.orEmpty(), SearchKind.SIMPLE)
						!filter.author.isNullOrEmpty() -> router?.openSearch(filter.author.orEmpty(), SearchKind.AUTHOR)
						filter.tags.size == 1 -> router?.openSearch(filter.tags.single().title, SearchKind.TAG)
					}
				},
				onTipPrimary = {
					runCatching { pickTreeLauncher.launch(null) }.onFailure {
						Toast.makeText(context, R.string.operation_not_supported, Toast.LENGTH_SHORT).show()
					}
				},
				onTipSecondary = { router?.openDirectoriesSettings() },
				onLoadMore = viewModel::loadNextPage,
				onRefresh = viewModel::onRefresh,
				isRefreshing = isLoading,
				imageLoader = imageLoader,
				fastScrollerEnabled = true,
				contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 24.dp, top = 8.dp),
				modifier = Modifier.fillMaxSize(),
			)
		}
	}
}

private fun submitSearch(
	viewModel: RemoteListViewModel,
	query: String,
	snackbarHostState: SnackbarHostState,
	undoLabel: String,
	scope: CoroutineScope,
	context: android.content.Context,
) {
	val filter = viewModel.filterCoordinator
	val snapshot = filter.snapshot()
	val trimmed = query.trim().ifEmpty { null }
	if (
		!trimmed.isNullOrEmpty() &&
		!filter.capabilities.isSearchWithFiltersSupported &&
		snapshot.listFilter.hasNonSearchOptions()
	) {
		filter.set(MangaListFilter(query = trimmed))
		scope.launch {
			showReversibleSnackbar(
				host = snackbarHostState,
				message = context.getString(R.string.filter_search_warning),
				undoLabel = undoLabel,
				action = ReversibleAction(R.string.filter_search_warning) { filter.set(snapshot.listFilter) },
			)
		}
	} else {
		filter.setQuery(trimmed)
	}
}

private suspend fun showReversibleSnackbar(
	host: SnackbarHostState,
	message: String,
	undoLabel: String,
	action: ReversibleAction,
) {
	val result = host.showSnackbar(
		message = message,
		actionLabel = if (action.handle != null) undoLabel else null,
		duration = if (action.handle != null) SnackbarDuration.Long else SnackbarDuration.Short,
	)
	if (result == SnackbarResult.ActionPerformed) {
		action.handle?.reverseAsync()
	}
}
