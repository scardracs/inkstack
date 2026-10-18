package org.inkstack.parsers.site.madara.vi

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaChapter
import org.inkstack.parsers.model.MangaPage
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser
import org.inkstack.parsers.util.*

@MangaSourceParser("QUAANHDAOCUTEO", "Quả Anh Đào Cuteo", "vi", ContentType.HENTAI)
internal class Quaanhdaocuteo(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.QUAANHDAOCUTEO, "qadcuteo.cc") {
	override val datePattern = "dd/MM/yyyy"
	override val selectPage = "p img"

	override suspend fun getPages(chapter: MangaChapter): List<MangaPage> {
		val fullUrl = chapter.url.toAbsoluteUrl(domain)
		val doc = webClient.httpGet(fullUrl).parseHtml()
		val root = doc.body().selectFirstOrThrow(selectBodyPage)
		return root.select(selectPage).map { img ->
			val url = img.requireSrc().toRelativeUrl(domain)
			MangaPage(
				id = generateUid(url),
				url = url,
				preview = null,
				source = source,
			)
		}
	}
}
