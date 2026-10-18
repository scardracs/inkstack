package org.inkstack.alternatives.ui.covers

import org.inkstack.list.ui.ListModelDiffCallback
import org.inkstack.list.ui.model.ListModel
import org.inkstack.parsers.model.Manga
import org.inkstack.parsers.model.MangaSource

data class AlternativeCoverModel(
	val coverUrl: String,
	val source: MangaSource,
	val manga: Manga,
	/** The cover already in use, kept first in the carousel so it can be compared against. */
	val isCurrent: Boolean,
	val isSelected: Boolean,
) : ListModel {

	override fun areItemsTheSame(other: ListModel): Boolean {
		return other is AlternativeCoverModel && other.coverUrl == coverUrl
	}

	override fun getChangePayload(previousState: ListModel): Any? {
		return if (previousState is AlternativeCoverModel && previousState.isSelected != isSelected) {
			ListModelDiffCallback.PAYLOAD_CHECKED_CHANGED
		} else {
			super.getChangePayload(previousState)
		}
	}
}
