package org.inkstack.download.ui.list

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import dagger.hilt.android.AndroidEntryPoint
import org.inkstack.core.ui.BaseComposeActivity
import org.inkstack.download.ui.list.compose.DownloadsScreen

@AndroidEntryPoint
class DownloadsActivity : BaseComposeActivity() {

	private val viewModel by viewModels<DownloadsViewModel>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setComposeContent {
			val items by viewModel.items.collectAsState()
			val rows = remember(items) {
				items.filterIsInstance<DownloadItemModel>().mapNotNull { model ->
					val title = model.manga?.title ?: return@mapNotNull null
					model.id.mostSignificantBits to title
				}
			}
			DownloadsScreen(
				items = rows,
				onBack = { dispatchNavigateUp() },
			)
		}
	}
}
