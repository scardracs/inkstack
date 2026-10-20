package org.inkstack.filter.ui.sheet

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.inkstack.R
import org.inkstack.core.nav.router
import org.inkstack.core.ui.compose.sheet.ComposeAdaptiveSheet
import org.inkstack.filter.ui.FilterCoordinator

/**
 * Filter sheet on [ComposeAdaptiveSheet] with full Compose body.
 */
class FilterSheetFragment : ComposeAdaptiveSheet() {

	/** Dialog uses Compose glass header; embedded side pane keeps chrome off. */
	override val useAdaptiveChrome: Boolean
		get() = showsDialog

	@Composable
	override fun sheetTitle(): String = stringResource(R.string.filter)

	@Composable
	override fun SheetContent(onDismissRequest: () -> Unit) {
		val filter = FilterCoordinator.require(this)
		FilterSheetContent(
			filter = filter,
			showsDialog = showsDialog,
			onShowTagsCatalog = { exclude -> router.showTagsCatalogSheet(excludeMode = exclude) },
			onDismissRequest = onDismissRequest,
		)
	}
}
