package org.inkstack.parsers.site.mangareader.ja

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaListFilterCapabilities
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser
import java.util.*

@MangaSourceParser("MANGAMATE", "MangaMate", "ja")
internal class MangaMate(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.MANGAMATE, "manga-mate.org", pageSize = 10, searchPageSize = 10) {
	override val datePattern = "M月 d, yyyy"
	override val sourceLocale: Locale = Locale.ENGLISH
	override val filterCapabilities: MangaListFilterCapabilities
		get() = super.filterCapabilities.copy(
			isTagsExclusionSupported = false,
		)
}
