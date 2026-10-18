package org.inkstack.parsers.site.madara.pt

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("MANGANINJA", "MangaNinja", "pt")
internal class MangaNinja(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.MANGANINJA, "manganinja.com", 10) {
	override val datePattern: String = "dd/MM/yyyy"
}
