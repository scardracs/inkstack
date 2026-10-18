package org.inkstack.parsers.site.madara.ar

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser
import org.inkstack.parsers.model.ContentType

@MangaSourceParser("WEBTOONEMPIRE", "WebtoonEmpire", "ar", type = ContentType.HENTAI)
internal class WebtoonEmpire(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.WEBTOONEMPIRE, "webtoonempire-bl.com", pageSize = 10) {
	override val listUrl = "webtoon/"
	override val datePattern = "d MMMM، yyyy"
	override val withoutAjax = true
}
