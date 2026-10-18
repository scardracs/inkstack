package org.inkstack.parsers.site.zeistmanga.pt

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.zeistmanga.ZeistMangaParser

@Broken
@MangaSourceParser("ZSCANLATION", "ZScanlation", "pt", ContentType.HENTAI)
internal class ZScanlation(context: MangaLoaderContext) :
	ZeistMangaParser(context, MangaParserSource.ZSCANLATION, "www.zscanlation.com") {
	override val sateOngoing: String = "Em Lançamento"
	override val sateFinished: String = "Completo"
	override val sateAbandoned: String = "Dropado"
}
