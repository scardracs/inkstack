package org.inkstack.settings.override

import android.app.Activity
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.core.ui.BaseComposeActivity
import org.inkstack.settings.override.compose.OverrideConfigScreen

@AndroidEntryPoint
class OverrideConfigActivity : BaseComposeActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setComposeContent {
			OverrideConfigScreen(
				onBack = { dispatchNavigateUp() },
				onSaved = {
					setResult(Activity.RESULT_OK)
					finish()
				},
			)
		}
	}
}
