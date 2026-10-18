package org.inkstack.parsers.site.madara.tr

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken
@MangaSourceParser("YETISKINRUYAMANGA", "Yetişkin Rüya Manga", "tr")
internal class YetiskinRuyaManga(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.YETISKINRUYAMANGA, "www.yetiskinruyamanga.com", 16) {
	override val datePattern = "dD/MM/yyyy"
}
