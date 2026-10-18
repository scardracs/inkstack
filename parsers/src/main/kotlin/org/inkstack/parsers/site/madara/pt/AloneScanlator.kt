package org.inkstack.parsers.site.madara.pt

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("ALONESCANLATOR", "AloneScanlator", "pt")
internal class AloneScanlator(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.ALONESCANLATOR, "alonescanlator.com.br", 10) {
	override val datePattern: String = "dd/MM/yyyy"
}
