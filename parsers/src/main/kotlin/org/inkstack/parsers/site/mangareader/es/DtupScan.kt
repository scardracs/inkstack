package org.inkstack.parsers.site.mangareader.es

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@MangaSourceParser("DTUPSCAN", "DtupScan", "es")
internal class DtupScan(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.DTUPSCAN, "dtupscan.com", pageSize = 20, searchPageSize = 10)
