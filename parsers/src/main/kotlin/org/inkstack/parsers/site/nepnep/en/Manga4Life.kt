package org.inkstack.parsers.site.nepnep.en

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.nepnep.NepnepParser

// site closed in favour of weeb central
@Broken("Site unreachable (DNS)")
@MangaSourceParser("MANGA4LIFE", "Manga4Life", "en")
internal class Manga4Life(context: MangaLoaderContext) :
	NepnepParser(context, MangaParserSource.MANGA4LIFE, "manga4life.com")
