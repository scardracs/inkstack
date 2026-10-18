package org.inkstack.list.ui.model

import org.inkstack.core.ui.model.MangaOverride
import org.inkstack.core.ui.widgets.ChipsView
import org.inkstack.list.domain.ReadingProgress
import org.inkstack.list.ui.ListModelDiffCallback.Companion.PAYLOAD_ANYTHING_CHANGED
import org.inkstack.list.ui.ListModelDiffCallback.Companion.PAYLOAD_PROGRESS_CHANGED
import org.inkstack.parsers.model.Manga

data class MangaDetailedListModel(
	override val manga: Manga,
	override val override: MangaOverride?,
	val subtitle: String?,
	override val counter: Int,
	val progress: ReadingProgress?,
	val isFavorite: Boolean,
	val isSaved: Boolean,
	val tags: List<ChipsView.ChipModel>,
	val isPinned: Boolean = false,
) : MangaListModel() {

	override fun getChangePayload(previousState: ListModel): Any? = when {
		previousState !is MangaDetailedListModel || previousState.manga != manga -> null

		previousState.progress != progress -> PAYLOAD_PROGRESS_CHANGED
		previousState.isFavorite != isFavorite ||
			previousState.isSaved != isSaved ||
			previousState.isPinned != isPinned -> PAYLOAD_ANYTHING_CHANGED

		else -> super.getChangePayload(previousState)
	}
}
