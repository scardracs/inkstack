package org.inkstack.parsers.site.mangareader.id

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaListFilterCapabilities
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser
import org.inkstack.parsers.Broken

@Broken // The website is either closed or constantly blocked
@MangaSourceParser("SIIKOMIK", "SiiKomik", "id")
internal class SiiKomik(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.SIIKOMIK, "siikomik.fun", pageSize = 20, searchPageSize = 10) {
	override val filterCapabilities: MangaListFilterCapabilities
		get() = super.filterCapabilities.copy(
			isTagsExclusionSupported = false,
			isSearchSupported = false,
		)
}

