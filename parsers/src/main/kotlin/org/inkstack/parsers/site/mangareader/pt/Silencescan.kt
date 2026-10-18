package org.inkstack.parsers.site.mangareader.pt

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@MangaSourceParser("SILENCESCAN", "SilenceScan", "pt", ContentType.HENTAI)
internal class Silencescan(context: MangaLoaderContext) :
	MangaReaderParser(
		context,
		MangaParserSource.SILENCESCAN,
		"silencescan.com.br",
		pageSize = 35,
		searchPageSize = 35,
	) {
	override val datePattern = "MMM d, yyyy"
}
