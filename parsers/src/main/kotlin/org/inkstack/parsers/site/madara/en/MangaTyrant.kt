package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("MANGATYRANT", "MangaTyrant", "en")
internal class MangaTyrant(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.MANGATYRANT, "mangatyrant.com")
