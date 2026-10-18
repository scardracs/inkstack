package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("MANGAFOXFULL", "MangaFoxFull", "en")
internal class MangaFoxFull(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.MANGAFOXFULL, "mangafoxfull.com") {
	override val postReq = true
}
