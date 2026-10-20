package org.inkstack.parsers.site.mangareader.id

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@Broken("Selectors outdated (empty list)")
@MangaSourceParser("DUNIAKOMIK", "DuniaKomik", "id", ContentType.HENTAI)
internal class Duniakomik(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.DUNIAKOMIK, "duniakomik.org", pageSize = 12, searchPageSize = 12) {
	override val datePattern = "MMM d, yyyy"
}