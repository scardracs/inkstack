package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken("Site unreachable (DNS)")
@MangaSourceParser("MANGA18XYZ", "Manga18.xyz", "en", ContentType.HENTAI)
internal class Manga18Xyz(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.MANGA18XYZ, "manga18.xyz", 36)
