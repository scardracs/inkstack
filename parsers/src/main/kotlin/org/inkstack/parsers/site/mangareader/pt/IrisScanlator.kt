package org.inkstack.parsers.site.mangareader.pt

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@MangaSourceParser("IRISSCANLATOR", "IrisScanlator", "pt")
internal class IrisScanlator(context: MangaLoaderContext) :
	MangaReaderParser(
		context,
		MangaParserSource.IRISSCANLATOR,
		"irisscanlator.com.br",
		pageSize = 20,
		searchPageSize = 10,
	)
