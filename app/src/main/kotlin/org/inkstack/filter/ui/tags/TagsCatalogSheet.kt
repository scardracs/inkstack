package org.inkstack.filter.ui.tags

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
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
import dagger.hilt.android.lifecycle.withCreationCallback
import org.inkstack.R
import org.inkstack.core.nav.AppRouter
import org.inkstack.core.ui.compose.sheet.ComposeAdaptiveSheet
import org.inkstack.filter.ui.FilterCoordinator
import org.inkstack.filter.ui.model.TagCatalogItem
import org.inkstack.list.ui.model.LoadingState

@AndroidEntryPoint
class TagsCatalogSheet : ComposeAdaptiveSheet() {

	private val viewModel by viewModels<TagsCatalogViewModel>(
		extrasProducer = {
			defaultViewModelCreationExtras.withCreationCallback<TagsCatalogViewModel.Factory> { factory ->
				factory.create(
					filter = FilterCoordinator.require(this),
					isExcludeTag = requireArguments().getBoolean(AppRouter.KEY_EXCLUDE),
				)
			}
		},
	)

	@Composable
	override fun sheetTitle(): String = stringResource(R.string.filter)

	override fun onSheetViewCreated(savedInstanceState: android.os.Bundle?) {
		disableFitToContents()
	}

	@Composable
	override fun SheetContent(onDismissRequest: () -> Unit) {
		val content by viewModel.content.collectAsState()
		val query by viewModel.searchQuery.collectAsState()

		Column(modifier = Modifier.fillMaxSize()) {
			OutlinedTextField(
				value = query,
				onValueChange = { viewModel.searchQuery.value = it },
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 16.dp, vertical = 8.dp),
				singleLine = true,
				label = { Text(stringResource(R.string.genres_search_hint)) },
			)
			when {
				content.any { it is LoadingState } -> {
					CircularProgressIndicator(
						modifier = Modifier
							.align(Alignment.CenterHorizontally)
							.padding(24.dp),
					)
				}
				else -> {
					LazyColumn(modifier = Modifier.fillMaxSize()) {
						items(
							items = content.filterIsInstance<TagCatalogItem>(),
							key = { it.tag.key },
						) { item ->
							Row(
								modifier = Modifier
									.fillMaxWidth()
									.clickable {
										viewModel.handleTagClick(item.tag, item.isChecked)
									}
									.padding(horizontal = 16.dp, vertical = 8.dp),
								verticalAlignment = Alignment.CenterVertically,
							) {
								Checkbox(
									checked = item.isChecked,
									onCheckedChange = {
										viewModel.handleTagClick(item.tag, item.isChecked)
									},
								)
								Text(
									text = item.tag.title,
									modifier = Modifier.padding(start = 8.dp),
								)
							}
						}
					}
				}
			}
		}
	}
}
