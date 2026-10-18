package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("READER_EVILFLOWERS", "EvilFlowers", "en")
internal class ReaderEvilflowers(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.READER_EVILFLOWERS, "evilflowers.com", pageSize = 10) {
	override val listUrl = "project/"
}
