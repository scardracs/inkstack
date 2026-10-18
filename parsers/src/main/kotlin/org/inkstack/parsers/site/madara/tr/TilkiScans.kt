package org.inkstack.parsers.site.madara.tr

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("TILKISCANS", "TilkiScans", "tr")
internal class TilkiScans(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.TILKISCANS, "www.tilkiscans.com", pageSize = 18) {
	override val datePattern = "dd/MM/yyyy"
}
