package org.inkstack.sourcescore.ui.compose

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch
import org.inkstack.R
import org.inkstack.core.nav.router
import org.inkstack.sourcescore.data.ApiErrorDto
import org.inkstack.sourcescore.data.CommentDto
import org.inkstack.sourcescore.data.CommentRulesDto
import org.inkstack.sourcescore.ui.CommentRejection
import org.inkstack.sourcescore.ui.CommunityCommentsViewModel
import org.inkstack.sourcescore.ui.CommunityNameGate
import org.inkstack.sourcescore.ui.ComposeTarget
import java.time.Instant
import java.time.format.DateTimeParseException

private const val MIN_LENGTH = 10

@Composable
private fun <T> LiveData<T>.observeAsState(initial: T): State<T> {
	val lifecycleOwner = LocalLifecycleOwner.current
	val state = remember { mutableStateOf(value ?: initial) }
	DisposableEffect(this, lifecycleOwner) {
		val observer = Observer<T> { state.value = it }
		observe(lifecycleOwner, observer)
		onDispose { removeObserver(observer) }
	}
	return state
}

@Composable
fun CommunityCommentsContent(
	viewModel: CommunityCommentsViewModel,
	nameGate: CommunityNameGate,
	workId: String,
	chapterId: Long?,
	focusCommentId: String?,
	fragmentManager: androidx.fragment.app.FragmentManager,
	modifier: Modifier = Modifier,
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val snackbarHostState = remember { SnackbarHostState() }
	val listState = rememberLazyListState()

	val comments by viewModel.comments.observeAsState(emptyList())
	val total by viewModel.total.observeAsState(0)
	val isLoading by viewModel.isLoading.observeAsState(false)
	val isSending by viewModel.isSending.observeAsState(false)
	val rules by viewModel.rules.observeAsState(CommentRulesDto())
	val compose by viewModel.compose.observeAsState(ComposeTarget())
	val rejection by viewModel.rejection.observeAsState(null as CommentRejection?)
	val loadFailed by viewModel.loadFailed.observeAsState(false)

	var draft by remember { mutableStateOf("") }
	var spoiler by remember { mutableStateOf(false) }
	var revealed by remember { mutableStateOf(setOf<String>()) }
	var pendingDelete by remember { mutableStateOf<CommentDto?>(null) }
	var focused by remember { mutableStateOf(focusCommentId) }

	LaunchedEffect(workId, chapterId) {
		viewModel.start(workId, chapterId)
	}

	LaunchedEffect(comments, focused) {
		val id = focused ?: return@LaunchedEffect
		val index = comments.indexOfFirst { it.id == id }
		if (index >= 0) {
			focused = null
			listState.animateScrollToItem(index)
		}
	}

	LaunchedEffect(compose) {
		val target = compose ?: ComposeTarget()
		if (target.comment == null) {
			draft = ""
			spoiler = false
		} else if (target.isEditing) {
			draft = target.comment.body
		}
	}

	val minimum = rules?.minLength ?: MIN_LENGTH
	val characters = if (draft.isBlank()) 0 else draft.trim().codePointCount(0, draft.trim().length)
	val sendEnabled = characters >= minimum && isSending != true

	Box(
		modifier = modifier
			.fillMaxSize()
			.navigationBarsPadding()
			.imePadding(),
	) {
		Column(Modifier.fillMaxSize()) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 20.dp, vertical = 4.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				Text(
					text = stringResource(R.string.community_comments_heading_count, total ?: 0),
					style = MaterialTheme.typography.bodyLarge,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier.weight(1f),
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
				FilterChip(
					selected = viewModel.currentSort() == CommunityCommentsViewModel.SORT_TOP,
					onClick = { viewModel.setSort(CommunityCommentsViewModel.SORT_TOP) },
					label = { Text(stringResource(R.string.community_comments_sort_top)) },
				)
				Spacer(Modifier.width(8.dp))
				FilterChip(
					selected = viewModel.currentSort() == CommunityCommentsViewModel.SORT_NEW,
					onClick = { viewModel.setSort(CommunityCommentsViewModel.SORT_NEW) },
					label = { Text(stringResource(R.string.community_comments_sort_new)) },
				)
			}

			Box(modifier = Modifier.weight(1f)) {
				when {
					isLoading == true && comments.isEmpty() -> {
						CircularProgressIndicator(Modifier.align(Alignment.Center))
					}
					comments.isEmpty() -> {
						Column(
							modifier = Modifier
								.align(Alignment.Center)
								.padding(32.dp),
							horizontalAlignment = Alignment.CenterHorizontally,
						) {
							Icon(
								painter = painterResource(R.drawable.ic_comment),
								contentDescription = null,
								tint = MaterialTheme.colorScheme.onSurfaceVariant,
							)
							Spacer(Modifier.height(12.dp))
							Text(
								text = stringResource(
									if (loadFailed == true) {
										R.string.community_comments_unavailable
									} else {
										R.string.community_comments_empty
									},
								),
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onSurfaceVariant,
							)
						}
					}
					else -> {
						LazyColumn(
							state = listState,
							modifier = Modifier.fillMaxSize(),
							contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
							verticalArrangement = Arrangement.spacedBy(8.dp),
						) {
							items(comments, key = { it.id }) { comment ->
								CommentRow(
									comment = comment,
									revealed = comment.id in revealed,
									onReveal = { revealed = revealed + comment.id },
									onVote = { viewModel.vote(comment, it) },
									onReply = { viewModel.replyTo(comment) },
									onEdit = { viewModel.editOwn(comment) },
									onDelete = { pendingDelete = comment },
								)
							}
						}
					}
				}
			}

			ComposeBar(
				draft = draft,
				onDraftChange = { draft = it },
				spoiler = spoiler,
				onSpoilerChange = { spoiler = it },
				compose = compose ?: ComposeTarget(),
				helperText = if (characters in 1 until minimum) {
					stringResource(R.string.community_comment_too_short, minimum)
				} else {
					null
				},
				sendEnabled = sendEnabled,
				onCancel = { viewModel.cancelCompose() },
				onSend = {
					scope.launch {
						if (!nameGate.ensureNamed(fragmentManager)) return@launch
						if (viewModel.send(draft.trim(), spoiler)) {
							draft = ""
							spoiler = false
						}
					}
				},
			)
		}

		SnackbarHost(
			hostState = snackbarHostState,
			modifier = Modifier.align(Alignment.BottomCenter),
		)
	}

	rejection?.let { r ->
		RejectionDialog(
			rejection = r,
			onDismiss = { viewModel.consumeRejection() },
			onDispute = { blockId ->
				viewModel.dispute(blockId)
				scope.launch {
					snackbarHostState.showSnackbar(context.getString(R.string.community_comment_filter_reported))
				}
			},
			onOpenRules = { url ->
				(context as? androidx.fragment.app.FragmentActivity)?.router
					?.openExternalBrowser(url, context.getString(R.string.community_rules))
			},
		)
	}

	pendingDelete?.let { comment ->
		AlertDialog(
			onDismissRequest = { pendingDelete = null },
			title = { Text(stringResource(R.string.community_comment_delete)) },
			text = { Text(stringResource(R.string.community_comment_delete_confirm)) },
			confirmButton = {
				TextButton(
					onClick = {
						viewModel.delete(comment)
						pendingDelete = null
					},
				) { Text(stringResource(R.string.community_comment_delete)) }
			},
			dismissButton = {
				TextButton(onClick = { pendingDelete = null }) {
					Text(stringResource(android.R.string.cancel))
				}
			},
		)
	}
}

@Composable
private fun ComposeBar(
	draft: String,
	onDraftChange: (String) -> Unit,
	spoiler: Boolean,
	onSpoilerChange: (Boolean) -> Unit,
	compose: ComposeTarget,
	helperText: String?,
	sendEnabled: Boolean,
	onCancel: () -> Unit,
	onSend: () -> Unit,
) {
	Surface(
		color = MaterialTheme.colorScheme.surfaceContainerHigh,
		shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
	) {
		Column(Modifier.padding(12.dp)) {
			val target = compose.comment
			if (target != null) {
				Row(verticalAlignment = Alignment.CenterVertically) {
					Text(
						text = stringResource(
							if (compose.isEditing) {
								R.string.community_comment_editing
							} else {
								R.string.community_comment_replying_to
							},
							target.author,
						),
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.primary,
						modifier = Modifier.weight(1f),
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
					)
					TextButton(onClick = onCancel) {
						Text(stringResource(android.R.string.cancel))
					}
				}
			}
			Row(verticalAlignment = Alignment.Bottom) {
				OutlinedTextField(
					value = draft,
					onValueChange = { if (it.length <= 4000) onDraftChange(it) },
					modifier = Modifier.weight(1f),
					placeholder = { Text(stringResource(R.string.community_comment_hint)) },
					supportingText = helperText?.let { { Text(it) } },
					shape = RoundedCornerShape(20.dp),
					minLines = 1,
					maxLines = 5,
				)
				Spacer(Modifier.width(6.dp))
				OutlinedIconButton(
					onClick = { onSpoilerChange(!spoiler) },
					modifier = Modifier.size(52.dp),
				) {
					Icon(
						imageVector = if (spoiler) Icons.Default.VisibilityOff else Icons.Default.Visibility,
						contentDescription = stringResource(R.string.community_comment_spoiler),
					)
				}
				IconButton(
					onClick = onSend,
					enabled = sendEnabled,
					modifier = Modifier.size(52.dp),
					colors = IconButtonDefaults.filledIconButtonColors(),
				) {
					Icon(
						imageVector = Icons.AutoMirrored.Filled.Send,
						contentDescription = stringResource(R.string.community_comment_send),
					)
				}
			}
		}
	}
}

@Composable
private fun CommentRow(
	comment: CommentDto,
	revealed: Boolean,
	onReveal: () -> Unit,
	onVote: (Int) -> Unit,
	onReply: () -> Unit,
	onEdit: () -> Unit,
	onDelete: () -> Unit,
) {
	val isReply = comment.depth > 0
	val container = when {
		comment.isMine -> MaterialTheme.colorScheme.secondaryContainer
		isReply -> MaterialTheme.colorScheme.surfaceContainer
		else -> MaterialTheme.colorScheme.surfaceContainerLow
	}
	val onContainer = if (comment.isMine) {
		MaterialTheme.colorScheme.onSecondaryContainer
	} else {
		MaterialTheme.colorScheme.onSurface
	}
	val shape = if (isReply) {
		RoundedCornerShape(topStart = 6.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
	} else {
		RoundedCornerShape(20.dp)
	}

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(start = if (isReply) 16.dp else 0.dp)
			.clip(shape)
			.background(container)
			.padding(horizontal = 14.dp, vertical = 10.dp),
	) {
		Row(verticalAlignment = Alignment.CenterVertically) {
			Text(
				text = if (comment.deleted) {
					stringResource(R.string.community_comment_deleted_author)
				} else {
					comment.author
				},
				style = MaterialTheme.typography.labelLarge,
				color = onContainer,
			)
			Spacer(Modifier.width(8.dp))
			Text(
				text = relativeTime(comment.createdAt),
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
			)
		}
		Spacer(Modifier.height(2.dp))
		if (comment.deleted) {
			Text(
				stringResource(R.string.community_comment_deleted),
				style = MaterialTheme.typography.bodyMedium,
				color = onContainer.copy(alpha = 0.6f),
			)
		} else {
			val hidden = comment.isSpoiler && !revealed
			Text(
				text = if (hidden) {
					stringResource(R.string.community_comment_spoiler_hidden)
				} else {
					comment.body
				},
				style = MaterialTheme.typography.bodyMedium,
				color = if (hidden) MaterialTheme.colorScheme.primary else onContainer,
				modifier = if (hidden) Modifier.clickable(onClick = onReveal) else Modifier,
			)
			Row(
				modifier = Modifier.padding(top = 4.dp),
				verticalAlignment = Alignment.CenterVertically,
			) {
				if (!comment.isMine) {
					TextButton(onClick = { onVote(1) }) {
						Icon(
							painter = painterResource(R.drawable.ic_thumb_up),
							contentDescription = stringResource(R.string.community_comment_like),
							tint = voteTint(comment.myVote > 0),
						)
						Spacer(Modifier.width(4.dp))
						Text("${comment.up}", color = voteTint(comment.myVote > 0))
					}
					TextButton(onClick = { onVote(-1) }) {
						Icon(
							painter = painterResource(R.drawable.ic_thumb_down),
							contentDescription = stringResource(R.string.community_comment_dislike),
							tint = voteTint(comment.myVote < 0),
						)
						Spacer(Modifier.width(4.dp))
						Text("${comment.down}", color = voteTint(comment.myVote < 0))
					}
				}
				TextButton(onClick = onReply) {
					Text(stringResource(R.string.community_comment_reply))
				}
				if (comment.isMine) {
					TextButton(onClick = onEdit) {
						Text(stringResource(R.string.community_comment_edit))
					}
					TextButton(onClick = onDelete) {
						Text(stringResource(R.string.community_comment_delete))
					}
				}
			}
		}
	}
}

@Composable
private fun voteTint(chosen: Boolean) = if (chosen) {
	MaterialTheme.colorScheme.primary
} else {
	MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
private fun RejectionDialog(
	rejection: CommentRejection,
	onDismiss: () -> Unit,
	onDispute: (String) -> Unit,
	onOpenRules: (String) -> Unit,
) {
	val error = rejection.error
	val message = when (error.error) {
		ApiErrorDto.FILTER_BLOCKED -> stringResource(R.string.community_comment_blocked, error.term.orEmpty())
		ApiErrorDto.TOO_SHORT -> stringResource(R.string.community_comment_too_short, error.min ?: MIN_LENGTH)
		ApiErrorDto.CHAIN_DEPTH_EXCEEDED -> stringResource(R.string.community_comment_chain_depth)
		ApiErrorDto.RATE_LIMITED -> {
			val minutes = error.retryAfterSeconds?.let { ((it + 59) / 60).toInt() }
			if (minutes != null && minutes > 0) {
				pluralStringResource(R.plurals.community_comment_rate_limited_in, minutes, minutes)
			} else {
				stringResource(R.string.community_comment_rate_limited)
			}
		}
		ApiErrorDto.BANNED -> stringResource(R.string.community_comment_banned)
		ApiErrorDto.UNAUTHORIZED -> stringResource(R.string.community_comments_unavailable)
		else -> stringResource(R.string.community_comment_failed)
	}
	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text(stringResource(R.string.community_comment_not_posted)) },
		text = { Text(message) },
		confirmButton = {
			TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.ok)) }
		},
		dismissButton = {
			Row {
				val blockId = error.blockId
				if (error.error == ApiErrorDto.FILTER_BLOCKED && blockId != null) {
					TextButton(
						onClick = {
							onDispute(blockId)
							onDismiss()
						},
					) { Text(stringResource(R.string.community_comment_filter_wrong)) }
				}
				error.rulesUrl?.let { url ->
					TextButton(
						onClick = {
							onOpenRules(url)
							onDismiss()
						},
					) { Text(stringResource(R.string.community_rules)) }
				}
			}
		},
	)
}

private fun relativeTime(iso: String): String = try {
	DateUtils.getRelativeTimeSpanString(
		Instant.parse(iso).toEpochMilli(),
		System.currentTimeMillis(),
		DateUtils.MINUTE_IN_MILLIS,
	).toString()
} catch (_: DateTimeParseException) {
	""
}
