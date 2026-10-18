package org.inkstack.parsers.site.mangareader.pt

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@Broken
@MangaSourceParser("FRANXXMANGAS", "FranxxMangas", "pt")
internal class FranxxMangas(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.FRANXXMANGAS, "franxxmangas.net", pageSize = 10, searchPageSize = 10)
