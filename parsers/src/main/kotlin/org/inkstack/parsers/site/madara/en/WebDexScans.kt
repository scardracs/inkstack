package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("WEBDEXSCANS", "WebDexScans", "en")
internal class WebDexScans(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.WEBDEXSCANS, "webdexscans.com")
