package org.inkstack.parsers.site.mangareader.th

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaListFilterCapabilities
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@MangaSourceParser("MANGA168", "Manga168", "th", ContentType.HENTAI)
internal class Manga168(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.MANGA168, "manga168.net", pageSize = 40, searchPageSize = 30) {
	override val filterCapabilities: MangaListFilterCapabilities
		get() = super.filterCapabilities.copy(
			isTagsExclusionSupported = false,
		)
}
