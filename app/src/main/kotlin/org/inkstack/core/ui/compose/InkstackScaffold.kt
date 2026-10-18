package org.inkstack.core.ui.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun InkstackScaffold(
	modifier: Modifier = Modifier,
	topBar: @Composable () -> Unit = {},
	bottomBar: @Composable () -> Unit = {},
	floatingActionButton: @Composable () -> Unit = {},
	snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
	contentWindowInsets: WindowInsets = WindowInsets.safeDrawing,
	content: @Composable (PaddingValues) -> Unit,
) {
	Scaffold(
		modifier = modifier,
		topBar = topBar,
		bottomBar = bottomBar,
		floatingActionButton = floatingActionButton,
		snackbarHost = { SnackbarHost(snackbarHostState) },
		contentWindowInsets = contentWindowInsets,
		content = content,
	)
}
