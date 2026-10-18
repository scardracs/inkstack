package org.inkstack.parsers.site.zeistmanga.es

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.zeistmanga.ZeistMangaParser

@MangaSourceParser("GISTAMISHOUSEFANSUB", "GistamisHouseFansub", "es")
internal class GistamisHouseFansub(context: MangaLoaderContext) :
	ZeistMangaParser(context, MangaParserSource.GISTAMISHOUSEFANSUB, "gistamishousefansub.blogspot.com") {
	override val sateOngoing: String = "Activo"
	override val sateFinished: String = "Completo"
	override val sateAbandoned: String = "Cancelado"
	override val selectPage = ".post img"
}
