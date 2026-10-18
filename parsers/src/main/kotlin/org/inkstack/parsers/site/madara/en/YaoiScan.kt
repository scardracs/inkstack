package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("YAOISCAN", "YaoiScan", "en", ContentType.HENTAI)
internal class YaoiScan(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.YAOISCAN, "yaoiscan.com", 20)
