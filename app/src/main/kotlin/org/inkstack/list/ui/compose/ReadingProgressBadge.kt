package org.inkstack.list.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.inkstack.R
import org.inkstack.core.prefs.ProgressIndicatorMode.CHAPTERS_LEFT
import org.inkstack.core.prefs.ProgressIndicatorMode.CHAPTERS_READ
import org.inkstack.core.prefs.ProgressIndicatorMode.NONE
import org.inkstack.core.prefs.ProgressIndicatorMode.PERCENT_LEFT
import org.inkstack.core.prefs.ProgressIndicatorMode.PERCENT_READ
import org.inkstack.list.domain.ReadingProgress

@Composable
fun ReadingProgressBadge(
	progress: ReadingProgress?,
	modifier: Modifier = Modifier,
) {
	if (progress == null || !progress.isValid()) return
	val label = when (progress.mode) {
		NONE -> return
		PERCENT_READ -> stringResource(
			R.string.percent_string_pattern,
			ReadingProgress.percentToString(progress.percent),
		)
		PERCENT_LEFT -> stringResource(
			R.string.percent_string_pattern,
			ReadingProgress.percentToString(progress.percentLeft),
		)
		CHAPTERS_READ -> progress.chapters.toString()
		CHAPTERS_LEFT -> progress.chaptersLeft.toString()
	}
	val sweep = (progress.percent.coerceIn(0f, 1f) * 360f).let {
		if (progress.isReversed()) 360f - it else it
	}
	val track = MaterialTheme.colorScheme.surfaceVariant
	val indicator = MaterialTheme.colorScheme.primary
	Box(
		modifier = modifier.size(dimensionResource(R.dimen.card_indicator_size)),
		contentAlignment = Alignment.Center,
	) {
		Canvas(modifier = Modifier.fillMaxSize()) {
			val stroke = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
			val inset = stroke.width / 2f
			val arcSize = Size(size.width - stroke.width, size.height - stroke.width)
			drawArc(
				color = track,
				startAngle = -90f,
				sweepAngle = 360f,
				useCenter = false,
				topLeft = Offset(inset, inset),
				size = arcSize,
				style = stroke,
			)
			drawArc(
				color = indicator,
				startAngle = -90f,
				sweepAngle = sweep,
				useCenter = false,
				topLeft = Offset(inset, inset),
				size = arcSize,
				style = stroke,
			)
		}
		Text(
			text = label,
			style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
			color = MaterialTheme.colorScheme.onSurface,
			textAlign = TextAlign.Center,
			maxLines = 1,
		)
	}
}
