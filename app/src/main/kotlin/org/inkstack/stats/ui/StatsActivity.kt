package org.inkstack.stats.ui

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.core.nav.router
import org.inkstack.core.ui.BaseComposeActivity
import org.inkstack.stats.ui.compose.StatsScreen

@AndroidEntryPoint
class StatsActivity : BaseComposeActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setComposeContent {
			StatsScreen(
				onBack = { dispatchNavigateUp() },
				onMangaClick = { router.showStatisticSheet(it) },
			)
		}
	}
}
