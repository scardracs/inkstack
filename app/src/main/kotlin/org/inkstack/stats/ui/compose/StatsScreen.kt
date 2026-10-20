package org.inkstack.stats.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.inkstack.R
import org.inkstack.core.ui.compose.InkstackScaffold
import org.inkstack.core.util.InkstackColors
import org.inkstack.parsers.model.Manga
import org.inkstack.stats.domain.StatsPeriod
import org.inkstack.stats.ui.StatsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
	onBack: () -> Unit,
	onMangaClick: (Manga) -> Unit,
	modifier: Modifier = Modifier,
	viewModel: StatsViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val records by viewModel.readingStats.collectAsState()
	val period by viewModel.period.collectAsState()
	val categories by viewModel.favoriteCategories.collectAsState(initial = emptyList())
	val selectedCategories by viewModel.selectedCategories.collectAsState()
	val isLoading by viewModel.isLoading.collectAsState()
	var periodMenu by remember { mutableStateOf(false) }
	var confirmClear by remember { mutableStateOf(false) }

	val sum = remember(records) { records.sumOf { it.duration }.coerceAtLeast(1L) }
	val segments = remember(records) {
		records.map { record ->
			PieSegment(
				color = Color(InkstackColors.ofManga(context, record.manga)),
				sweep = (record.duration.toFloat() / sum) * 360f,
				label = record.manga?.title ?: context.getString(R.string.other_manga),
				manga = record.manga,
			)
		}
	}

	InkstackScaffold(
		modifier = modifier,
		topBar = {
			TopAppBar(
				title = { Text(stringResource(R.string.reading_stats)) },
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
					}
				},
				actions = {
					IconButton(onClick = { confirmClear = true }) {
						Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.clear_stats))
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
			if (isLoading) {
				LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
			}
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.horizontalScroll(rememberScrollState())
					.padding(horizontal = 12.dp, vertical = 8.dp),
				horizontalArrangement = Arrangement.spacedBy(8.dp),
			) {
				Box {
					FilterChip(
						selected = true,
						onClick = { periodMenu = true },
						label = { Text(stringResource(period.titleResId)) },
					)
					DropdownMenu(expanded = periodMenu, onDismissRequest = { periodMenu = false }) {
						StatsPeriod.entries.forEach { p ->
							DropdownMenuItem(
								text = { Text(stringResource(p.titleResId)) },
								onClick = {
									viewModel.period.value = p
									periodMenu = false
								},
							)
						}
					}
				}
				categories.forEach { category ->
					FilterChip(
						selected = category.id in selectedCategories,
						onClick = {
							viewModel.setCategoryChecked(category, category.id !in selectedCategories)
						},
						label = { Text(category.title) },
					)
				}
			}
			if (records.isEmpty()) {
				Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
					Column(horizontalAlignment = Alignment.CenterHorizontally) {
						Text(stringResource(R.string.text_empty_holder_primary))
						Text(
							stringResource(R.string.empty_stats_text),
							style = MaterialTheme.typography.bodyMedium,
						)
					}
				}
			} else {
				LazyColumn(modifier = Modifier.fillMaxSize()) {
					item {
						StatsPieChart(
							segments = segments,
							onSegmentClick = { manga -> manga?.let(onMangaClick) },
							modifier = Modifier
								.fillMaxWidth()
								.padding(24.dp)
								.aspectRatio(1f),
						)
					}
					items(records, key = { it.manga?.id ?: -1L }) { record ->
						ListItem(
							headlineContent = {
								Text(record.manga?.title ?: stringResource(R.string.other_manga))
							},
							supportingContent = {
								Text(record.time.format(context.resources))
							},
							leadingContent = {
								Box(
									Modifier
										.size(12.dp)
										.clip(CircleShape)
										.then(
											Modifier.clickable(enabled = false) {},
										),
								) {
									Canvas(Modifier.fillMaxSize()) {
										drawCircle(Color(InkstackColors.ofManga(context, record.manga)))
									}
								}
							},
							modifier = Modifier
								.fillMaxWidth()
								.clickable(enabled = record.manga != null) {
									record.manga?.let(onMangaClick)
								},
						)
					}
				}
			}
		}
	}

	if (confirmClear) {
		AlertDialog(
			onDismissRequest = { confirmClear = false },
			title = { Text(stringResource(R.string.clear_stats)) },
			text = { Text(stringResource(R.string.clear_stats_confirm)) },
			confirmButton = {
				TextButton(
					onClick = {
						viewModel.clearStats()
						confirmClear = false
					},
				) { Text(stringResource(R.string.clear)) }
			},
			dismissButton = {
				TextButton(onClick = { confirmClear = false }) {
					Text(stringResource(android.R.string.cancel))
				}
			},
		)
	}
}

private data class PieSegment(
	val color: Color,
	val sweep: Float,
	val label: String,
	val manga: Manga?,
)

@Composable
private fun StatsPieChart(
	segments: List<PieSegment>,
	onSegmentClick: (Manga?) -> Unit,
	modifier: Modifier = Modifier,
) {
	Canvas(
		modifier = modifier.clickable(enabled = segments.isNotEmpty()) {
			segments.maxByOrNull { it.sweep }?.manga?.let(onSegmentClick)
		},
	) {
		var start = -90f
		val stroke = Stroke(width = size.minDimension * 0.18f)
		val diameter = size.minDimension
		val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
		val arcSize = Size(diameter, diameter)
		segments.forEach { segment ->
			drawArc(
				color = segment.color,
				startAngle = start,
				sweepAngle = segment.sweep,
				useCenter = false,
				topLeft = topLeft,
				size = arcSize,
				style = stroke,
			)
			start += segment.sweep
		}
	}
}
