package org.inkstack.parsers.site.mangareader.th

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@MangaSourceParser("DRAGONMANGA", "DragonManga", "th", ContentType.HENTAI)
internal class DragonManga(context: MangaLoaderContext) :
	MangaReaderParser(
		context,
		MangaParserSource.DRAGONMANGA,
		"www.dragon-manga.com",
		pageSize = 40,
		searchPageSize = 10,
	)
