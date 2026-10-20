package org.inkstack.settings.sources.catalog

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.core.nav.router
import org.inkstack.core.ui.BaseComposeActivity
import org.inkstack.settings.sources.catalog.compose.SourcesCatalogScreen

@AndroidEntryPoint
class SourcesCatalogActivity : BaseComposeActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setComposeContent {
			SourcesCatalogScreen(
				onBack = { dispatchNavigateUp() },
				onOpenSource = { source -> router.openList(source, null, null) },
			)
		}
	}
}
