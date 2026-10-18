package org.inkstack.parsers.site.mangareader.ar

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@MangaSourceParser("LAGOONSCANS", "Lagoon Scans", "en")
internal class LagoonScans(context: MangaLoaderContext) :
    MangaReaderParser(context, MangaParserSource.LAGOONSCANS, "lagoonscans.com", pageSize = 20, searchPageSize = 10)
