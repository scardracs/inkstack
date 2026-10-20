package org.inkstack.settings.protect

import android.os.Bundle
import android.view.WindowManager
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.core.ui.BaseComposeActivity
import org.inkstack.settings.protect.compose.ProtectSetupScreen

@AndroidEntryPoint
class ProtectSetupActivity : BaseComposeActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
		setComposeContent {
			ProtectSetupScreen(
				onCancel = { finish() },
				onDone = { finishAfterTransition() },
			)
		}
	}
}
