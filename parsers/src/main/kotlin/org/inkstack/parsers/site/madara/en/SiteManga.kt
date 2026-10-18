package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("SITEMANGA", "SiteManga", "en")
internal class SiteManga(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.SITEMANGA, "sitemanga.com") {
	override val datePattern = "MM/dd/yyyy"
}
