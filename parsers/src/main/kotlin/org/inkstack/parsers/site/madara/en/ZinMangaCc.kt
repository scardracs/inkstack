package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken
@MangaSourceParser("ZINMANGA_CC", "ZinManga.cc", "en")
internal class ZinMangaCc(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.ZINMANGA_CC, "zinmanga.cc")
