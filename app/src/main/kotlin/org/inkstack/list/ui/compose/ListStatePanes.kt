package org.inkstack.list.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.inkstack.list.ui.model.EmptyState
import org.inkstack.list.ui.model.ErrorState

@Composable
fun EmptyStatePane(
	state: EmptyState,
	modifier: Modifier = Modifier,
	onAction: (() -> Unit)? = null,
) {
	Column(
		modifier = modifier
			.fillMaxWidth()
			.padding(32.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(8.dp),
	) {
		Text(
			text = stringResource(state.textPrimary),
			style = MaterialTheme.typography.titleMedium,
			textAlign = TextAlign.Center,
		)
		if (state.textSecondary != 0) {
			Text(
				text = stringResource(state.textSecondary),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				textAlign = TextAlign.Center,
			)
		}
		if (state.actionStringRes != 0 && onAction != null) {
			TextButton(onClick = onAction) {
				Text(stringResource(state.actionStringRes))
			}
		}
	}
}

@Composable
fun ErrorStatePane(
	state: ErrorState,
	modifier: Modifier = Modifier,
	onRetry: (() -> Unit)? = null,
	onSecondaryAction: ((Throwable) -> Unit)? = null,
) {
	Column(
		modifier = modifier
			.fillMaxWidth()
			.padding(32.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(8.dp),
	) {
		Text(
			text = state.exception.localizedMessage ?: state.exception.javaClass.simpleName,
			style = MaterialTheme.typography.bodyMedium,
			textAlign = TextAlign.Center,
		)
		if (state.canRetry && onRetry != null) {
			TextButton(onClick = onRetry) {
				Text(stringResource(state.buttonText))
			}
		}
		if (state.secondaryButtonText != 0 && onSecondaryAction != null) {
			TextButton(onClick = { onSecondaryAction(state.exception) }) {
				Text(stringResource(state.secondaryButtonText))
			}
		}
	}
}
