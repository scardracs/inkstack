package org.inkstack.tracker.ui.updates

import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import coil3.ImageLoader
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.R
import org.inkstack.core.nav.router
import org.inkstack.core.ui.BaseComposeActivity
import org.inkstack.core.ui.compose.InkstackScaffold
import org.inkstack.tracker.ui.updates.compose.UpdatesScreen
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class UpdatesActivity : BaseComposeActivity() {

	@Inject
	lateinit var imageLoader: ImageLoader

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setComposeContent {
			InkstackScaffold(
				topBar = {
					TopAppBar(
						title = { Text(stringResource(R.string.updates)) },
						navigationIcon = {
							IconButton(onClick = { dispatchNavigateUp() }) {
								Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
							}
						},
					)
				},
			) { padding ->
				UpdatesScreen(
					onMangaClick = { manga -> router.openDetails(manga) },
					imageLoader = imageLoader,
					contentPadding = padding,
					modifier = Modifier.fillMaxSize(),
				)
			}
		}
	}
}
