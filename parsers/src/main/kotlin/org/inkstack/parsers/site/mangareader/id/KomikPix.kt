package org.inkstack.parsers.site.mangareader.id

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@Broken("Site unreachable (connection error)")
@MangaSourceParser("KOMIKPIX", "KomikPix", "id", ContentType.HENTAI)
internal class KomikPix(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.KOMIKPIX, "komikpix.com", pageSize = 30, searchPageSize = 14) {
	override val listUrl = "/hentai"
}