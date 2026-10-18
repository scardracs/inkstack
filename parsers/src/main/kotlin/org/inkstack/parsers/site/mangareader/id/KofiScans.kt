package org.inkstack.parsers.site.mangareader.id

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser
import java.util.*

@MangaSourceParser("KOFISCANS", "KofiScans", "id")
internal class KofiScans(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.KOFISCANS, "isekaikomik.com", pageSize = 20, searchPageSize = 10) {
	override val sourceLocale: Locale = Locale.ENGLISH
}
