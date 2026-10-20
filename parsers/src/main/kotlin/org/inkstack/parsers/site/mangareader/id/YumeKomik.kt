package org.inkstack.parsers.site.mangareader.id

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser

@Broken("Domain no longer a manga site (corporate landing)")
@MangaSourceParser("YUMEKOMIK", "YumeKomik", "id")
internal class YumeKomik(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.YUMEKOMIK, "yumekomik.com", pageSize = 20, searchPageSize = 10)
