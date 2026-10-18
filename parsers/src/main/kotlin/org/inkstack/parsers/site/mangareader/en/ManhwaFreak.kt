package org.inkstack.parsers.site.mangareader.en

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@Broken("Not dead, changed template")
@MangaSourceParser("MANHWA_FREAK", "ManhwaFreak", "en")
internal class ManhwaFreak(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.MANHWA_FREAK, "manhwafreak.xyz", pageSize = 30, searchPageSize = 42)
