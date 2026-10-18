package org.inkstack.parsers.site.mangareader.tr

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@MangaSourceParser("SEREINSCAN", "SereinScan", "tr")
internal class SereinScan(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.SEREINSCAN, "sereinscan.com", pageSize = 20, searchPageSize = 10)
