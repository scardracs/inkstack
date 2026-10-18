package org.inkstack.parsers.site.mangareader.th

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaListFilterCapabilities
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@MangaSourceParser("ECCHIDOUJIN", "EcchiDoujin", "th", ContentType.HENTAI)
internal class EcchiDoujin(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.ECCHIDOUJIN, "ecchi-doujin.com", pageSize = 30, searchPageSize = 10) {
	override val listUrl = "/doujin"
	override val filterCapabilities: MangaListFilterCapabilities
		get() = super.filterCapabilities.copy(
			isTagsExclusionSupported = false,
		)
}
