package org.inkstack.parsers.site.madara.pt

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("FAYSCANS", "FayScans", "pt")
internal class FayScans(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.FAYSCANS, "fayscans.net") {
	override val datePattern: String = "dd/MM/yyyy"
}
