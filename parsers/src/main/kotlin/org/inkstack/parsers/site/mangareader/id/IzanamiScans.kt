package org.inkstack.parsers.site.mangareader.id

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@MangaSourceParser("IZANAMISCANS", "IzanamiScans", "id")
internal class IzanamiScans(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.IZANAMISCANS, "izanamiscans.my.id", pageSize = 20, searchPageSize = 10)
