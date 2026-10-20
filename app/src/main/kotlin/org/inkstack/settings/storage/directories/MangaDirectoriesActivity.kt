package org.inkstack.settings.storage.directories

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.core.ui.BaseComposeActivity
import org.inkstack.settings.storage.directories.compose.MangaDirectoriesScreen

@AndroidEntryPoint
class MangaDirectoriesActivity : BaseComposeActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setComposeContent {
			MangaDirectoriesScreen(onBack = { dispatchNavigateUp() })
		}
	}
}
