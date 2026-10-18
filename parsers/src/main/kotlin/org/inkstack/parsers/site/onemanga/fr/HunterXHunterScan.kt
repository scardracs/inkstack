package org.inkstack.parsers.site.onemanga.fr

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.onemanga.OneMangaParser

@MangaSourceParser("HUNTERXHUNTERSCAN", "HunterXHunterScan", "fr")
internal class HunterXHunterScan(context: MangaLoaderContext) :
	OneMangaParser(context, MangaParserSource.HUNTERXHUNTERSCAN, "hunterxhunterscan.com")
