package org.inkstack.parsers.site.mangareader.id

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser
import java.util.Locale

@MangaSourceParser("APKOMIK", "Apkomik", "id")
internal class Apkomik(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.APKOMIK, "01.apkomik.com", pageSize = 20, searchPageSize = 10) {
	override val sourceLocale: Locale = Locale.ENGLISH
}
