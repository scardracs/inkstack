package org.inkstack.parsers.site.madara.tr

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken("Rate limited (HTTP 429)")
@MangaSourceParser("DOMALFANSB", "DomalFansub", "tr")
internal class DomalFansb(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.DOMALFANSB, "domalfansb.com.tr") {
	override val datePattern = "d MMMM yyyy"
	override val tagPrefix = "manga-turleri/"
}
