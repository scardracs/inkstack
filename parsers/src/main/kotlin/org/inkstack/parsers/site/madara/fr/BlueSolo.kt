package org.inkstack.parsers.site.madara.fr

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken ( "Need refactor")
@MangaSourceParser("BLUESOLO", "BlueSolo", "fr")
internal class BlueSolo(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.BLUESOLO, "www1.bluesolo.org", 10) {
	override val datePattern = "d MMMM yyyy"
}
