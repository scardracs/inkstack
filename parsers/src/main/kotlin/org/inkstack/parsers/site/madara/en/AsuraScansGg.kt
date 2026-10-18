package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("ASURASCANSGG", "AsuraScansGg", "en")
internal class AsuraScansGg(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.ASURASCANSGG, "asurascans.us")
