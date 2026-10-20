package org.inkstack.favourites.ui.categories.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.collection.mutableLongSetOf
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
import kotlinx.coroutines.flow.collectLatest
import org.inkstack.R
import org.inkstack.core.model.FavouriteCategory
import org.inkstack.core.ui.compose.InkstackScaffold
import org.inkstack.core.util.ext.getDisplayMessage
import org.inkstack.favourites.ui.categories.FavouritesCategoriesViewModel
import org.inkstack.favourites.ui.categories.adapter.AllCategoriesListModel
import org.inkstack.favourites.ui.categories.adapter.CategoryListModel
import org.inkstack.list.ui.model.EmptyState
import org.inkstack.list.ui.model.LoadingState

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun FavouriteCategoriesScreen(
	onBack: () -> Unit,
	onOpenAll: () -> Unit,
	onOpenCategory: (FavouriteCategory) -> Unit,
	onEditCategory: (Long) -> Unit,
	onCreate: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: FavouritesCategoriesViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val content by viewModel.content.collectAsState()
	val snackbarHostState = remember { SnackbarHostState() }
	var selectedIds by remember { mutableStateOf(setOf<Long>()) }
	var pendingDelete by remember { mutableStateOf(false) }
	val selectionMode = selectedIds.isNotEmpty()

	LaunchedEffect(viewModel) {
		viewModel.onError.collectLatest { event ->
			event?.consume { error ->
				snackbarHostState.showSnackbar(error.getDisplayMessage(context.resources))
			}
		}
	}

	LaunchedEffect(selectionMode) {
		viewModel.setActionsEnabled(!selectionMode)
	}

	InkstackScaffold(
		modifier = modifier,
		snackbarHostState = snackbarHostState,
		topBar = {
			TopAppBar(
				title = {
					Text(
						if (selectionMode) {
							selectedIds.size.toString()
						} else {
							stringResource(R.string.favourites_categories)
						},
					)
				},
				navigationIcon = {
					IconButton(
						onClick = {
							if (selectionMode) selectedIds = emptySet() else onBack()
						},
					) {
						Icon(
							if (selectionMode) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
							contentDescription = null,
						)
					}
				},
				actions = {
					if (selectionMode) {
						val categories = viewModel.getCategories(mutableLongSetOf(*selectedIds.toLongArray()))
						val canShow = categories.isNotEmpty() && categories.all { !it.isVisibleInLibrary }
						val canHide = categories.isNotEmpty() && categories.all { it.isVisibleInLibrary }
						if (canShow) {
							IconButton(onClick = {
								viewModel.setIsVisible(selectedIds, true)
								selectedIds = emptySet()
							}) {
								Icon(Icons.Default.Visibility, contentDescription = stringResource(R.string.show))
							}
						}
						if (canHide) {
							IconButton(onClick = {
								viewModel.setIsVisible(selectedIds, false)
								selectedIds = emptySet()
							}) {
								Icon(Icons.Default.VisibilityOff, contentDescription = stringResource(R.string.hide))
							}
						}
						IconButton(onClick = { pendingDelete = true }) {
							Icon(Icons.Default.Delete, contentDescription = null)
						}
					}
				},
				windowInsets = WindowInsets.statusBars,
				colors = TopAppBarDefaults.topAppBarColors(),
			)
		},
		floatingActionButton = {
			if (!selectionMode) {
				FloatingActionButton(onClick = onCreate) {
					Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_new_category))
				}
			}
		},
	) { padding ->
		LazyColumn(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding),
			state = rememberLazyListState(),
		) {
			items(
				items = content,
				key = { item ->
					when (item) {
						is CategoryListModel -> "cat:${item.category.id}"
						is AllCategoriesListModel -> "all"
						is LoadingState -> "loading"
						is EmptyState -> "empty"
						else -> item.toString()
					}
				},
			) { item ->
				when (item) {
					is AllCategoriesListModel -> {
						ListItem(
							headlineContent = { Text(stringResource(R.string.all_favourites)) },
							supportingContent = {
								Text(
									if (item.mangaCount == 0) {
										stringResource(R.string.empty)
									} else {
										pluralStringResource(R.plurals.items, item.mangaCount, item.mangaCount)
									},
								)
							},
							trailingContent = {
								if (item.isActionsEnabled) {
									IconButton(onClick = { viewModel.setAllCategoriesVisible(!item.isVisible) }) {
										Icon(
											if (item.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
											contentDescription = null,
										)
									}
								}
							},
							modifier = Modifier
								.fillMaxWidth()
								.clickable(enabled = !selectionMode) { onOpenAll() },
						)
					}
					is CategoryListModel -> {
						val selected = item.category.id in selectedIds
						ListItem(
							headlineContent = { Text(item.category.title) },
							supportingContent = {
								Text(
									if (item.mangaCount == 0) {
										stringResource(R.string.empty)
									} else {
										pluralStringResource(R.plurals.items, item.mangaCount, item.mangaCount)
									},
								)
							},
							trailingContent = {
								if (item.isActionsEnabled && !selectionMode) {
									IconButton(onClick = { onEditCategory(item.category.id) }) {
										Icon(Icons.Default.Edit, contentDescription = null)
									}
								}
							},
							modifier = Modifier
								.fillMaxWidth()
								.combinedClickable(
									onClick = {
										if (selectionMode) {
											selectedIds = if (selected) {
												selectedIds - item.category.id
											} else {
												selectedIds + item.category.id
											}
										} else {
											onOpenCategory(item.category)
										}
									},
									onLongClick = {
										selectedIds = selectedIds + item.category.id
									},
								),
						)
					}
					is EmptyState -> {
						ListItem(headlineContent = { Text(stringResource(R.string.text_empty_holder_primary)) })
					}
					is LoadingState -> {
						ListItem(headlineContent = { Text(stringResource(R.string.loading_)) })
					}
				}
			}
		}
	}

	if (pendingDelete) {
		AlertDialog(
			onDismissRequest = { pendingDelete = false },
			title = { Text(stringResource(R.string.remove_category)) },
			text = { Text(stringResource(R.string.categories_delete_confirm)) },
			confirmButton = {
				TextButton(
					onClick = {
						viewModel.deleteCategories(selectedIds)
						selectedIds = emptySet()
						pendingDelete = false
					},
				) {
					Text(stringResource(R.string.remove))
				}
			},
			dismissButton = {
				TextButton(onClick = { pendingDelete = false }) {
					Text(stringResource(android.R.string.cancel))
				}
			},
		)
	}
}
