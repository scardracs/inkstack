package org.inkstack.core.ui.util

import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController

class SystemUiController(
	private val window: Window,
) {

	private val insetsController = checkNotNull(window.decorView.windowInsetsController)

	fun setSystemUiVisible(value: Boolean) {
		if (value) {
			insetsController.show(WindowInsets.Type.systemBars())
			insetsController.systemBarsBehavior = WindowInsetsController.BEHAVIOR_DEFAULT
		} else {
			insetsController.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
			insetsController.hide(WindowInsets.Type.systemBars())
		}
	}
}
