package org.inkstack.search.ui.multi

import android.os.Bundle
import coil3.ImageLoader
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.core.nav.router
import org.inkstack.core.ui.BaseComposeActivity
import org.inkstack.search.ui.multi.compose.SearchScreen
import javax.inject.Inject

@AndroidEntryPoint
class SearchActivity : BaseComposeActivity() {

	@Inject
	lateinit var imageLoader: ImageLoader

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setComposeContent {
			SearchScreen(
				imageLoader = imageLoader,
				onBack = { dispatchNavigateUp() },
				onMangaClick = { manga -> router.openDetails(manga) },
				onSourceClick = { source, query -> router.openSearch(source, query) },
			)
		}
	}
}
