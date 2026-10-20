package org.inkstack.settings.reader.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.inkstack.R
import org.inkstack.core.ui.compose.InkstackScaffold
import org.inkstack.reader.domain.TapGridArea
import org.inkstack.reader.ui.tapgrid.TapAction
import org.inkstack.settings.reader.ReaderTapGridConfigViewModel

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ReaderTapGridConfigScreen(
	onBack: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: ReaderTapGridConfigViewModel = hiltViewModel(),
) {
	val content by viewModel.content.collectAsState()
	var menuOpen by remember { mutableStateOf(false) }
	var confirmReset by remember { mutableStateOf(false) }
	var selector by remember { mutableStateOf<Pair<TapGridArea, Boolean>?>(null) }

	val rows = listOf(
		listOf(TapGridArea.TOP_LEFT, TapGridArea.TOP_CENTER, TapGridArea.TOP_RIGHT),
		listOf(TapGridArea.CENTER_LEFT, TapGridArea.CENTER, TapGridArea.CENTER_RIGHT),
		listOf(TapGridArea.BOTTOM_LEFT, TapGridArea.BOTTOM_CENTER, TapGridArea.BOTTOM_RIGHT),
	)

	InkstackScaffold(
		modifier = modifier,
		topBar = {
			TopAppBar(
				title = { Text(stringResource(R.string.reader_actions)) },
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
					}
				},
				actions = {
					IconButton(onClick = { menuOpen = true }) {
						Icon(Icons.Default.MoreVert, contentDescription = null)
					}
					DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
						DropdownMenuItem(
							text = { Text(stringResource(R.string.reset)) },
							onClick = {
								menuOpen = false
								confirmReset = true
							},
						)
						DropdownMenuItem(
							text = { Text(stringResource(R.string.disable_all)) },
							onClick = {
								viewModel.disableAll()
								menuOpen = false
							},
						)
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
			rows.forEach { row ->
				Row(
					modifier = Modifier
						.weight(1f)
						.fillMaxWidth(),
				) {
					row.forEach { area ->
						val actions = content[area]
						val bg = actions?.tapAction?.color?.let { Color(it).copy(alpha = 0.16f) }
							?: Color.Transparent
						Box(
							modifier = Modifier
								.weight(1f)
								.fillMaxSize()
								.background(bg)
								.combinedClickable(
									onClick = { selector = area to false },
									onLongClick = { selector = area to true },
								)
								.padding(8.dp),
							contentAlignment = Alignment.Center,
						) {
							Column(horizontalAlignment = Alignment.CenterHorizontally) {
								Text(stringResource(R.string.tap_action), textAlign = TextAlign.Center)
								Text(
									actions?.tapAction?.let { stringResource(it.nameStringResId) }
										?: stringResource(R.string.none),
									fontWeight = FontWeight.Bold,
									textAlign = TextAlign.Center,
								)
								Text(stringResource(R.string.long_tap_action), textAlign = TextAlign.Center)
								Text(
									actions?.longTapAction?.let { stringResource(it.nameStringResId) }
										?: stringResource(R.string.none),
									fontWeight = FontWeight.Bold,
									textAlign = TextAlign.Center,
								)
							}
						}
					}
				}
			}
		}
	}

	selector?.let { (area, isLong) ->
		val selected = content[area]?.let { if (isLong) it.longTapAction else it.tapAction }
		val names = listOf(stringResource(R.string.none)) +
			TapAction.entries.map { stringResource(it.nameStringResId) }
		AlertDialog(
			onDismissRequest = { selector = null },
			title = {
				Text(stringResource(if (isLong) R.string.long_tap_action else R.string.tap_action))
			},
			text = {
				Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
					names.forEachIndexed { index, name ->
						val action = TapAction.entries.getOrNull(index - 1)
						val checked = action == selected || (index == 0 && selected == null)
						TextButton(onClick = {
							viewModel.setTapAction(area, isLong, action)
							selector = null
						}) {
							Text(
								name,
								fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal,
								color = if (checked) MaterialTheme.colorScheme.primary
								else MaterialTheme.colorScheme.onSurface,
							)
						}
					}
				}
			},
			confirmButton = {
				TextButton(onClick = { selector = null }) {
					Text(stringResource(android.R.string.cancel))
				}
			},
		)
	}

	if (confirmReset) {
		AlertDialog(
			onDismissRequest = { confirmReset = false },
			title = { Text(stringResource(R.string.reader_actions)) },
			text = { Text(stringResource(R.string.config_reset_confirm)) },
			confirmButton = {
				TextButton(onClick = {
					viewModel.reset()
					confirmReset = false
				}) { Text(stringResource(R.string.reset)) }
			},
			dismissButton = {
				TextButton(onClick = { confirmReset = false }) {
					Text(stringResource(android.R.string.cancel))
				}
			},
		)
	}
}
