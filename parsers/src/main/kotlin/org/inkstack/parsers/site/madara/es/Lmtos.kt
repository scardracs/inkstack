package org.inkstack.parsers.site.madara.es

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken("Not dead, changed template")
@MangaSourceParser("LMTOS", "Lmtos", "es")
internal class Lmtos(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.LMTOS, "lmtos.com") {
	override val datePattern = "dd/MM"
}
