package org.inkstack.settings.tracker.categories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.R
import org.inkstack.core.ui.compose.sheet.ComposeAdaptiveSheet

@AndroidEntryPoint
class TrackerCategoriesConfigSheet : ComposeAdaptiveSheet() {

	private val viewModel by viewModels<TrackerCategoriesConfigViewModel>()

	@Composable
	override fun sheetTitle(): String = stringResource(R.string.favourites_categories)

	@Composable
	override fun SheetContent(onDismissRequest: () -> Unit) {
		val content by viewModel.content.collectAsState()
		LazyColumn(modifier = Modifier.fillMaxWidth()) {
			items(content, key = { it.id }) { category ->
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.clickable { viewModel.toggleItem(category) }
						.padding(horizontal = 16.dp, vertical = 8.dp),
					verticalAlignment = Alignment.CenterVertically,
				) {
					Checkbox(
						checked = category.isTrackingEnabled,
						onCheckedChange = { viewModel.toggleItem(category) },
					)
					Text(
						text = category.title,
						modifier = Modifier.padding(start = 8.dp),
					)
				}
			}
		}
	}
}
