package org.inkstack.settings.reader

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.core.ui.BaseComposeActivity
import org.inkstack.settings.reader.compose.ReaderTapGridConfigScreen

@AndroidEntryPoint
class ReaderTapGridConfigActivity : BaseComposeActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setComposeContent {
			ReaderTapGridConfigScreen(onBack = { dispatchNavigateUp() })
		}
	}
}
