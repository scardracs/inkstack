package org.inkstack.parsers.site.mangareader.id

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser
import java.util.*

@Broken("Site unreachable (connection refused)")
@MangaSourceParser("MANGAYARO", "MangaYaro", "id")
internal class Mangayaro(context: MangaLoaderContext) :
	MangaReaderParser(
		context,
		MangaParserSource.MANGAYARO,
		"mangayaro.id",
		pageSize = 20,
		searchPageSize = 20,
	) {
	override val sourceLocale: Locale = Locale.ENGLISH
}
