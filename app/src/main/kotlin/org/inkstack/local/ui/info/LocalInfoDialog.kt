package org.inkstack.local.ui.info

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import org.inkstack.R
import org.inkstack.core.ui.compose.ComposeAlertDialogFragment
import org.inkstack.core.util.FileSize
import org.inkstack.core.util.ext.getQuantityStringSafe

@AndroidEntryPoint
class LocalInfoDialog : ComposeAlertDialogFragment() {

	private val viewModel: LocalInfoViewModel by viewModels()

	@Composable
	override fun DialogContent(onDismissRequest: () -> Unit) {
		LocalInfoDialogContent(
			viewModel = viewModel,
			onDismissRequest = onDismissRequest,
		)
	}
}

@Composable
private fun LocalInfoDialogContent(
	viewModel: LocalInfoViewModel,
	onDismissRequest: () -> Unit,
) {
	val context = LocalContext.current
	val path by viewModel.path.collectAsState()
	val size by viewModel.size.collectAsState()
	val available by viewModel.availableSize.collectAsState()
	val isCleaningUp by viewModel.isCleaningUp.collectAsState()

	LaunchedEffect(viewModel) {
		viewModel.onCleanedUp.collectLatest { event ->
			event?.consume { result ->
				val text = if (result.first == 0 && result.second == 0L) {
					context.getString(R.string.no_chapters_deleted)
				} else {
					context.getString(
						R.string.chapters_deleted_pattern,
						context.resources.getQuantityStringSafe(R.plurals.chapters, result.first, result.first),
						FileSize.BYTES.format(context, result.second),
					)
				}
				Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
			}
		}
	}

	val fraction = if (size >= 0 && available >= 0 && size + available > 0) {
		(size.toDouble() / (size + available).toDouble()).toFloat()
	} else {
		0f
	}

	AlertDialog(
		onDismissRequest = { if (!isCleaningUp) onDismissRequest() },
		title = { Text(stringResource(R.string.saved_manga)) },
		text = {
			Column(modifier = Modifier.fillMaxWidth()) {
				Text(
					text = stringResource(R.string.location),
					style = MaterialTheme.typography.labelMedium,
				)
				Text(
					text = path.orEmpty(),
					style = MaterialTheme.typography.bodyMedium,
					modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
				)
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(18.dp)
						.clip(RoundedCornerShape(4.dp))
						.background(MaterialTheme.colorScheme.secondaryContainer),
				) {
					if (fraction > 0f) {
						Box(
							modifier = Modifier
								.fillMaxHeight()
								.fillMaxWidth(fraction.coerceIn(0f, 1f))
								.background(MaterialTheme.colorScheme.primary),
						)
					}
				}
				if (size >= 0 && available >= 0) {
					LegendRow(
						color = MaterialTheme.colorScheme.primary,
						label = stringResource(
							R.string.memory_usage_pattern,
							stringResource(R.string.this_manga),
							FileSize.BYTES.format(context, size),
						),
					)
					LegendRow(
						color = MaterialTheme.colorScheme.secondaryContainer,
						label = stringResource(
							R.string.memory_usage_pattern,
							stringResource(R.string.available),
							FileSize.BYTES.format(context, available),
						),
					)
				}
				FilterChip(
					selected = false,
					onClick = { if (!isCleaningUp) viewModel.cleanup() },
					enabled = !isCleaningUp,
					label = { Text(stringResource(R.string.delete_read_chapters)) },
					leadingIcon = {
						if (isCleaningUp) {
							CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
						} else {
							Icon(
								painter = painterResource(R.drawable.ic_delete),
								contentDescription = null,
								modifier = Modifier.size(18.dp),
							)
						}
					},
					modifier = Modifier.padding(top = 12.dp),
				)
			}
		},
		confirmButton = {
			TextButton(
				onClick = onDismissRequest,
				enabled = !isCleaningUp,
			) {
				Text(stringResource(R.string.close))
			}
		},
	)
}

@Composable
private fun LegendRow(
	color: androidx.compose.ui.graphics.Color,
	label: String,
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(top = 8.dp),
		verticalAlignment = Alignment.CenterVertically,
	) {
		Box(
			modifier = Modifier
				.size(12.dp)
				.clip(RoundedCornerShape(2.dp))
				.background(color),
		)
		Text(
			text = label,
			style = MaterialTheme.typography.bodySmall,
			modifier = Modifier.padding(start = 8.dp),
		)
	}
}
