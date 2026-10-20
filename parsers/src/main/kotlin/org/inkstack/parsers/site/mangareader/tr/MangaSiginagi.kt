package org.inkstack.parsers.site.mangareader.tr

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser
import org.inkstack.parsers.Broken

@Broken("Selectors outdated (empty list)")
@MangaSourceParser("MANGASIGINAGI", "MangaSiginagi", "tr")
internal class MangaSiginagi(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.MANGASIGINAGI, "mangasiginagi.com", pageSize = 20, searchPageSize = 10)
