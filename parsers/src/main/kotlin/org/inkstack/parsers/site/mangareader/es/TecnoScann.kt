package org.inkstack.parsers.site.mangareader.es

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaListFilterCapabilities
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@Broken("Site unreachable (timeout)")
@MangaSourceParser("TECNOSCANN", "TecnoScann", "es")
internal class TecnoScann(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.TECNOSCANN, "tecnoscann.com", 20, 10) {
	override val filterCapabilities: MangaListFilterCapabilities
		get() = super.filterCapabilities.copy(
			isTagsExclusionSupported = false,
		)
}
