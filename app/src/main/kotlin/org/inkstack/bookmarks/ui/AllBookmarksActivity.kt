package org.inkstack.bookmarks.ui

import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import coil3.ImageLoader
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.R
import org.inkstack.bookmarks.ui.compose.AllBookmarksScreen
import org.inkstack.core.ui.BaseComposeActivity
import org.inkstack.core.ui.compose.InkstackScaffold
import org.inkstack.reader.ui.PageSaveHelper
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class AllBookmarksActivity : BaseComposeActivity() {

	@Inject
	lateinit var imageLoader: ImageLoader

	@Inject
	lateinit var pageSaveHelperFactory: PageSaveHelper.Factory

	private lateinit var pageSaveHelper: PageSaveHelper

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		pageSaveHelper = pageSaveHelperFactory.create(this)
		setComposeContent {
			val snackbarHostState = remember { SnackbarHostState() }
			InkstackScaffold(
				topBar = {
					TopAppBar(
						title = { Text(stringResource(R.string.bookmarks)) },
						navigationIcon = {
							IconButton(onClick = { dispatchNavigateUp() }) {
								Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
							}
						},
					)
				},
				snackbarHostState = snackbarHostState,
			) { padding ->
				AllBookmarksScreen(
					pageSaveHelper = pageSaveHelper,
					imageLoader = imageLoader,
					contentPadding = padding,
					snackbarHostState = snackbarHostState,
					modifier = Modifier.fillMaxSize(),
				)
			}
		}
	}
}
