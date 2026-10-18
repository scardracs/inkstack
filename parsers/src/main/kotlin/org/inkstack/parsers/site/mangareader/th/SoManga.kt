package org.inkstack.parsers.site.mangareader.th

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser
import org.inkstack.parsers.Broken

@Broken("Original site closed")
@MangaSourceParser("SOMANGA", "SoManga", "th")
internal class SoManga(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.SOMANGA, "so-manga.com", pageSize = 5, searchPageSize = 25)
