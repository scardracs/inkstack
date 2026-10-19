package org.inkstack.search.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import org.inkstack.core.nav.AppRouter
import org.inkstack.core.ui.widgets.ChipsView
import org.inkstack.core.util.ext.findActivity
import org.inkstack.filter.data.PersistableFilter
import org.inkstack.filter.ui.FilterCoordinator
import org.inkstack.filter.ui.FilterHeaderProducer
import org.inkstack.filter.ui.model.FilterHeaderModel
import org.inkstack.parsers.model.ContentRating
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.Demographic
import org.inkstack.parsers.model.MangaState
import org.inkstack.parsers.model.MangaTag
import org.inkstack.parsers.model.YEAR_UNKNOWN
import java.util.Locale

@Composable
fun FilterHeaderBar(
	filterCoordinator: FilterCoordinator,
	producer: FilterHeaderProducer,
	modifier: Modifier = Modifier,
	visible: Boolean = true,
) {
	if (!visible) return
	val headerFlow = remember(filterCoordinator, producer) {
		producer.observeHeader(filterCoordinator).flowOn(Dispatchers.Default)
	}
	val header by headerFlow.collectAsState(
		initial = FilterHeaderModel(
			chips = emptyList(),
			sortOrder = filterCoordinator.snapshot().sortOrder,
			isFilterApplied = false,
		),
	)
	if (header.chips.isEmpty()) return
	val context = LocalContext.current
	val router = remember(context) {
		(context.findActivity() as? FragmentActivity)?.let(::AppRouter)
	}
	val scroll = rememberScrollState()
	Row(
		modifier = modifier
			.fillMaxWidth()
			.horizontalScroll(scroll)
			.padding(horizontal = 12.dp, vertical = 4.dp),
		horizontalArrangement = Arrangement.spacedBy(8.dp),
	) {
		header.chips.forEach { chip ->
			FilterHeaderChip(
				chip = chip,
				onClick = { onChipClick(filterCoordinator, router, chip) },
				onClose = { onChipClose(filterCoordinator, chip) },
			)
		}
	}
}

@Composable
private fun FilterHeaderChip(
	chip: ChipsView.ChipModel,
	onClick: () -> Unit,
	onClose: () -> Unit,
) {
	val label = chipTitle(chip)
	val leading: (@Composable () -> Unit)? = if (chip.icon != 0) {
		{ Icon(painterResource(chip.icon), contentDescription = null) }
	} else {
		null
	}
	if (chip.isCloseable) {
		InputChip(
			selected = true,
			onClick = onClick,
			label = { Text(label, style = MaterialTheme.typography.labelLarge) },
			leadingIcon = leading,
			trailingIcon = {
				Icon(
					Icons.Filled.Close,
					contentDescription = null,
					modifier = Modifier
						.size(18.dp)
						.clickable(onClick = onClose),
				)
			},
		)
	} else {
		FilterChip(
			selected = chip.isChecked,
			onClick = onClick,
			label = { Text(label, style = MaterialTheme.typography.labelLarge) },
			leadingIcon = leading,
			colors = FilterChipDefaults.filterChipColors(
				containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
				selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
				selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
				selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
			),
		)
	}
}

@Composable
private fun chipTitle(chip: ChipsView.ChipModel): String {
	val raw = chip.title?.toString()
	if (!raw.isNullOrEmpty()) return raw
	return if (chip.titleResId != 0) stringResource(chip.titleResId) else ""
}

private fun onChipClick(filter: FilterCoordinator, router: AppRouter?, chip: ChipsView.ChipModel) {
	when (val data = chip.data) {
		is MangaTag -> filter.toggleTag(data, !chip.isChecked)
		is PersistableFilter -> if (chip.isChecked) {
			filter.reset()
		} else {
			filter.setAdjusted(data.filter)
		}
		is String -> Unit
		null -> router?.showTagsCatalogSheet(excludeMode = false)
		else -> Unit
	}
}

private fun onChipClose(filter: FilterCoordinator, chip: ChipsView.ChipModel) {
	when (val data = chip.data) {
		is String -> if (data == filter.snapshot().listFilter.author) {
			filter.setAuthor(null)
		} else {
			filter.setQuery(null)
		}
		is ContentRating -> filter.toggleContentRating(data, false)
		is Demographic -> filter.toggleDemographic(data, false)
		is ContentType -> filter.toggleContentType(data, false)
		is MangaState -> filter.toggleState(data, false)
		is Locale -> filter.setLocale(null)
		is Int -> filter.setYear(YEAR_UNKNOWN)
		is IntRange -> filter.setYearRange(YEAR_UNKNOWN, YEAR_UNKNOWN)
		else -> Unit
	}
}
