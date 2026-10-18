package org.inkstack.parsers.site.zeistmanga.pt

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.zeistmanga.ZeistMangaParser

@MangaSourceParser("YAOIFANCLUB", "YaoiFanClub", "pt")
internal class YaoiFanClub(context: MangaLoaderContext) :
	ZeistMangaParser(context, MangaParserSource.YAOIFANCLUB, "www.yaoifanclub.com") {
	override val sateOngoing: String = "Ativo"
	override val sateFinished: String = "Completo"
	override val sateAbandoned: String = "Dropado"
}
