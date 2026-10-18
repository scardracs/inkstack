package org.inkstack.parsers.site.madara.tr

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("RUYAMANGA", "RuyaManga", "tr")
internal class RuyaManga(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.RUYAMANGA, "ruyamanga.net", 18) {
	override val tagPrefix = "manga-kategori/"
	override val datePattern = "dd/MM/yyyy"
}
