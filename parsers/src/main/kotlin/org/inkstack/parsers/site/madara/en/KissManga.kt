package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("KISSMANGA", "KissManga", "en")
internal class KissManga(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.KISSMANGA, "kissmanga.in") {
	override val datePattern = "MMMM dd, yyyy"
	override val listUrl = "mangalist/"
}
