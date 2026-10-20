package org.inkstack.reader.ui.colorfilter

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.core.ui.BaseComposeActivity
import org.inkstack.reader.ui.colorfilter.compose.ColorFilterConfigScreen

@AndroidEntryPoint
class ColorFilterConfigActivity : BaseComposeActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setComposeContent {
			ColorFilterConfigScreen(onBack = { finishAfterTransition() })
		}
	}
}
