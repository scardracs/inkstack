package org.inkstack.parsers.site.madara.pt

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("WONDERLANDSCAN", "WonderlandScan", "pt")
internal class WonderlandScan(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.WONDERLANDSCAN, "wonderlandscan.com") {
	override val datePattern: String = "dd/MM/yyyy"
}
