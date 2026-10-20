package org.inkstack.parsers.site.zeistmanga.es

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.zeistmanga.ZeistMangaParser

@Broken("Site unreachable (DNS)")
@MangaSourceParser("AIYUMANGASCANLATION", "AiyuManhua", "es")
internal class AiyuMangaScanlation(context: MangaLoaderContext) :
	ZeistMangaParser(context, MangaParserSource.AIYUMANGASCANLATION, "www.aiyumanhua.com") {
	override val selectPage = "article.chapter img"
}
