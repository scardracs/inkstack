package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("BOOKMANGA", "BookManga", "en")
internal class BookManga(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.BOOKMANGA, "bookmanga.com", 20)
