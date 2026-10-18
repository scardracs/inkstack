package org.inkstack.parsers.site.mangareader.en

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@Broken("Redirect to HiveComic")
@MangaSourceParser("VOIDSCANS", "HiveToon", "en")
internal class VoidScans(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.VOIDSCANS, "hivetoon.com", pageSize = 15, searchPageSize = 10)
