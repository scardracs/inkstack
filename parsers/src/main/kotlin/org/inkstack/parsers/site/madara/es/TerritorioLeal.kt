package org.inkstack.parsers.site.madara.es

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("TERRITORIOLEAL", "TerritorioLeal", "es")
internal class TerritorioLeal(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.TERRITORIOLEAL, "territorioleal.com") {
	override val datePattern = "d 'de' MMMM 'de' yyyy"
}
