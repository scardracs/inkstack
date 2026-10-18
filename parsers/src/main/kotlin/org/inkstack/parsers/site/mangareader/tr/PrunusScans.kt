package org.inkstack.parsers.site.mangareader.tr

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser
import org.inkstack.parsers.Broken

@Broken
@MangaSourceParser("PRUNUSSCANS", "PrunusScans", "tr")
internal class PrunusScans(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.PRUNUSSCANS, "prunusscans.com", pageSize = 20, searchPageSize = 10)
