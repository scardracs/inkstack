package org.inkstack.parsers.site.mangareader.tr

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaListFilterCapabilities
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@MangaSourceParser("NIRVANAMANGA", "NirvanaManga", "tr")
internal class NirvanaManga(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.NIRVANAMANGA, "nirvanamanga.com", pageSize = 20, searchPageSize = 10) {
	override val filterCapabilities: MangaListFilterCapabilities
		get() = super.filterCapabilities.copy(
			isTagsExclusionSupported = false,
		)
}
