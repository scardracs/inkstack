package org.inkstack.core.ui

import android.os.Bundle
import android.view.WindowManager
import org.inkstack.core.ui.util.SystemUiController

abstract class BaseComposeFullscreenActivity : BaseComposeActivity() {

	protected lateinit var systemUiController: SystemUiController

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		with(window) {
			systemUiController = SystemUiController(this)
			attributes.layoutInDisplayCutoutMode =
				WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
		}
		systemUiController.setSystemUiVisible(true)
	}
}
