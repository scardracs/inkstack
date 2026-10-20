package org.inkstack.alternatives.ui

import android.os.Bundle
import coil3.ImageLoader
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.alternatives.ui.compose.AlternativesScreen
import org.inkstack.core.nav.router
import org.inkstack.core.ui.BaseComposeActivity
import javax.inject.Inject

@AndroidEntryPoint
class AlternativesActivity : BaseComposeActivity() {

	@Inject
	lateinit var coil: ImageLoader

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setComposeContent {
			AlternativesScreen(
				onBack = { finishAfterTransition() },
				onOpenDetails = { router.openDetails(it) },
				onOpenSearch = { source, query -> router.openSearch(source, query) },
				imageLoader = coil,
			)
		}
	}
}
