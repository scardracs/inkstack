package org.inkstack.core.ui.compose.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.inkstack.R
import org.inkstack.core.ui.compose.InkstackGlassShape
import org.inkstack.core.ui.compose.InkstackGlassSurface
import org.inkstack.core.ui.compose.inkstackGlassTint

/**
 * Phone vs tablet presentation for Compose sheets.
 * Mirrors [org.inkstack.core.ui.sheet.BaseAdaptiveSheet]: bottom on phone, side on tablet
 * (`R.bool.is_tablet`, currently w600dp-land).
 */
enum class AdaptiveSheetPlacement {
	Bottom,
	Side,
}

@Composable
fun rememberAdaptiveSheetPlacement(): AdaptiveSheetPlacement {
	val isTablet = booleanResource(R.bool.is_tablet)
	return remember(isTablet) {
		if (isTablet) AdaptiveSheetPlacement.Side else AdaptiveSheetPlacement.Bottom
	}
}

/**
 * Shared sheet chrome: frosted glass surface, drag handle on phone, title + close on tablet.
 * Content slot is a [ColumnScope] so callers can scroll or fill as needed.
 */
@Composable
fun InkstackAdaptiveSheet(
	title: String,
	onDismissRequest: () -> Unit,
	modifier: Modifier = Modifier,
	placement: AdaptiveSheetPlacement = rememberAdaptiveSheetPlacement(),
	showHeader: Boolean = true,
	content: @Composable ColumnScope.() -> Unit,
) {
	val shape = when (placement) {
		AdaptiveSheetPlacement.Bottom -> RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
		AdaptiveSheetPlacement.Side -> RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp)
	}
	// Edge-flush sheet: no glass stroke/shadow (they read as side hairlines on the scrim).
	InkstackGlassSurface(
		modifier = modifier.fillMaxWidth(),
		shape = shape,
		elevation = 0.dp,
		borderWidth = 0.dp,
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.windowInsetsPadding(WindowInsets.navigationBars),
		) {
			if (showHeader) {
				InkstackAdaptiveSheetHeader(
					title = title,
					placement = placement,
					onDismissRequest = onDismissRequest,
				)
			}
			content()
		}
	}
}

/**
 * Header parity with [org.inkstack.core.ui.sheet.AdaptiveSheetHeaderBar]:
 * drag handle on bottom sheets; title row + close on side sheets.
 */
@Composable
fun InkstackAdaptiveSheetHeader(
	title: String,
	onDismissRequest: () -> Unit,
	modifier: Modifier = Modifier,
	placement: AdaptiveSheetPlacement = rememberAdaptiveSheetPlacement(),
) {
	when (placement) {
		AdaptiveSheetPlacement.Bottom -> {
			Column(
				modifier = modifier.fillMaxWidth(),
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				Box(
					modifier = Modifier
						.padding(top = 10.dp, bottom = 6.dp)
						.size(width = 32.dp, height = 4.dp)
						.semantics { contentDescription = title },
				) {
					Surface(
						modifier = Modifier.matchParentSize(),
						shape = InkstackGlassShape,
						color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.40f),
						content = {},
					)
				}
				Text(
					text = title,
					style = MaterialTheme.typography.titleMedium,
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 20.dp, vertical = 4.dp),
				)
				HorizontalDivider(
					color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
				)
			}
		}

		AdaptiveSheetPlacement.Side -> {
			Row(
				modifier = modifier
					.fillMaxWidth()
					.padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.SpaceBetween,
			) {
				Text(
					text = title,
					style = MaterialTheme.typography.titleLarge,
					modifier = Modifier.weight(1f),
				)
				IconButton(onClick = onDismissRequest) {
					Icon(
						imageVector = Icons.Filled.Close,
						contentDescription = stringResource(R.string.close),
					)
				}
			}
			HorizontalDivider(
				color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
			)
		}
	}
}

/** Glass-tinted fill for sheet containers hosted inside Material Bottom/Side dialogs. */
@Composable
fun adaptiveSheetContainerColor() = inkstackGlassTint(alpha = 0.90f)

@Composable
fun isAdaptiveSheetTablet(): Boolean {
	val context = LocalContext.current
	return remember(context.resources.configuration) {
		context.resources.getBoolean(R.bool.is_tablet)
	}
}
