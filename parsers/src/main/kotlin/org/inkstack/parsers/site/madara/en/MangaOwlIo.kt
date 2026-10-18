package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("MANGAOWL_IO", "MangaOwl.io", "en")
internal class MangaOwlIo(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.MANGAOWL_IO, "mangaowl.io") {
	override val listUrl = "mangaowl-all/"
}
