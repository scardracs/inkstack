package org.inkstack.parsers.site.mangareader.id

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@MangaSourceParser("MANHWAKU", "Manhwaku", "id")
internal class Manhwaku(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.MANHWAKU, "manhwaku.id", pageSize = 20, searchPageSize = 10)
