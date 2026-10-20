package org.inkstack.parsers.site.madara.de

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken("Site unreachable (connection error)")
@MangaSourceParser("MANGALESEN", "MangaLesen", "de")
internal class MangaLesen(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.MANGALESEN, "mangalesen.net")
