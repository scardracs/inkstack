package org.inkstack.download.ui.dialog

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import org.inkstack.R
import org.inkstack.core.nav.AppRouter
import org.inkstack.core.nav.router
import org.inkstack.core.prefs.DownloadFormat
import org.inkstack.core.ui.compose.ComposeAlertDialogFragment
import org.inkstack.core.util.ext.findActivity
import org.inkstack.core.util.ext.getDisplayMessage
import org.inkstack.core.util.ext.getQuantityStringSafe
import org.inkstack.core.util.ext.joinToStringWithLimit
import org.inkstack.main.ui.owners.BottomNavOwner
import org.inkstack.parsers.util.format
import org.inkstack.settings.storage.DirectoryModel

private enum class DownloadOption {
	WholeManga,
	WholeBranch,
	FirstChapters,
	UnreadChapters,
}

@AndroidEntryPoint
class DownloadDialogFragment : ComposeAlertDialogFragment() {

	private val viewModel by viewModels<DownloadDialogViewModel>()

	@Composable
	override fun DialogContent(onDismissRequest: () -> Unit) {
		DownloadDialogContent(
			viewModel = viewModel,
			onDismissRequest = onDismissRequest,
			onConfirm = { startNow, macro, format, destination ->
				router.askForDownloadOverMeteredNetwork { allowMetered ->
					viewModel.confirm(
						startNow = startNow,
						chaptersMacro = macro,
						format = format,
						destination = destination,
						allowMetered = allowMetered,
					)
				}
			},
			onScheduled = { isStarted ->
				val bundle = Bundle(1)
				bundle.putBoolean(ARG_STARTED, isStarted)
				setFragmentResult(RESULT_KEY, bundle)
				onDismissRequest()
			},
		)
	}

	companion object {

		private const val RESULT_KEY = "DOWNLOAD_STARTED"
		private const val ARG_STARTED = "started"

		fun registerCallback(
			fm: FragmentManager,
			lifecycleOwner: LifecycleOwner,
			snackbarHost: View,
		) = fm.setFragmentResultListener(RESULT_KEY, lifecycleOwner, SnackbarResultListener(snackbarHost))

		fun unregisterCallback(fm: FragmentManager) = fm.clearFragmentResultListener(RESULT_KEY)
	}

	private class SnackbarResultListener(
		private val host: View,
	) : FragmentResultListener {

		override fun onFragmentResult(requestKey: String, result: Bundle) {
			val isStarted = result.getBoolean(ARG_STARTED, true)
			val snackbar = Snackbar.make(
				host,
				if (isStarted) R.string.download_started else R.string.download_added,
				Snackbar.LENGTH_LONG,
			)
			(host.context.findActivity() as? BottomNavOwner)?.let {
				snackbar.anchorView = it.bottomNav
			}
			val router = AppRouter.from(host)
			if (router != null) {
				snackbar.setAction(R.string.details) { router.openDownloads() }
			}
			snackbar.show()
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DownloadDialogContent(
	viewModel: DownloadDialogViewModel,
	onDismissRequest: () -> Unit,
	onConfirm: (Boolean, ChaptersSelectMacro, DownloadFormat?, DirectoryModel?) -> Unit,
	onScheduled: (Boolean) -> Unit,
) {
	val context = LocalContext.current
	val isLoading by viewModel.isLoading.collectAsState()
	val isOptionsLoading by viewModel.isOptionsLoading.collectAsState()
	val options by viewModel.chaptersSelectOptions.collectAsState()
	val destinations by viewModel.availableDestinations.collectAsState()
	val defaultFormat by viewModel.defaultFormat.collectAsState()
	val formatLabels = stringArrayResource(R.array.download_formats)

	var selectedOption by rememberSaveable { mutableStateOf(DownloadOption.WholeManga) }
	var startNow by rememberSaveable { mutableStateOf(true) }
	var showMore by rememberSaveable { mutableStateOf(false) }
	var formatIndex by rememberSaveable { mutableIntStateOf(0) }
	var destinationIndex by rememberSaveable { mutableIntStateOf(0) }
	var formatExpanded by remember { mutableStateOf(false) }
	var destinationExpanded by remember { mutableStateOf(false) }
	var branchMenuExpanded by remember { mutableStateOf(false) }
	var firstCountMenuExpanded by remember { mutableStateOf(false) }
	var unreadCountMenuExpanded by remember { mutableStateOf(false) }
	var errorMessage by remember { mutableStateOf<String?>(null) }

	LaunchedEffect(defaultFormat) {
		defaultFormat?.let { formatIndex = it.ordinal }
	}
	LaunchedEffect(destinations) {
		val checked = destinations.indexOfFirst { it.isChecked }
		if (checked >= 0) destinationIndex = checked
	}
	LaunchedEffect(viewModel) {
		viewModel.onScheduled.collectLatest { event ->
			event?.consume { onScheduled(it) }
		}
	}
	LaunchedEffect(viewModel) {
		viewModel.onError.collectLatest { event ->
			event?.consume { error ->
				errorMessage = error.getDisplayMessage(context.resources)
			}
		}
	}

	val summary = remember(viewModel.manga) {
		viewModel.manga.joinToStringWithLimit(context, 120) { it.title }
	}

	AlertDialog(
		onDismissRequest = onDismissRequest,
		title = { Text(stringResource(R.string.save_manga)) },
		text = {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.verticalScroll(rememberScrollState()),
				verticalArrangement = Arrangement.spacedBy(4.dp),
			) {
				Text(
					text = summary,
					style = MaterialTheme.typography.bodyMedium,
				)
				Column(modifier = Modifier.selectableGroup()) {
					DownloadOptionRow(
						selected = selectedOption == DownloadOption.WholeManga,
						title = stringResource(R.string.download_option_whole_manga),
						subtitle = chaptersSubtitle(options.wholeManga.chaptersCount),
						onSelect = { selectedOption = DownloadOption.WholeManga },
					)
					options.wholeBranch?.let { branch ->
						DownloadOptionRow(
							selected = selectedOption == DownloadOption.WholeBranch,
							title = stringResource(
								R.string.download_option_all_chapters,
								branch.selectedBranch ?: stringResource(R.string.unknown),
							),
							subtitle = chaptersSubtitle(branch.chaptersCount),
							onSelect = { selectedOption = DownloadOption.WholeBranch },
							trailing = if (branch.branches.size > 1) {
								{
									TextButton(onClick = { branchMenuExpanded = true }) {
										Text(stringResource(R.string.more_options))
									}
									DropdownMenu(
										expanded = branchMenuExpanded,
										onDismissRequest = { branchMenuExpanded = false },
									) {
										branch.branches.keys.forEach { name ->
											DropdownMenuItem(
												text = {
													Text(name ?: stringResource(R.string.unknown))
												},
												onClick = {
													viewModel.setSelectedBranch(name)
													branchMenuExpanded = false
												},
											)
										}
									}
								}
							} else {
								null
							},
						)
					}
					options.firstChapters?.let { first ->
						DownloadOptionRow(
							selected = selectedOption == DownloadOption.FirstChapters,
							title = stringResource(
								R.string.download_option_first_n_chapters,
								chaptersSubtitle(first.chaptersCount).orEmpty(),
							),
							subtitle = first.branch,
							onSelect = { selectedOption = DownloadOption.FirstChapters },
							trailing = {
								TextButton(onClick = { firstCountMenuExpanded = true }) {
									Text(first.chaptersCount.format())
								}
								DropdownMenu(
									expanded = firstCountMenuExpanded,
									onDismissRequest = { firstCountMenuExpanded = false },
								) {
									chaptersCountSequence(first.maxAvailableCount).forEach { count ->
										DropdownMenuItem(
											text = { Text(count.format()) },
											onClick = {
												viewModel.setFirstChaptersCount(count)
												firstCountMenuExpanded = false
											},
										)
									}
								}
							},
						)
					}
					options.unreadChapters?.let { unread ->
						val title = if (unread.chaptersCount == Int.MAX_VALUE) {
							stringResource(R.string.download_option_all_unread)
						} else {
							stringResource(
								R.string.download_option_next_unread_n_chapters,
								chaptersSubtitle(unread.chaptersCount).orEmpty(),
							)
						}
						DownloadOptionRow(
							selected = selectedOption == DownloadOption.UnreadChapters,
							title = title,
							subtitle = null,
							onSelect = { selectedOption = DownloadOption.UnreadChapters },
							trailing = {
								TextButton(onClick = { unreadCountMenuExpanded = true }) {
									Text(
										if (unread.chaptersCount == Int.MAX_VALUE) {
											stringResource(R.string.chapters_all)
										} else {
											unread.chaptersCount.format()
										},
									)
								}
								DropdownMenu(
									expanded = unreadCountMenuExpanded,
									onDismissRequest = { unreadCountMenuExpanded = false },
								) {
									chaptersCountSequence(unread.maxAvailableCount).forEach { count ->
										DropdownMenuItem(
											text = { Text(count.format()) },
											onClick = {
												viewModel.setUnreadChaptersCount(count)
												unreadCountMenuExpanded = false
											},
										)
									}
									DropdownMenuItem(
										text = { Text(stringResource(R.string.chapters_all)) },
										onClick = {
											viewModel.setUnreadChaptersCount(Int.MAX_VALUE)
											unreadCountMenuExpanded = false
										},
									)
								}
							},
						)
					}
				}
				if (isOptionsLoading) {
					LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
				}
				if (viewModel.manga.size == 1) {
					Text(
						text = stringResource(R.string.chapter_selection_hint),
						style = MaterialTheme.typography.bodySmall,
						modifier = Modifier.padding(vertical = 4.dp),
					)
				}
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.clickable { startNow = !startNow }
						.padding(vertical = 8.dp),
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.SpaceBetween,
				) {
					Text(stringResource(R.string.start_download))
					Switch(checked = startNow, onCheckedChange = { startNow = it })
				}
				TextButton(onClick = { showMore = !showMore }) {
					Text(stringResource(R.string.more_options))
				}
				if (showMore) {
					Text(
						text = stringResource(R.string.destination_directory),
						style = MaterialTheme.typography.titleSmall,
					)
					ExposedDropdownMenuBox(
						expanded = destinationExpanded,
						onExpandedChange = { destinationExpanded = it },
					) {
						val dest = destinations.getOrNull(destinationIndex)
						OutlinedTextField(
							value = dest?.title
								?: dest?.titleRes?.takeIf { it != 0 }?.let { stringResource(it) }
								?: "",
							onValueChange = {},
							readOnly = true,
							modifier = Modifier
								.fillMaxWidth()
								.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
							trailingIcon = {
								ExposedDropdownMenuDefaults.TrailingIcon(expanded = destinationExpanded)
							},
						)
						ExposedDropdownMenu(
							expanded = destinationExpanded,
							onDismissRequest = { destinationExpanded = false },
						) {
							destinations.forEachIndexed { index, item ->
								DropdownMenuItem(
									text = {
										Column {
											Text(
												item.title
													?: if (item.titleRes != 0) stringResource(item.titleRes) else "",
											)
											item.file?.path?.let {
												Text(it, style = MaterialTheme.typography.bodySmall)
											}
										}
									},
									onClick = {
										destinationIndex = index
										destinationExpanded = false
									},
								)
							}
						}
					}
					Text(
						text = stringResource(R.string.preferred_download_format),
						style = MaterialTheme.typography.titleSmall,
						modifier = Modifier.padding(top = 8.dp),
					)
					ExposedDropdownMenuBox(
						expanded = formatExpanded,
						onExpandedChange = { formatExpanded = it },
					) {
						OutlinedTextField(
							value = formatLabels.getOrElse(formatIndex) { "" },
							onValueChange = {},
							readOnly = true,
							modifier = Modifier
								.fillMaxWidth()
								.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
							trailingIcon = {
								ExposedDropdownMenuDefaults.TrailingIcon(expanded = formatExpanded)
							},
						)
						ExposedDropdownMenu(
							expanded = formatExpanded,
							onDismissRequest = { formatExpanded = false },
						) {
							formatLabels.forEachIndexed { index, label ->
								DropdownMenuItem(
									text = { Text(label) },
									onClick = {
										formatIndex = index
										formatExpanded = false
									},
								)
							}
						}
					}
				}
			}
		},
		confirmButton = {
			TextButton(
				onClick = {
					val macro = when (selectedOption) {
						DownloadOption.WholeManga -> options.wholeManga
						DownloadOption.WholeBranch -> options.wholeBranch ?: return@TextButton
						DownloadOption.FirstChapters -> options.firstChapters ?: return@TextButton
						DownloadOption.UnreadChapters -> options.unreadChapters ?: return@TextButton
					}
					onConfirm(
						startNow,
						macro,
						DownloadFormat.entries.getOrNull(formatIndex),
						destinations.getOrNull(destinationIndex),
					)
				},
				enabled = !isLoading,
			) {
				Text(stringResource(R.string.save))
			}
		},
		dismissButton = {
			TextButton(onClick = onDismissRequest) {
				Text(stringResource(android.R.string.cancel))
			}
		},
	)

	errorMessage?.let { msg ->
		AlertDialog(
			onDismissRequest = {
				errorMessage = null
				onDismissRequest()
			},
			title = { Text(stringResource(R.string.error)) },
			text = { Text(msg) },
			confirmButton = {
				TextButton(
					onClick = {
						errorMessage = null
						onDismissRequest()
					},
				) {
					Text(stringResource(R.string.close))
				}
			},
		)
	}
}

@Composable
private fun DownloadOptionRow(
	selected: Boolean,
	title: String,
	subtitle: String?,
	onSelect: () -> Unit,
	trailing: (@Composable () -> Unit)? = null,
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.selectable(
				selected = selected,
				onClick = onSelect,
				role = Role.RadioButton,
			)
			.padding(vertical = 6.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		RadioButton(selected = selected, onClick = null)
		Column(
			modifier = Modifier
				.weight(1f)
				.padding(horizontal = 8.dp),
		) {
			Text(title, style = MaterialTheme.typography.bodyLarge)
			if (!subtitle.isNullOrEmpty()) {
				Text(subtitle, style = MaterialTheme.typography.bodySmall)
			}
		}
		trailing?.invoke()
	}
	HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
}

@Composable
private fun chaptersSubtitle(count: Int): String? {
	val resources = LocalContext.current.resources
	return if (count > 0) {
		resources.getQuantityStringSafe(R.plurals.chapters, count, count)
	} else {
		null
	}
}

private fun chaptersCountSequence(max: Int) = sequence {
	yield(1)
	var seed = 5
	var step = 5
	while (seed + step <= max) {
		yield(seed)
		step = when {
			seed < 20 -> 5
			seed < 60 -> 10
			else -> 20
		}
		seed += step
	}
	if (seed < max) {
		yield(max)
	}
}
