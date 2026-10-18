package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("MANHUAGA", "ManhuaGa", "en")
internal class Manhuaga(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.MANHUAGA, "manhuaga.com")
