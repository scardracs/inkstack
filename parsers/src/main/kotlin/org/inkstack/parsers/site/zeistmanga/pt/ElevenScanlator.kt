package org.inkstack.parsers.site.zeistmanga.pt

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.zeistmanga.ZeistMangaParser

@MangaSourceParser("ELEVENSCANLATOR", "ElevenScanlator", "pt")
internal class ElevenScanlator(context: MangaLoaderContext) :
	ZeistMangaParser(context, MangaParserSource.ELEVENSCANLATOR, "elevenscanlator.blogspot.com") {
	override val sateOngoing: String = "Lançando"
	override val sateFinished: String = "Completo"
	override val sateAbandoned: String = "Dropado"
}
