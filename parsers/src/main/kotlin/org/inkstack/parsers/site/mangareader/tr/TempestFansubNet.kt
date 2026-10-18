package org.inkstack.parsers.site.mangareader.tr

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@MangaSourceParser("TEMPESTFANSUBNET", "tempestmangas.com", "tr")
internal class TempestFansubNet(context: MangaLoaderContext) :
	MangaReaderParser(
		context,
		MangaParserSource.TEMPESTFANSUBNET,
		"tempestfansub.net",
		pageSize = 30,
		searchPageSize = 10,
	)
