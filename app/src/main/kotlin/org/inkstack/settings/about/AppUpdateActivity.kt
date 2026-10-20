package org.inkstack.settings.about

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.core.ui.BaseComposeActivity
import org.inkstack.settings.about.compose.AppUpdateScreen

@AndroidEntryPoint
class AppUpdateActivity : BaseComposeActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setComposeContent {
			AppUpdateScreen(onCancel = { finishAfterTransition() })
		}
	}
}
