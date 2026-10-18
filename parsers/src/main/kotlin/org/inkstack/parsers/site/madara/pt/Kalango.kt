package org.inkstack.parsers.site.madara.pt

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser
import java.util.*

@MangaSourceParser("KALANGO", "Kalango", "pt")
internal class Kalango(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.KALANGO, "kalango.org") {
	override val datePattern: String = "dd 'de' MMMM 'de' yyyy"
	override val sourceLocale: Locale = Locale.ENGLISH
}
