package org.inkstack.parsers.site.mangareader.it

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaListFilterCapabilities
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@Broken("Site unreachable (DNS)")
@MangaSourceParser("WITCOMICS", "WitComics", "it")
internal class WitComics(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.WITCOMICS, "www.witcomics.net", pageSize = 5, searchPageSize = 10) {
	override val filterCapabilities: MangaListFilterCapabilities
		get() = super.filterCapabilities.copy(
			isTagsExclusionSupported = false,
		)
}
