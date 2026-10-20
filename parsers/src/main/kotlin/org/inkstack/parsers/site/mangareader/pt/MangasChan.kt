package org.inkstack.parsers.site.mangareader.pt

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@Broken("Site unreachable (DNS)")
@MangaSourceParser("MANGASCHAN", "MangasChan", "pt")
internal class MangasChan(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.MANGASCHAN, "mangaschan.net", pageSize = 20, searchPageSize = 20) {
	override val datePattern = "MMMM d, yyyy"
}
