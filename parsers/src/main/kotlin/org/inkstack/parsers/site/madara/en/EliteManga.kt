package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.Broken
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@Broken
@MangaSourceParser("ELITEMANGA", "EliteManga", "en")
internal class EliteManga(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.ELITEMANGA, "www.beinmanga.com")
