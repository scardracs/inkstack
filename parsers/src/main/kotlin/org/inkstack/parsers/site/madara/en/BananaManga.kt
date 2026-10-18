package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("BANANA_MANGA", "BananaManga", "en")
internal class BananaManga(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.BANANA_MANGA, "bananamanga.net", 20)
