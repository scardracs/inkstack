package org.inkstack.parsers.site.mangareader.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaListFilterCapabilities
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@MangaSourceParser("RAVENSCANS", "RavenScans", "en")
internal class RavenScans(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.RAVENSCANS, "ravenscans.org", pageSize = 95, searchPageSize = 28) {
	override val listUrl = "/manga"
	override val datePattern = "MMM d, yyyy"
	override val filterCapabilities: MangaListFilterCapabilities
		get() = super.filterCapabilities.copy(
			isTagsExclusionSupported = false,
		)
}
