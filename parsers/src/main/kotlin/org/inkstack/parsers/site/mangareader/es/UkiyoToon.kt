package org.inkstack.parsers.site.mangareader.es

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaListFilterCapabilities
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser
import org.inkstack.parsers.Broken

@Broken("Selectors outdated (empty list)")
@MangaSourceParser("UKIYOTOON", "UkiyoToon", "es")
internal class UkiyoToon(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.UKIYOTOON, "nakamatoon.com", 30, 10) {
	override val filterCapabilities: MangaListFilterCapabilities
		get() = super.filterCapabilities.copy(
			isTagsExclusionSupported = false,
		)
}
