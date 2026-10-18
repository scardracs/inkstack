package org.inkstack.parsers.site.madara.es

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("HERENSCAN", "HerenScan", "es")
internal class HerenScan(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.HERENSCAN, "herenscan.com") {
	override val datePattern = "dd/MM/yyyy"
}
