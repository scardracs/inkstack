package org.inkstack.parsers.site.madara.tr

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

//This source requires an account.
@MangaSourceParser("OPIATOON", "OpiaToon", "tr")
internal class OpiaToon(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.OPIATOON, "opiatoon.art", 20) {
	override val datePattern = "d MMMM"
}
