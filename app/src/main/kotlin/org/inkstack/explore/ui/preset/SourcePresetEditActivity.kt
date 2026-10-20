package org.inkstack.explore.ui.preset

import android.content.Context
import android.content.Intent
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.core.nav.AppRouter
import org.inkstack.core.ui.BaseComposeActivity
import org.inkstack.explore.ui.preset.compose.SourcePresetEditScreen

@AndroidEntryPoint
class SourcePresetEditActivity : BaseComposeActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setComposeContent {
			SourcePresetEditScreen(
				onBack = { dispatchNavigateUp() },
				onSaved = { finishAfterTransition() },
			)
		}
	}

	companion object {

		fun newIntent(context: Context, presetId: Long = SourcePresetEditViewModel.NO_ID): Intent {
			return Intent(context, SourcePresetEditActivity::class.java)
				.putExtra(AppRouter.KEY_ID, presetId)
		}
	}
}
