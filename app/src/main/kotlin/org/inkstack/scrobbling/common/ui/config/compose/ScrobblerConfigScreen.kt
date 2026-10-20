package org.inkstack.scrobbling.common.ui.config.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest
import org.inkstack.R
import org.inkstack.core.ui.compose.InkstackScaffold
import org.inkstack.core.util.ext.getDisplayMessage
import org.inkstack.list.ui.model.EmptyState
import org.inkstack.list.ui.model.ListHeader
import org.inkstack.scrobbling.common.domain.model.ScrobblingInfo
import org.inkstack.scrobbling.common.ui.config.ScrobblerConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrobblerConfigScreen(
	onBack: () -> Unit,
	onOpenManga: (Long) -> Unit,
	onLoggedOut: () -> Unit,
	onAuthCode: (String) -> Unit,
	authCode: String?,
	modifier: Modifier = Modifier,
	viewModel: ScrobblerConfigViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val content by viewModel.content.collectAsState()
	val user by viewModel.user.collectAsState()
	val isLoading by viewModel.isLoading.collectAsState()
	val snackbarHostState = remember { SnackbarHostState() }
	var showUserDialog by remember { mutableStateOf(false) }

	LaunchedEffect(authCode) {
		if (!authCode.isNullOrEmpty()) viewModel.onAuthCodeReceived(authCode)
	}
	LaunchedEffect(viewModel) {
		viewModel.onError.collectLatest { event ->
			event?.consume { error ->
				snackbarHostState.showSnackbar(error.getDisplayMessage(context.resources))
			}
		}
	}
	LaunchedEffect(viewModel) {
		viewModel.onLoggedOut.collectLatest { event ->
			event?.consume { onLoggedOut() }
		}
	}

	InkstackScaffold(
		modifier = modifier,
		snackbarHostState = snackbarHostState,
		topBar = {
			TopAppBar(
				title = { Text(stringResource(viewModel.titleResId)) },
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
					}
				},
				actions = {
					IconButton(onClick = { showUserDialog = true }) {
						AsyncImage(
							model = user?.avatar,
							contentDescription = null,
							modifier = Modifier
								.size(28.dp)
								.clip(CircleShape),
							contentScale = ContentScale.Crop,
						)
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
			if (isLoading) {
				item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
			}
			items(
				items = content,
				key = {
					when (it) {
						is ScrobblingInfo -> "${it.scrobbler}:${it.mangaId}"
						is ListHeader -> "h:${it.hashCode()}"
						is EmptyState -> "empty"
						else -> it.hashCode()
					}
				},
			) { item ->
				when (item) {
					is ListHeader -> {
						ListItem(
							headlineContent = {
								Text(item.getText(context)?.toString().orEmpty())
							},
						)
					}
					is ScrobblingInfo -> {
						ListItem(
							headlineContent = { Text(item.title) },
							supportingContent = item.description?.let { { Text(it.toString()) } },
							leadingContent = {
								AsyncImage(
									model = item.coverUrl,
									contentDescription = null,
									modifier = Modifier.size(48.dp),
									contentScale = ContentScale.Crop,
								)
							},
							modifier = Modifier
								.fillMaxWidth()
								.clickable { onOpenManga(item.mangaId) },
						)
					}
					is EmptyState -> {
						ListItem(headlineContent = { Text(stringResource(item.textPrimary)) })
					}
				}
			}
		}
	}

	if (showUserDialog) {
		AlertDialog(
			onDismissRequest = { showUserDialog = false },
			title = { Text(stringResource(viewModel.titleResId)) },
			text = {
				Text(stringResource(R.string.logged_in_as, user?.nickname.orEmpty()))
			},
			confirmButton = {
				TextButton(onClick = {
					showUserDialog = false
					viewModel.logout()
				}) { Text(stringResource(R.string.logout)) }
			},
			dismissButton = {
				TextButton(onClick = { showUserDialog = false }) {
					Text(stringResource(R.string.close))
				}
			},
		)
	}
}
