package org.inkstack.parsers.site.madara.pt

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("NORTEROSE", "Norterose", "pt")
internal class Norterose(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.NORTEROSE, "norterose.com.br", 10) {
	override val datePattern: String = "dd/MM/yyyy"
	override val withoutAjax = true
}
