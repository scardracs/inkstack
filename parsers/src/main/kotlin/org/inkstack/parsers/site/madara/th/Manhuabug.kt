package org.inkstack.parsers.site.madara.th

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser
import java.util.*

@MangaSourceParser("MANHUABUG", "ManhuaBug", "th")
internal class Manhuabug(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.MANHUABUG, "www.manhuabug.com", 10) {
	override val datePattern: String = "d MMMM yyyy"
	override val sourceLocale: Locale = Locale.ENGLISH
	override val selectPage = "img"
}
