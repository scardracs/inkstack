package org.inkstack.settings.sources.catalog

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.inkstack.list.ui.model.ListModel
import org.inkstack.parsers.model.MangaParserSource

sealed interface SourceCatalogItem : ListModel {

	data class Source(
		val source: MangaParserSource,
		val isInPreset: Boolean = false,
		/** Really popular: flame-marked and listed first - see SourceRank.hotSources. */
		val isHot: Boolean = false,
	) : SourceCatalogItem {

		override fun areItemsTheSame(other: ListModel): Boolean {
			return other is Source && other.source == source
		}
	}

	data class Hint(
		@DrawableRes val icon: Int,
		@StringRes val title: Int,
		@StringRes val text: Int,
	) : SourceCatalogItem {

		override fun areItemsTheSame(other: ListModel): Boolean {
			return other is Hint && other.title == title
		}
	}
}
