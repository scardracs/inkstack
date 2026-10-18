package org.inkstack.parsers.site.mangareader.id

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@MangaSourceParser("KOMIKINDO_LIVE", "Komikindo.live", "id", ContentType.HENTAI)
internal class KomikindoLive(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.KOMIKINDO_LIVE, "komikindo.live", pageSize = 20, searchPageSize = 10) {
	override val datePattern = "MMMM d, yyyy"
}
