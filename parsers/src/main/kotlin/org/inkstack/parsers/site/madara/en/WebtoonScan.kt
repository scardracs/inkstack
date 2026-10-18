package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("WEBTOONSCAN", "WebtoonScan", "en", ContentType.HENTAI)
internal class WebtoonScan(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.WEBTOONSCAN, "webtoonscan.com", 20)
