package org.inkstack.parsers.site.madara.tr

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser


@MangaSourceParser("MUGIMANGA", "MugiManga", "tr")
internal class MugiManga(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.MUGIMANGA, "mugimanga.com", 20) {
	override val datePattern = "dd/MM/yyyy"
}
