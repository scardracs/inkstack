package org.inkstack.core.ui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Cover content with a theme-colored title underneath (readable in light and dark).
 */
@Composable
fun CoverWithTitle(
	title: String,
	modifier: Modifier = Modifier,
	titleStyle: TextStyle = MaterialTheme.typography.labelMedium,
	cover: @Composable () -> Unit,
) {
	Column(modifier = modifier) {
		cover()
		Text(
			text = title,
			color = MaterialTheme.colorScheme.onSurface,
			style = titleStyle,
			maxLines = 2,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 6.dp, start = 2.dp, end = 2.dp),
		)
	}
}
