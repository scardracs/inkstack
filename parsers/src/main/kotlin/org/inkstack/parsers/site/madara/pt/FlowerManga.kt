package org.inkstack.parsers.site.madara.pt

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("FLOWERMANGA", "FlowerManga", "pt")
internal class FlowerManga(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.FLOWERMANGA, "flowermanga.net", 24) {
	override val datePattern = "d MMMM yyyy"
}
