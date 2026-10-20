package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser
import org.inkstack.parsers.Broken

@Broken("Site unreachable (HTTP_522)")
@MangaSourceParser("ARCANESCANS", "ArcaneScans", "en")
internal class ArcaneScans(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.ARCANESCANS, "arcanescans.com", 10)
