package org.inkstack.parsers.site.mangareader.tr

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaListFilterCapabilities
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@MangaSourceParser("AYATOON", "AyaToon", "tr")
internal class Ayatoon(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.AYATOON, "ayatoon.com", pageSize = 20, searchPageSize = 20) {

	override val filterCapabilities: MangaListFilterCapabilities
		get() = super.filterCapabilities.copy(
			isTagsExclusionSupported = false,
		)
}
