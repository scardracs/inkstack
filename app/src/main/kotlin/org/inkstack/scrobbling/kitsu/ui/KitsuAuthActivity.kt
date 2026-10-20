package org.inkstack.scrobbling.kitsu.ui

import android.os.Bundle
import org.inkstack.core.ui.BaseComposeActivity
import org.inkstack.scrobbling.kitsu.ui.compose.KitsuAuthScreen

class KitsuAuthActivity : BaseComposeActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setComposeContent {
			KitsuAuthScreen(
				onCancel = { finish() },
				onContinue = { finishAfterTransition() },
			)
		}
	}
}
