package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken
@MangaSourceParser("DUCKMANGA", "DuckManga", "en", ContentType.HENTAI)
internal class DuckManga(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.DUCKMANGA, "duckmanga.com", 20)
