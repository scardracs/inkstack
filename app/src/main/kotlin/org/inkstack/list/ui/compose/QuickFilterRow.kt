package org.inkstack.list.ui.compose

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.inkstack.core.ui.widgets.ChipsView
import org.inkstack.list.domain.ListFilterOption
import org.inkstack.list.ui.model.QuickFilter

@Composable
fun QuickFilterRow(
	filter: QuickFilter,
	onToggle: (ListFilterOption) -> Unit,
	modifier: Modifier = Modifier,
) {
	Row(
		modifier = modifier
			.fillMaxWidth()
			.horizontalScroll(rememberScrollState())
			.padding(horizontal = 16.dp, vertical = 4.dp),
		horizontalArrangement = Arrangement.spacedBy(8.dp),
	) {
		filter.items.forEach { chip ->
			val option = chip.data as? ListFilterOption ?: return@forEach
			FilterChip(
				selected = chip.isChecked,
				onClick = { onToggle(option) },
				label = {
					Text(
						text = chipTitle(chip),
						style = MaterialTheme.typography.labelLarge,
					)
				},
				leadingIcon = if (chip.icon != 0) {
					{
						Icon(
							painter = painterResource(chip.icon),
							contentDescription = null,
						)
					}
				} else {
					null
				},
				colors = FilterChipDefaults.filterChipColors(
					containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
					selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
					selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
					selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
				),
			)
		}
	}
}

@Composable
private fun chipTitle(chip: ChipsView.ChipModel): String {
	val raw = chip.title?.toString()
	if (!raw.isNullOrEmpty()) return raw
	return if (chip.titleResId != 0) stringResource(chip.titleResId) else ""
}
