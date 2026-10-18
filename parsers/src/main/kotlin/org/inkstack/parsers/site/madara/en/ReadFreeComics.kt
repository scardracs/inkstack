package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser
import org.inkstack.parsers.Broken

@Broken
@MangaSourceParser("READFREECOMICS", "ReadFreeComics", "en")
internal class ReadFreeComics(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.READFREECOMICS, "readfreecomics.com") {
	override val tagPrefix = "webtoon-comic-genre/"
	override val listUrl = "webtoon-comic/"
}
