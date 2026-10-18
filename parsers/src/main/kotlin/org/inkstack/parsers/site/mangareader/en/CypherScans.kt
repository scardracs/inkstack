package org.inkstack.parsers.site.mangareader.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@MangaSourceParser("CYPHERSCANS", "CypherScans", "en")
internal class CypherScans(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.CYPHERSCANS, "cypheroscans.xyz", pageSize = 20, searchPageSize = 10)
