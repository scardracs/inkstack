package org.inkstack.parsers.site.madara.es

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser
import java.util.*

@MangaSourceParser("RICHTOSCAN", "RichtoScan", "es")
internal class RichtoScan(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.RICHTOSCAN, "r1.richtoon.top") {
	override val tagPrefix = "manga-generos/"
	override val sourceLocale: Locale = Locale.ENGLISH
}
