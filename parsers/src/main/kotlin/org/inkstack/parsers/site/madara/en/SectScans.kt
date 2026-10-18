package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("SECTSCANS", "SectScans", "en")
internal class SectScans(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.SECTSCANS, "sectscans.com") {
	override val listUrl = "comics/"
}
