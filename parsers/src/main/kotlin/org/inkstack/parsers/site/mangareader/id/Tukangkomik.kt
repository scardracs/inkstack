package org.inkstack.parsers.site.mangareader.id

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaListFilterCapabilities
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser
import java.util.*

@Broken("Selectors outdated (empty list)")
@MangaSourceParser("TUKANGKOMIK", "Tukang Komik", "id")
internal class Tukangkomik(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.TUKANGKOMIK, "tukangkomik.co", pageSize = 20, searchPageSize = 20) {
	override val datePattern = "MMM d, yyyy"
	override val sourceLocale: Locale = Locale.ENGLISH
	override val filterCapabilities: MangaListFilterCapabilities
		get() = super.filterCapabilities.copy(
			isTagsExclusionSupported = false,
		)
}
