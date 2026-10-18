package org.inkstack.parsers.site.madara.es

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("INFRAFANDUB", "InfraFandub", "es")
internal class Infrafandub(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.INFRAFANDUB, "infrafandub.com", 10) {
	override val datePattern = "dd/MM/yyyy"
}
