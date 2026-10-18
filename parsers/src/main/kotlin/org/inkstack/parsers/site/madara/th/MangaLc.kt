package org.inkstack.parsers.site.madara.th

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("MANGALC", "MangaLc", "th")
internal class MangaLc(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.MANGALC, "manga-lc.net", 24) {
	override val datePattern: String = "d MMMM yyyy"
	override val selectPage = "img"
}
