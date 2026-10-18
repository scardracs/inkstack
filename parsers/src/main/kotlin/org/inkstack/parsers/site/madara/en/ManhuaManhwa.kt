package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("MANHUAMANHWA", "ManhuaManhwa", "en")
internal class ManhuaManhwa(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.MANHUAMANHWA, "manhuamanhwa.com") {
	override val datePattern = "MM/dd/yyyy"
}
