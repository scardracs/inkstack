package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken("Site unreachable (HTTP_522)")
@MangaSourceParser("LEVIATANSCANS", "LsComic", "en")
internal class LeviatanScans(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.LEVIATANSCANS, "lscomic.com", 10)
