package org.inkstack.browser.compose

import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import org.inkstack.R
import org.inkstack.core.ui.compose.InkstackScaffold

private const val PROGRESS_MAX = 100

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
	webView: WebView,
	title: CharSequence,
	subtitle: CharSequence?,
	isLoading: Boolean,
	loadProgress: Int,
	showChrome: Boolean,
	showCloseNavigation: Boolean,
	snackbarHostState: SnackbarHostState,
	onClose: () -> Unit,
	actions: @Composable RowScope.() -> Unit = {},
	modifier: Modifier = Modifier,
) {
	InkstackScaffold(
		modifier = modifier.fillMaxSize(),
		snackbarHostState = snackbarHostState,
		contentWindowInsets = if (showChrome) {
			WindowInsets.safeDrawing
		} else {
			WindowInsets(0, 0, 0, 0)
		},
		topBar = {
			if (showChrome) {
				TopAppBar(
					title = {
						Column {
							Text(
								text = title.toString(),
								maxLines = 1,
								overflow = TextOverflow.Ellipsis,
							)
							if (!subtitle.isNullOrEmpty()) {
								Text(
									text = subtitle.toString(),
									style = MaterialTheme.typography.bodySmall,
									maxLines = 1,
									overflow = TextOverflow.Ellipsis,
								)
							}
						}
					},
					navigationIcon = {
						if (showCloseNavigation) {
							IconButton(onClick = onClose) {
								Icon(
									Icons.Default.Close,
									contentDescription = stringResource(R.string.close),
								)
							}
						}
					},
					actions = actions,
					windowInsets = WindowInsets.statusBars,
					colors = TopAppBarDefaults.topAppBarColors(),
				)
			}
		},
	) { padding ->
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding),
		) {
			AndroidView(
				factory = {
					(webView.parent as? ViewGroup)?.removeView(webView)
					webView
				},
				modifier = Modifier.fillMaxSize(),
			)
			if (showChrome && isLoading) {
				if (loadProgress in 1 until PROGRESS_MAX) {
					LinearProgressIndicator(
						progress = { loadProgress / PROGRESS_MAX.toFloat() },
						modifier = Modifier.fillMaxWidth(),
					)
				} else {
					LinearProgressIndicator(
						modifier = Modifier.fillMaxWidth(),
					)
				}
			}
		}
	}
}
