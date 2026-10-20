package org.inkstack.parsers.site.heancmsalt.es

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@MangaSourceParser("LEGIONSCANS", "CerberusSeries", "es")
internal class CerberuSeries(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.LEGIONSCANS, "legionscans.com", 20, 10) {
	override val listUrl = "/wp/manga"
}
