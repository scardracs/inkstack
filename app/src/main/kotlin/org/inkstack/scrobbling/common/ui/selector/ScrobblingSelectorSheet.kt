package org.inkstack.scrobbling.common.ui.selector

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.inkstack.R
import org.inkstack.core.exceptions.resolve.ExceptionResolver
import org.inkstack.core.nav.AppRouter
import org.inkstack.core.ui.compose.MangaCover
import org.inkstack.core.ui.compose.sheet.ComposeAdaptiveSheet
import org.inkstack.core.util.ext.getDisplayMessage
import org.inkstack.list.ui.model.LoadingFooter
import org.inkstack.list.ui.model.LoadingState
import org.inkstack.scrobbling.common.domain.model.ScrobblerManga
import org.inkstack.scrobbling.common.ui.selector.model.ScrobblerHint

@AndroidEntryPoint
class ScrobblingSelectorSheet : ComposeAdaptiveSheet() {

	private val viewModel by viewModels<ScrobblingSelectorViewModel>()

	@Composable
	override fun sheetTitle(): String = stringResource(R.string.tracking)

	override fun onSheetViewCreated(savedInstanceState: android.os.Bundle?) {
		disableFitToContents()
		val selectedId = arguments?.getInt(AppRouter.KEY_ID, -1) ?: -1
		if (selectedId >= 0) {
			val index = viewModel.availableScrobblers.indexOfFirst { it.scrobblerService.id == selectedId }
			if (index >= 0) {
				viewModel.setScrobblerIndex(index)
			}
		}
	}

	@Composable
	override fun SheetContent(onDismissRequest: () -> Unit) {
		val context = LocalContext.current
		val scope = rememberCoroutineScope()
		val content by viewModel.content.collectAsState()
		val selectedItemId by viewModel.selectedItemId.collectAsState()
		val selectedScrobblerIndex by viewModel.selectedScrobblerIndex.collectAsState()
		val isLoading by viewModel.isLoading.collectAsState()
		var query by remember { mutableStateOf(viewModel.manga.title) }
		val listState = rememberLazyListState()
		val keyboard = LocalSoftwareKeyboardController.current

		LaunchedEffect(viewModel) {
			viewModel.onError.collectLatest { event ->
				event?.consume { error ->
					Toast.makeText(context, error.getDisplayMessage(context.resources), Toast.LENGTH_LONG).show()
					if (viewModel.isEmpty) onDismissRequest()
				}
			}
		}
		LaunchedEffect(viewModel) {
			viewModel.onClose.collectLatest { event ->
				event?.consume { onDismissRequest() }
			}
		}
		LaunchedEffect(listState, content) {
			snapshotFlow {
				val info = listState.layoutInfo
				val last = info.visibleItemsInfo.lastOrNull()?.index ?: -1
				last >= info.totalItemsCount - 4
			}
				.distinctUntilChanged()
				.filter { it }
				.collect { viewModel.loadNextPage() }
		}

		Column(modifier = Modifier.fillMaxSize()) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 8.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				Row(
					modifier = Modifier
						.weight(1f)
						.horizontalScroll(rememberScrollState()),
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					verticalAlignment = Alignment.CenterVertically,
				) {
					viewModel.availableScrobblers.forEachIndexed { index, scrobbler ->
						FilterChip(
							selected = index == selectedScrobblerIndex,
							onClick = { if (!isLoading) viewModel.setScrobblerIndex(index) },
							enabled = !isLoading,
							label = { Text(stringResource(scrobbler.scrobblerService.titleResId)) },
							leadingIcon = {
								Icon(
									painter = painterResource(scrobbler.scrobblerService.iconResId),
									contentDescription = null,
									modifier = Modifier.size(18.dp),
								)
							},
						)
					}
				}
				FilledIconButton(
					onClick = { viewModel.onDoneClick() },
					enabled = !isLoading,
				) {
					if (isLoading) {
						CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
					} else {
						Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.done))
					}
				}
			}

			OutlinedTextField(
				value = query,
				onValueChange = { query = it },
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 16.dp, vertical = 8.dp),
				singleLine = true,
				label = { Text(stringResource(R.string.search)) },
				enabled = !isLoading,
				keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
				keyboardActions = KeyboardActions(
					onSearch = {
						if (query.length >= 3) {
							viewModel.search(query)
							keyboard?.hide()
						}
					},
				),
			)

			when {
				content.any { it is LoadingState } || (content.singleOrNull() is LoadingFooter && viewModel.isEmpty) -> {
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.weight(1f),
						contentAlignment = Alignment.Center,
					) {
						CircularProgressIndicator()
					}
				}
				else -> {
					LazyColumn(
						state = listState,
						modifier = Modifier
							.fillMaxWidth()
							.weight(1f)
							.padding(horizontal = 8.dp),
					) {
						items(
							items = content,
							key = { item ->
								when (item) {
									is ScrobblerManga -> "m-${item.id}"
									is ScrobblerHint -> "h-${item.textPrimary}"
									is LoadingFooter -> "footer"
									else -> item.hashCode()
								}
							},
						) { item ->
							when (item) {
								is ScrobblerManga -> ScrobblerMangaRow(
									item = item,
									selected = item.id == selectedItemId,
									onClick = { viewModel.selectItem(item.id) },
								)
								is ScrobblerHint -> ScrobblerHintBlock(
									hint = item,
									onAction = {
										val error = item.error
										if (error != null) {
											if (ExceptionResolver.canResolve(error)) {
												scope.launch {
													if (exceptionResolver.resolve(error)) {
														viewModel.retry()
													}
												}
											} else {
												viewModel.retry()
											}
										}
									},
								)
								is LoadingFooter -> {
									Box(
										modifier = Modifier
											.fillMaxWidth()
											.padding(16.dp),
										contentAlignment = Alignment.Center,
									) {
										CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
									}
								}
							}
						}
					}
				}
			}
		}
	}
}

@Composable
private fun ScrobblerMangaRow(
	item: ScrobblerManga,
	selected: Boolean,
	onClick: () -> Unit,
) {
	val bg = if (selected) {
		MaterialTheme.colorScheme.secondaryContainer
	} else {
		MaterialTheme.colorScheme.surface
	}
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 4.dp)
			.background(bg, MaterialTheme.shapes.medium)
			.clickable(onClick = onClick)
			.padding(8.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		MangaCover(
			model = item.cover,
			contentDescription = item.name,
			modifier = Modifier.width(48.dp),
		)
		Column(
			modifier = Modifier
				.weight(1f)
				.padding(start = 12.dp),
		) {
			Row(verticalAlignment = Alignment.CenterVertically) {
				Text(
					text = item.name,
					style = MaterialTheme.typography.titleSmall,
					maxLines = 2,
					overflow = TextOverflow.Ellipsis,
					modifier = Modifier.weight(1f, fill = false),
				)
				if (item.isBestMatch) {
					Icon(
						imageVector = Icons.Filled.Star,
						contentDescription = null,
						modifier = Modifier
							.padding(start = 4.dp)
							.size(16.dp),
						tint = MaterialTheme.colorScheme.primary,
					)
				}
			}
			if (!item.altName.isNullOrEmpty()) {
				Text(
					text = item.altName,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
			}
		}
	}
}

@Composable
private fun ScrobblerHintBlock(
	hint: ScrobblerHint,
	onAction: () -> Unit,
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(8.dp),
	) {
		Icon(
			painter = painterResource(hint.icon),
			contentDescription = null,
			modifier = Modifier.size(64.dp),
			tint = MaterialTheme.colorScheme.onSurfaceVariant,
		)
		Text(
			text = stringResource(hint.textPrimary),
			style = MaterialTheme.typography.titleMedium,
		)
		val secondary = when {
			hint.error != null -> hint.error.getDisplayMessage(LocalContext.current.resources)
			hint.textSecondary != 0 -> stringResource(hint.textSecondary)
			else -> null
		}
		if (!secondary.isNullOrEmpty()) {
			Text(
				text = secondary,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
			)
		}
		if (hint.actionStringRes != 0) {
			TextButton(onClick = onAction) {
				Text(stringResource(hint.actionStringRes))
			}
		}
	}
}
