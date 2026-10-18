package org.inkstack.parsers.site.madara.tr

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser

@MangaSourceParser("WEBTOONTR", "WebtoonTr", "tr")
internal class Webtoontr(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.WEBTOONTR, "webtoontr.net", 16) {
	override val tagPrefix = "webtoon-kategori/"
	override val listUrl = "webtoon/"
	override val datePattern = "dd/MM/yyyy"
}
