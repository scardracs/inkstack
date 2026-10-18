package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("WEBTOONXYZ", "Webtoon.xyz", "en", ContentType.HENTAI)
internal class WebtoonXyz(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.WEBTOONXYZ, "www.webtoon.xyz", 20) {
	override val tagPrefix = "webtoon-genre/"
	override val listUrl = "read/"
	override val datePattern = "d MMM yyyy"
}
