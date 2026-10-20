package org.inkstack.settings.compose.sections

import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.noties.markwon.Markwon
import org.inkstack.R
import org.inkstack.core.ui.compose.InkstackScaffold
import org.inkstack.settings.about.changelog.ChangelogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangelogScreen(
	onBack: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: ChangelogViewModel = hiltViewModel(),
) {
	val context = LocalContext.current
	val changelog by viewModel.changelog.collectAsState()
	val isLoading by viewModel.isLoading.collectAsState()
	val markwon = remember { Markwon.create(context) }

	InkstackScaffold(
		modifier = modifier,
		topBar = {
			TopAppBar(
				title = { Text(stringResource(R.string.changelog)) },
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
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding),
		) {
			if (isLoading && changelog == null) {
				CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
			}
			changelog?.let { md ->
				AndroidView(
					factory = { ctx ->
						TextView(ctx).apply {
							setTextIsSelectable(true)
							setPadding(
								resources.getDimensionPixelOffset(R.dimen.screen_padding),
								resources.getDimensionPixelOffset(R.dimen.screen_padding),
								resources.getDimensionPixelOffset(R.dimen.screen_padding),
								resources.getDimensionPixelOffset(R.dimen.screen_padding),
							)
						}
					},
					update = { tv ->
						markwon.setMarkdown(tv, md)
					},
					modifier = Modifier
						.fillMaxWidth()
						.verticalScroll(rememberScrollState()),
				)
			}
		}
	}
}
