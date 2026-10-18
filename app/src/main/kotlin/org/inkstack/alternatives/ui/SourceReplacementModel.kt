package org.inkstack.alternatives.ui

import org.inkstack.list.ui.model.ListModel
import org.inkstack.parsers.model.MangaSource

data class SourceReplacementModel(
	val source: MangaSource?,
	val matched: Int,
	val total: Int,
	val matchPercent: Int,
	val isSelected: Boolean,
) : ListModel {

	override fun areItemsTheSame(other: ListModel): Boolean {
		return other is SourceReplacementModel && other.source?.name == source?.name
	}
}
