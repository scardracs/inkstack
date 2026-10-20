package org.inkstack.stats.ui.sheet

import androidx.collection.IntList
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.R
import org.inkstack.core.nav.router
import org.inkstack.core.ui.compose.sheet.ComposeAdaptiveSheet
import org.inkstack.parsers.util.format

@AndroidEntryPoint
class MangaStatsSheet : ComposeAdaptiveSheet() {

	private val viewModel: MangaStatsViewModel by viewModels()

	@Composable
	override fun sheetTitle(): String = stringResource(R.string.reading_stats)

	@Composable
	override fun SheetContent(onDismissRequest: () -> Unit) {
		val context = LocalContext.current
		val stats by viewModel.stats.collectAsState()
		val startDate by viewModel.startDate.collectAsState()
		val totalPages by viewModel.totalPagesRead.collectAsState()
		val barColor = MaterialTheme.colorScheme.primary

		Column(
			modifier = Modifier
				.fillMaxWidth()
				.verticalScroll(rememberScrollState())
				.padding(horizontal = 16.dp, vertical = 8.dp),
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.SpaceBetween,
			) {
				Text(
					text = viewModel.manga.title,
					style = MaterialTheme.typography.titleMedium,
					modifier = Modifier.weight(1f),
				)
				IconButton(onClick = { router.openDetails(viewModel.manga) }) {
					Icon(
						painter = painterResource(R.drawable.ic_open_external),
						contentDescription = stringResource(R.string.details),
					)
				}
			}
			SimpleBarChart(
				stats = stats,
				barColor = barColor,
				modifier = Modifier
					.fillMaxWidth()
					.height(240.dp)
					.padding(top = 12.dp),
			)
			startDate?.let {
				Text(
					text = it.format(context),
					style = MaterialTheme.typography.labelSmall,
					modifier = Modifier.padding(top = 2.dp),
				)
			}
			Text(
				text = stringResource(R.string.pages_read_s, totalPages.format()),
				style = MaterialTheme.typography.bodyMedium,
				modifier = Modifier.padding(top = 12.dp),
			)
		}
	}
}

@Composable
private fun SimpleBarChart(
	stats: IntList,
	barColor: Color,
	modifier: Modifier = Modifier,
) {
	val values = remember(stats) {
		buildList(stats.size) {
			stats.forEach { add(it) }
		}
	}
	val max = values.maxOrNull()?.coerceAtLeast(1) ?: 1
	Canvas(modifier = modifier) {
		if (values.isEmpty()) return@Canvas
		val gap = 2.dp.toPx()
		val barWidth = ((size.width - gap * (values.size - 1)) / values.size).coerceAtLeast(1f)
		values.forEachIndexed { index, value ->
			val h = (value.toFloat() / max) * size.height
			val x = index * (barWidth + gap)
			drawRoundRect(
				color = barColor,
				topLeft = Offset(x, size.height - h),
				size = Size(barWidth, h),
				cornerRadius = CornerRadius(2.dp.toPx()),
			)
		}
	}
}
