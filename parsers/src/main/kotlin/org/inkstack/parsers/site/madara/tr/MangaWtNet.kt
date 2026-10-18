package org.inkstack.parsers.site.madara.tr

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("MANGAWT_NET", "MangaWt.net", "tr")
internal class MangaWtNet(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.MANGAWT_NET, "mangawt.com")
