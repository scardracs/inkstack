package org.inkstack.parsers.site.mangareader.ar

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@MangaSourceParser("HIJALACOM", "Hijalacom", "ar")
internal class Hijalacom(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.HIJALACOM, "hijala.com", pageSize = 30, searchPageSize = 10)
