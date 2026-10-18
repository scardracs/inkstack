package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken
@MangaSourceParser("LIKEMANGAIN", "LikeManga.in", "en")
internal class LikeMangaIn(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.LIKEMANGAIN, "likemanga.in", 36) {
	override val datePattern = "d MMMM, yyyy"
}
