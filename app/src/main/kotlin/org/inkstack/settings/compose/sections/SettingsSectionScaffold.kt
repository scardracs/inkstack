package org.inkstack.settings.compose.sections

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.inkstack.core.ui.compose.InkstackScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSectionScaffold(
	@StringRes titleRes: Int,
	onBack: () -> Unit,
	modifier: Modifier = Modifier,
	title: String? = null,
	snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
	actions: @Composable () -> Unit = {},
	content: LazyListScope.() -> Unit,
) {
	InkstackScaffold(
		modifier = modifier,
		snackbarHostState = snackbarHostState,
		topBar = {
			TopAppBar(
				title = { Text(title ?: stringResource(titleRes)) },
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
					}
				},
				actions = { actions() },
				windowInsets = WindowInsets.statusBars,
				colors = TopAppBarDefaults.topAppBarColors(),
			)
		},
	) { padding ->
		LazyColumn(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding),
			content = content,
		)
	}
}
