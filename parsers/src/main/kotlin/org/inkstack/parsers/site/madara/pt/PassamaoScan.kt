package org.inkstack.parsers.site.madara.pt

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("PASSAMAOSCAN", "PassamaoScan", "pt")
internal class PassamaoScan(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.PASSAMAOSCAN, "passamaoscan.com") {
	override val datePattern: String = "dd/MM/yyyy"
}
