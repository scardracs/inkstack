package org.inkstack.search.domain

import org.inkstack.parsers.model.Manga
import org.inkstack.parsers.model.MangaListFilter
import org.inkstack.parsers.model.SortOrder

data class SearchResults(
	val listFilter: MangaListFilter,
	val sortOrder: SortOrder,
	val manga: List<Manga>,
)
