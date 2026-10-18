package org.inkstack.parsers.core

import org.inkstack.parsers.InternalParsersApi
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.model.Manga
import org.inkstack.parsers.model.MangaListFilter
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.model.SortOrder

@InternalParsersApi
public abstract class SinglePageMangaParser(
	context: MangaLoaderContext,
	source: MangaParserSource,
) : AbstractMangaParser(context, source) {

	final override suspend fun getList(offset: Int, order: SortOrder, filter: MangaListFilter): List<Manga> {
		if (offset > 0) {
			return emptyList()
		}
		return getList(order, filter)
	}

	public abstract suspend fun getList(order: SortOrder, filter: MangaListFilter): List<Manga>
}
