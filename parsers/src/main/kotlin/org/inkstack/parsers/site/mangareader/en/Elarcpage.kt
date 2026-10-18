package org.inkstack.parsers.site.mangareader.en

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@Broken
@MangaSourceParser("ELARCPAGE", "ElarcPage", "en")
internal class Elarcpage(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.ELARCPAGE, "elarctoon.com", pageSize = 20, searchPageSize = 10) {
	override val listUrl = "/series"
}
