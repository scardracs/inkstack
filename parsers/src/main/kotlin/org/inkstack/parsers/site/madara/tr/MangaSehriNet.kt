package org.inkstack.parsers.site.madara.tr

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("MANGASEHRINET", "MangaSehri.net", "tr")
internal class MangaSehriNet(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.MANGASEHRINET, "manga-sehri.net", 20) {
	override val datePattern = "dd MMMM yyyy"
}
