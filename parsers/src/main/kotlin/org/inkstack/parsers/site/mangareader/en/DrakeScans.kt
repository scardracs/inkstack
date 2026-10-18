package org.inkstack.parsers.site.mangareader.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.mangareader.MangaReaderParser
import org.inkstack.parsers.Broken

@Broken
@MangaSourceParser("DRAKESCANS", "DrakeComic", "en")
internal class DrakeScans(context: MangaLoaderContext) :
	MangaReaderParser(context, MangaParserSource.DRAKESCANS, "drakecomic.org", 20, 10)
