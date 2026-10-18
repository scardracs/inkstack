package org.inkstack.parsers.site.mangareader.ar

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@MangaSourceParser("MANGAFLAME", "MangaFlame", "ar")
internal class MangaFlame(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.MANGAFLAME, "mangaflame.org", pageSize = 20, searchPageSize = 10)
