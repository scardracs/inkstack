package org.inkstack.parsers.site.mangareader.id

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@Broken
@MangaSourceParser("KOMIKGO", "KomikGo", "id", ContentType.HENTAI)
internal class KomikGo(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.KOMIKGO, "komikgo.xyz", pageSize = 20, searchPageSize = 10)
