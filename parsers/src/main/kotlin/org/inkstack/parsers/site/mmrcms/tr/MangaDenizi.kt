package org.inkstack.parsers.site.mmrcms.tr

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mmrcms.MmrcmsParser
import java.util.*

@MangaSourceParser("MANGA_DENIZI", "MangaDenizi", "tr")
internal class MangaDenizi(context: MangaLoaderContext) :
	MmrcmsParser(context, MangaParserSource.MANGA_DENIZI, "www.mangadenizi.net") {
	override val selectState = "dt:contains(Durum)"
	override val selectAlt = "dt:contains(Diğer Adları)"
	override val selectAut = "dt:contains(Yazar & Çizer)"
	override val selectTag = "dt:contains(Kategoriler)"
	override val sourceLocale: Locale = Locale.ENGLISH
	override val datePattern = "dd.MM.yyyy"
}
