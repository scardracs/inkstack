package org.inkstack.details.ui.scrobbling

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import org.inkstack.R
import org.inkstack.core.nav.AppRouter
import org.inkstack.core.nav.router
import org.inkstack.core.ui.compose.MangaCover
import org.inkstack.core.ui.compose.sheet.ComposeAdaptiveSheet
import org.inkstack.core.util.ext.getDisplayMessage
import org.inkstack.core.util.ext.sanitize
import org.inkstack.details.ui.DetailsViewModel
import org.inkstack.scrobbling.common.domain.model.ScrobblingStatus

@AndroidEntryPoint
class ScrobblingInfoSheet : ComposeAdaptiveSheet() {

	private val viewModel by activityViewModels<DetailsViewModel>()
	private var scrobblerIndex: Int = -1

	override fun onCreate(savedInstanceState: android.os.Bundle?) {
		super.onCreate(savedInstanceState)
		scrobblerIndex = requireArguments().getInt(AppRouter.KEY_INDEX, scrobblerIndex)
	}

	@Composable
	override fun sheetTitle(): String = stringResource(R.string.tracking)

	@OptIn(ExperimentalMaterial3Api::class)
	@Composable
	override fun SheetContent(onDismissRequest: () -> Unit) {
		val context = LocalContext.current
		val scrobblings by viewModel.scrobblingInfo.collectAsState()
		val scrobbling = scrobblings.getOrNull(scrobblerIndex)
		val statuses = stringArrayResource(R.array.scrobbling_statuses)
		var menuExpanded by remember { mutableStateOf(false) }
		var statusExpanded by remember { mutableStateOf(false) }

		LaunchedEffect(viewModel) {
			viewModel.onError.collectLatest { event ->
				event?.consume { error ->
					Toast.makeText(context, error.getDisplayMessage(context.resources), Toast.LENGTH_SHORT).show()
				}
			}
		}
		LaunchedEffect(scrobbling) {
			if (scrobbling == null) onDismissRequest()
		}
		if (scrobbling == null) return

		Column(
			modifier = Modifier
				.fillMaxWidth()
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 16.dp, vertical = 8.dp),
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.Top,
			) {
				MangaCover(
					model = scrobbling.coverUrl,
					contentDescription = scrobbling.title,
					modifier = Modifier
						.width(72.dp)
						.clickable {
							router.openImage(
								url = scrobbling.coverUrl ?: return@clickable,
								source = null,
								anchor = null,
							)
						},
				)
				Column(
					modifier = Modifier
						.weight(1f)
						.padding(start = 12.dp),
				) {
					Row(verticalAlignment = Alignment.CenterVertically) {
						Icon(
							painter = painterResource(scrobbling.scrobbler.iconResId),
							contentDescription = stringResource(scrobbling.scrobbler.titleResId),
							modifier = Modifier.size(20.dp),
						)
						Text(
							text = scrobbling.title.orEmpty(),
							style = MaterialTheme.typography.titleMedium,
							modifier = Modifier
								.weight(1f)
								.padding(start = 8.dp),
						)
						Box {
							IconButton(onClick = { menuExpanded = true }) {
								Icon(
									imageVector = Icons.Filled.MoreVert,
									contentDescription = stringResource(R.string.more_options),
								)
							}
							DropdownMenu(
								expanded = menuExpanded,
								onDismissRequest = { menuExpanded = false },
							) {
								DropdownMenuItem(
									text = { Text(stringResource(R.string.open_in_browser)) },
									onClick = {
										menuExpanded = false
										val url = scrobbling.externalUrl ?: return@DropdownMenuItem
										if (!router.openExternalBrowser(url, context.getString(R.string.open_in_browser))) {
											Snackbar.make(requireView(), R.string.operation_not_supported, Snackbar.LENGTH_SHORT).show()
										}
									},
								)
								DropdownMenuItem(
									text = { Text(stringResource(R.string.edit)) },
									onClick = {
										menuExpanded = false
										val manga = viewModel.manga.value ?: return@DropdownMenuItem
										activity?.router?.showScrobblingSelectorSheet(manga, scrobbling.scrobbler)
										onDismissRequest()
									},
								)
								DropdownMenuItem(
									text = { Text(stringResource(R.string.remove)) },
									onClick = {
										menuExpanded = false
										viewModel.unregisterScrobbling(scrobblerIndex)
										onDismissRequest()
									},
								)
							}
						}
					}
					ExposedDropdownMenuBox(
						expanded = statusExpanded,
						onExpandedChange = { statusExpanded = it },
						modifier = Modifier.padding(top = 8.dp),
					) {
						val statusIndex = scrobbling.status?.ordinal ?: -1
						OutlinedTextField(
							value = statuses.getOrNull(statusIndex).orEmpty(),
							onValueChange = {},
							readOnly = true,
							modifier = Modifier
								.fillMaxWidth()
								.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
							trailingIcon = {
								ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded)
							},
						)
						ExposedDropdownMenu(
							expanded = statusExpanded,
							onDismissRequest = { statusExpanded = false },
						) {
							statuses.forEachIndexed { index, label ->
								DropdownMenuItem(
									text = { Text(label) },
									onClick = {
										statusExpanded = false
										viewModel.updateScrobbling(
											index = scrobblerIndex,
											rating = scrobbling.rating,
											status = ScrobblingStatus.entries.getOrNull(index),
										)
									},
								)
							}
						}
					}
					StarRatingRow(
						rating = scrobbling.rating,
						onRatingChange = { rating ->
							viewModel.updateScrobbling(
								index = scrobblerIndex,
								rating = rating,
								status = scrobbling.status,
							)
						},
						modifier = Modifier.padding(top = 8.dp),
					)
				}
			}
			scrobbling.description?.sanitize()?.toString()?.let { description ->
				Text(
					text = description,
					style = MaterialTheme.typography.bodyMedium,
					modifier = Modifier.padding(top = 12.dp),
				)
			}
		}
	}
}

@Composable
private fun StarRatingRow(
	rating: Float,
	onRatingChange: (Float) -> Unit,
	modifier: Modifier = Modifier,
) {
	val stars = 5
	Row(modifier = modifier) {
		repeat(stars) { index ->
			val filled = rating * stars > index
			Icon(
				painter = painterResource(
					if (filled) R.drawable.ic_star_rate else R.drawable.ic_star_rate_outline,
				),
				contentDescription = null,
				modifier = Modifier
					.size(28.dp)
					.clickable { onRatingChange((index + 1f) / stars) }
					.padding(2.dp),
				tint = MaterialTheme.colorScheme.primary,
			)
		}
	}
}
