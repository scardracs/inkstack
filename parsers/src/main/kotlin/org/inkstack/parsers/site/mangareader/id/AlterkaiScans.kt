package org.inkstack.parsers.site.mangareader.id

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser
import java.util.*

@MangaSourceParser("ALTERKAISCANS", "AlterkaiScans", "id")
internal class AlterkaiScans(context: MangaLoaderContext) :
	MangaReaderParser(
		context,
		MangaParserSource.ALTERKAISCANS,
		"alterkaiscans.my.id",
		pageSize = 20,
		searchPageSize = 10,
	) {
	override val sourceLocale: Locale = Locale.ENGLISH
}
