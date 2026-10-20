package org.inkstack.search.ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.inkstack.R
import org.inkstack.core.ui.compose.InkstackScaffold
import org.inkstack.parsers.model.Manga

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
	results: List<Manga>,
	onBack: () -> Unit,
	onQueryChange: (String) -> Unit,
	onOpenManga: (Manga) -> Unit,
) {
	var query by rememberSaveable { mutableStateOf("") }
	InkstackScaffold(
		topBar = {
			TopAppBar(
				title = { Text(stringResource(R.string.search)) },
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
					}
				},
				windowInsets = WindowInsets.statusBars,
				colors = TopAppBarDefaults.topAppBarColors(),
			)
		},
	) { padding ->
		LazyColumn(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding),
		) {
			item {
				OutlinedTextField(
					value = query,
					onValueChange = {
						query = it
						onQueryChange(it)
					},
					modifier = Modifier
						.fillMaxWidth()
						.padding(16.dp),
					singleLine = true,
					placeholder = { Text(stringResource(R.string.search_manga)) },
				)
			}
			items(results, key = { it.id }) { manga ->
				ListItem(
					headlineContent = { Text(manga.title) },
					modifier = Modifier
						.fillMaxWidth()
						.clickable { onOpenManga(manga) },
				)
			}
		}
	}
}
