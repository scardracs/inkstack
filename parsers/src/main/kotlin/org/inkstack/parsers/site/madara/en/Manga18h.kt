package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken("Redirect to @hentai20")
@MangaSourceParser("MANGA18H", "Manga18h", "en", ContentType.HENTAI)
internal class Manga18h(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.MANGA18H, "manga18h.xyz", 20)
