package org.inkstack.parsers.site.mangareader.tr

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaListFilterCapabilities
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser
import java.util.*

@MangaSourceParser("ADUMANGA", "AduManga", "tr")
internal class AduManga(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.ADUMANGA, "adumanga.com", pageSize = 20, searchPageSize = 10) {

	override val sourceLocale: Locale = Locale.ENGLISH

	override val filterCapabilities: MangaListFilterCapabilities
		get() = super.filterCapabilities.copy(
			isTagsExclusionSupported = false,
		)
}
