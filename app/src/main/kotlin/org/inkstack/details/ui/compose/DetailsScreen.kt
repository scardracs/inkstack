package org.inkstack.details.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.ImageLoader
import coil3.request.ImageRequest
import org.inkstack.R
import org.inkstack.core.model.getTitle
import org.inkstack.core.model.titleResId
import org.inkstack.core.ui.compose.MangaCover
import org.inkstack.core.util.ext.mangaExtra
import org.inkstack.details.ui.DetailsViewModel
import org.inkstack.details.ui.model.ChapterListItem
import org.inkstack.details.ui.model.HistoryInfo
import org.inkstack.details.ui.model.MangaBranch
import org.inkstack.parsers.model.ContentRating
import org.inkstack.parsers.model.Manga
import org.inkstack.parsers.model.MangaChapter
import org.inkstack.parsers.util.ifNullOrEmpty

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DetailsScreen(
	imageLoader: ImageLoader,
	onBack: () -> Unit,
	onRead: (Manga, MangaChapter?, incognito: Boolean) -> Unit,
	onChapterClick: (MangaChapter) -> Unit,
	onFavorite: (Manga) -> Unit = {},
	onTagClick: (org.inkstack.parsers.model.MangaTag) -> Unit = {},
	onSourceClick: (Manga) -> Unit = {},
	menuVisibility: DetailsMenuVisibility = DetailsMenuVisibility(),
	readMenuVisibility: DetailsReadMenuVisibility = DetailsReadMenuVisibility(),
	historyInfo: HistoryInfo? = null,
	onShare: () -> Unit = {},
	onDownload: () -> Unit = {},
	onDelete: () -> Unit = {},
	onEditOverride: () -> Unit = {},
	onScrobbling: () -> Unit = {},
	onStats: () -> Unit = {},
	onFindSimilar: () -> Unit = {},
	onAlternatives: () -> Unit = {},
	onReplaceSource: () -> Unit = {},
	onOnline: () -> Unit = {},
	onBrowser: () -> Unit = {},
	onShortcut: () -> Unit = {},
	onChaptersSheet: () -> Unit = {},
	onForgetHistory: () -> Unit = {},
	viewModel: DetailsViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val resources = LocalResources.current
	val details by viewModel.mangaDetails.collectAsState()
	val chapters by viewModel.chapters.collectAsState()
	val branches by viewModel.branches.collectAsState()
	val favourites by viewModel.favouriteCategories.collectAsState()
	val coverUrl by viewModel.coverUrl.collectAsState()
	val isLoading by viewModel.isLoading.collectAsState()
	val emptyReason by viewModel.emptyReason.collectAsState()
	val manga = details?.toManga()
	val description = details?.description?.toString().ifNullOrEmpty { manga?.description }
	var overflowExpanded by remember { mutableStateOf(false) }
	var readMenuExpanded by remember { mutableStateOf(false) }
	val fabLabel = when {
		historyInfo?.canContinue == true -> stringResource(R.string._continue)
		else -> stringResource(R.string.read)
	}

	Scaffold(
		modifier = Modifier.fillMaxSize(),
		topBar = {
			TopAppBar(
				title = {
					Text(
						text = manga?.title.orEmpty(),
						maxLines = 1,
						overflow = TextOverflow.Ellipsis,
					)
				},
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
					}
				},
				actions = {
					IconButton(onClick = { overflowExpanded = true }) {
						Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more))
					}
					DropdownMenu(
						expanded = overflowExpanded,
						onDismissRequest = { overflowExpanded = false },
					) {
						if (menuVisibility.share) {
							DropdownMenuItem(
								text = { Text(stringResource(R.string.share)) },
								onClick = { overflowExpanded = false; onShare() },
							)
						}
						if (menuVisibility.download) {
							DropdownMenuItem(
								text = { Text(stringResource(R.string.download)) },
								onClick = { overflowExpanded = false; onDownload() },
							)
						}
						if (menuVisibility.delete) {
							DropdownMenuItem(
								text = { Text(stringResource(R.string.delete)) },
								onClick = { overflowExpanded = false; onDelete() },
							)
						}
						if (menuVisibility.editOverride) {
							DropdownMenuItem(
								text = { Text(stringResource(R.string.edit)) },
								onClick = { overflowExpanded = false; onEditOverride() },
							)
						}
						if (menuVisibility.scrobbling) {
							DropdownMenuItem(
								text = { Text(stringResource(R.string.tracking)) },
								onClick = { overflowExpanded = false; onScrobbling() },
							)
						}
						if (menuVisibility.stats) {
							DropdownMenuItem(
								text = { Text(stringResource(R.string.statistics)) },
								onClick = { overflowExpanded = false; onStats() },
							)
						}
						if (menuVisibility.findSimilar) {
							DropdownMenuItem(
								text = { Text(stringResource(R.string.find_similar)) },
								onClick = { overflowExpanded = false; onFindSimilar() },
							)
						}
						if (menuVisibility.alternatives) {
							DropdownMenuItem(
								text = { Text(stringResource(R.string.alternatives)) },
								onClick = { overflowExpanded = false; onAlternatives() },
							)
						}
						if (menuVisibility.replaceSource) {
							DropdownMenuItem(
								text = { Text(stringResource(R.string.replace_source)) },
								onClick = { overflowExpanded = false; onReplaceSource() },
							)
						}
						if (menuVisibility.online) {
							DropdownMenuItem(
								text = { Text(stringResource(R.string.online_variant)) },
								onClick = { overflowExpanded = false; onOnline() },
							)
						}
						if (menuVisibility.browser) {
							DropdownMenuItem(
								text = { Text(stringResource(R.string.open_in_browser)) },
								onClick = { overflowExpanded = false; onBrowser() },
							)
						}
						if (menuVisibility.shortcut) {
							DropdownMenuItem(
								text = { Text(stringResource(R.string.create_shortcut)) },
								onClick = { overflowExpanded = false; onShortcut() },
							)
						}
						if (menuVisibility.chaptersSheet) {
							DropdownMenuItem(
								text = { Text(stringResource(R.string.chapters)) },
								onClick = { overflowExpanded = false; onChaptersSheet() },
							)
						}
					}
				},
				colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
			)
		},
		floatingActionButton = {
			if (manga != null) {
				Box {
					FloatingActionButton(
						onClick = {},
						modifier = Modifier.combinedClickable(
							interactionSource = remember { MutableInteractionSource() },
							indication = null,
							onClick = { onRead(manga, null, false) },
							onLongClick = { readMenuExpanded = true },
						),
					) {
						Icon(Icons.Default.PlayArrow, contentDescription = fabLabel)
					}
					DropdownMenu(
						expanded = readMenuExpanded,
						onDismissRequest = { readMenuExpanded = false },
					) {
						if (readMenuVisibility.incognito) {
							DropdownMenuItem(
								text = { Text(stringResource(R.string.incognito_mode)) },
								onClick = {
									readMenuExpanded = false
									onRead(manga, null, true)
								},
							)
						}
						if (readMenuVisibility.forget) {
							DropdownMenuItem(
								text = { Text(stringResource(R.string.remove_from_history)) },
								onClick = {
									readMenuExpanded = false
									onForgetHistory()
								},
							)
						}
						if (readMenuVisibility.download) {
							DropdownMenuItem(
								text = { Text(stringResource(R.string.download)) },
								onClick = {
									readMenuExpanded = false
									onDownload()
								},
							)
						}
					}
				}
			}
		},
	) { padding ->
		PullToRefreshBox(
			isRefreshing = isLoading,
			onRefresh = viewModel::reload,
			modifier = Modifier
				.fillMaxSize()
				.padding(padding),
		) {
			when {
				manga == null && isLoading -> {
					Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
						CircularProgressIndicator()
					}
				}

				manga == null -> {
					Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
						Text(stringResource(R.string.error_occurred))
					}
				}

				else -> {
					LazyColumn(
						modifier = Modifier.fillMaxSize(),
						contentPadding = PaddingValues(bottom = 96.dp),
					) {
						item(key = "header") {
							Column(
								modifier = Modifier
									.fillMaxWidth()
									.padding(16.dp),
							) {
								Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
									val request = ImageRequest.Builder(context)
										.data(coverUrl ?: manga.coverUrl)
										.mangaExtra(manga)
										.build()
									Box(modifier = Modifier.width(120.dp)) {
										MangaCover(
											model = request,
											contentDescription = manga.title,
											imageLoader = imageLoader,
											modifier = Modifier.fillMaxWidth(),
										)
										val badge = when (manga.contentRating) {
											ContentRating.SUGGESTIVE -> stringResource(R.string.nsfw_16)
											ContentRating.ADULT -> stringResource(R.string.nsfw)
											else -> null
										}
										if (badge != null) {
											Text(
												text = badge,
												style = MaterialTheme.typography.labelSmall,
												color = Color.White,
												modifier = Modifier
													.align(Alignment.BottomEnd)
													.padding(6.dp)
													.clip(RoundedCornerShape(4.dp))
													.background(MaterialTheme.colorScheme.error)
													.padding(horizontal = 6.dp, vertical = 2.dp),
											)
										}
									}
									Column(modifier = Modifier.weight(1f)) {
										Text(
											text = manga.title,
											style = MaterialTheme.typography.headlineSmall,
											fontWeight = FontWeight.Bold,
											maxLines = 3,
											overflow = TextOverflow.Ellipsis,
										)
										Spacer(Modifier.height(8.dp))
										SuggestionChip(
											onClick = { onFavorite(manga) },
											label = {
												Text(
													text = if (favourites.isEmpty()) {
														stringResource(R.string.add_to_favourites)
													} else {
														favourites.joinToString { it.title }
													},
													maxLines = 1,
													overflow = TextOverflow.Ellipsis,
												)
											},
											icon = {
												Icon(
													imageVector = if (favourites.isEmpty()) {
														Icons.Outlined.FavoriteBorder
													} else {
														Icons.Outlined.Favorite
													},
													contentDescription = null,
													modifier = Modifier.size(18.dp),
												)
											},
										)
										manga.state?.let { state ->
											Spacer(Modifier.height(8.dp))
											Text(
												text = stringResource(state.titleResId),
												style = MaterialTheme.typography.bodyMedium,
												color = MaterialTheme.colorScheme.onSurfaceVariant,
											)
										}
										Spacer(Modifier.height(4.dp))
										Text(
											text = manga.source.getTitle(context),
											style = MaterialTheme.typography.bodyMedium,
											color = MaterialTheme.colorScheme.primary,
											modifier = Modifier.clickable { onSourceClick(manga) },
										)
									}
								}

								if (!description.isNullOrBlank()) {
									Spacer(Modifier.height(16.dp))
									Text(
										text = description,
										style = MaterialTheme.typography.bodyMedium,
										color = MaterialTheme.colorScheme.onSurfaceVariant,
										maxLines = 8,
										overflow = TextOverflow.Ellipsis,
									)
								}

								val tags = manga.tags
								if (tags.isNotEmpty()) {
									Spacer(Modifier.height(12.dp))
									Row(
										modifier = Modifier.horizontalScroll(rememberScrollState()),
										horizontalArrangement = Arrangement.spacedBy(8.dp),
									) {
										tags.forEach { tag ->
											SuggestionChip(
												onClick = { onTagClick(tag) },
												label = { Text(tag.title) },
											)
										}
									}
								}

								if (branches.size > 1) {
									Spacer(Modifier.height(12.dp))
									BranchRow(
										branches = branches,
										onSelect = viewModel::setSelectedBranch,
									)
								}

								Spacer(Modifier.height(16.dp))
								Text(
									text = stringResource(R.string.chapters),
									style = MaterialTheme.typography.titleMedium,
								)
							}
						}

						when {
							chapters.isEmpty() && emptyReason != null -> {
								item(key = "empty") {
									Text(
										text = stringResource(emptyReason!!.msgResId),
										style = MaterialTheme.typography.bodyMedium,
										color = MaterialTheme.colorScheme.onSurfaceVariant,
										modifier = Modifier.padding(16.dp),
									)
								}
							}

							chapters.isEmpty() && isLoading -> {
								item(key = "loading") {
									Box(
										Modifier
											.fillMaxWidth()
											.padding(32.dp),
										contentAlignment = Alignment.Center,
									) {
										CircularProgressIndicator()
									}
								}
							}

							else -> {
								items(chapters, key = { it.chapter.id }) { item ->
									ChapterRow(
										item = item,
										title = item.getTitle(resources),
										onClick = { onChapterClick(item.chapter) },
									)
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
private fun BranchRow(
	branches: List<MangaBranch>,
	onSelect: (String?) -> Unit,
) {
	Row(
		modifier = Modifier.horizontalScroll(rememberScrollState()),
		horizontalArrangement = Arrangement.spacedBy(8.dp),
	) {
		branches.forEach { branch ->
			FilterChip(
				selected = branch.isSelected,
				onClick = { onSelect(branch.name) },
				label = {
					Text(
						buildString {
							append(branch.name ?: stringResource(R.string.system_default))
							append(" · ")
							append(branch.count)
						},
					)
				},
			)
		}
	}
}

@Composable
private fun ChapterRow(
	item: ChapterListItem,
	title: String,
	onClick: () -> Unit,
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onClick)
			.background(
				if (item.isCurrent) {
					MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
				} else {
					Color.Transparent
				},
			)
			.padding(horizontal = 16.dp, vertical = 12.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(8.dp),
	) {
		Column(modifier = Modifier.weight(1f)) {
			Text(
				text = title,
				style = MaterialTheme.typography.bodyLarge,
				fontWeight = if (item.isUnread || item.isCurrent) FontWeight.SemiBold else FontWeight.Normal,
				color = if (item.isUnread || item.isCurrent) {
					MaterialTheme.colorScheme.onSurface
				} else {
					MaterialTheme.colorScheme.onSurfaceVariant
				},
				maxLines = 2,
				overflow = TextOverflow.Ellipsis,
			)
			item.description?.let {
				Text(
					text = it,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
				)
			}
		}
		if (item.isUnread && item.isNew) {
			Icon(
				imageVector = Icons.Default.NewReleases,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.primary,
				modifier = Modifier.size(18.dp),
			)
		}
		if (item.isBookmarked) {
			Icon(
				imageVector = Icons.Default.Bookmark,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.secondary,
				modifier = Modifier.size(18.dp),
			)
		}
		if (item.isDownloaded) {
			Icon(
				imageVector = Icons.Default.Download,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onSurfaceVariant,
				modifier = Modifier.size(18.dp),
			)
		}
	}
}
