package org.inkstack.tracker.ui.debug

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.core.nav.router
import org.inkstack.core.ui.BaseComposeActivity
import org.inkstack.tracker.ui.debug.compose.TrackerDebugScreen

@AndroidEntryPoint
class TrackerDebugActivity : BaseComposeActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setComposeContent {
			TrackerDebugScreen(
				onBack = { dispatchNavigateUp() },
				onOpenManga = { router.openDetails(it) },
			)
		}
	}
}
