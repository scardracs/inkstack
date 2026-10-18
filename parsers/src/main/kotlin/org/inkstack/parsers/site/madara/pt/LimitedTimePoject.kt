package org.inkstack.parsers.site.madara.pt

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("LIMITEDTIMEPOJECT", "LimitedTimePoject", "pt")
internal class LimitedTimePoject(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.LIMITEDTIMEPOJECT, "limitedtimeproject.com", 10) {
	override val listUrl = "manhwa/"
	override val tagPrefix = "manhwa-genero/"
	override val datePattern = "dd/MM/yyyy"
}
