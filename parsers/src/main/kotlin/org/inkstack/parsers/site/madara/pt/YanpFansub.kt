package org.inkstack.parsers.site.madara.pt

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("YANPFANSUB", "YanpFansub", "pt")
internal class YanpFansub(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.YANPFANSUB, "trisalyanp.com") {
	override val datePattern = "d 'de' MMMM 'de' yyyy"
}
