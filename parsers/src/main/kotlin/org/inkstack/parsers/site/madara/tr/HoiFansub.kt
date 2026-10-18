package org.inkstack.parsers.site.madara.tr

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("HOIFANSUB", "HoiFansub", "tr")
internal class HoiFansub(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.HOIFANSUB, "hoifansub.com", 20)
