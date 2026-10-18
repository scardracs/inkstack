package org.inkstack.parsers.site.mangareader.id

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser
import java.util.*

@MangaSourceParser("MANGADOP", "MangaDop", "id", ContentType.HENTAI)
internal class Mangadop(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.MANGADOP, "mangadop.net", pageSize = 20, searchPageSize = 20) {
	override val sourceLocale: Locale = Locale.ENGLISH
}
