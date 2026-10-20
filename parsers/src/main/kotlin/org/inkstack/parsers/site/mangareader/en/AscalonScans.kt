package org.inkstack.parsers.site.mangareader.en

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaListFilterCapabilities
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@Broken("Site unreachable (connection error)")
@MangaSourceParser("ASCALONSCANS", "AscalonScans", "en")
internal class AscalonScans(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.ASCALONSCANS, "ascalonscans.com", pageSize = 20, searchPageSize = 10) {

	override val filterCapabilities: MangaListFilterCapabilities
		get() = super.filterCapabilities.copy(
			isTagsExclusionSupported = false,
		)
}
