package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken("Site unreachable (connection error)")
@MangaSourceParser("MANGAACTION", "MangaAction", "en")
internal class MangaAction(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.MANGAACTION, "mangaaction.com")
