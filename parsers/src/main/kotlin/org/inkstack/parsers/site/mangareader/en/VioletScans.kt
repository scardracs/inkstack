package org.inkstack.parsers.site.mangareader.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@MangaSourceParser("VIOLETSCANS", "VioletScans", "en")
internal class VioletScans(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.VIOLETSCANS, "violetmanga.com", pageSize = 20, searchPageSize = 10) {

	override val listUrl = "/comics"
}
