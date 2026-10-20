package org.inkstack.explore.ui.preset

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.core.ui.BaseComposeActivity
import org.inkstack.explore.ui.preset.compose.SourcePresetListScreen

@AndroidEntryPoint
class SourcePresetListActivity : BaseComposeActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setComposeContent {
			SourcePresetListScreen(
				onBack = { dispatchNavigateUp() },
				onCreate = { startActivity(SourcePresetEditActivity.newIntent(this)) },
				onEdit = { id -> startActivity(SourcePresetEditActivity.newIntent(this, id)) },
			)
		}
	}
}
