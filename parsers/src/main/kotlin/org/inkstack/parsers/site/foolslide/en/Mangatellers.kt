package org.inkstack.parsers.site.foolslide.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.foolslide.FoolSlideParser

@MangaSourceParser("MANGATELLERS", "Mangatellers", "en")
internal class Mangatellers(context: MangaLoaderContext) :
	FoolSlideParser(context, MangaParserSource.MANGATELLERS, "reader.mangatellers.gr") {
	override val pagination = false
}
