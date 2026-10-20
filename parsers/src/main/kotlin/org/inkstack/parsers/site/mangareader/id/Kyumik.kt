package org.inkstack.parsers.site.mangareader.id

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@Broken("Site unreachable (DNS)")
@MangaSourceParser("KYUMIK", "Kyumik", "id", ContentType.HENTAI)
internal class Kyumik(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.KYUMIK, "kyumik.com", pageSize = 20, searchPageSize = 10)