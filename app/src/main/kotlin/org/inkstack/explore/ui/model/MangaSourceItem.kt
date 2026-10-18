package org.inkstack.explore.ui.model

import org.inkstack.core.model.MangaSourceInfo
import org.inkstack.list.ui.model.ListModel
import org.inkstack.parsers.util.longHashCode

data class MangaSourceItem(
	val source: MangaSourceInfo,
	val isGrid: Boolean,
	/** Among the most-used sources for this user's language - see SourceRanker.trendingSources. */
	val isTrending: Boolean = false,
) : ListModel {

	val id: Long = source.name.longHashCode()

	override fun areItemsTheSame(other: ListModel): Boolean {
		return other is MangaSourceItem && other.source == source
	}
}
