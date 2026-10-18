package org.inkstack.parsers.site.foolslide.en

import kotlinx.coroutines.coroutineScope
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.Manga
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.foolslide.FoolSlideParser
import org.inkstack.parsers.util.*

@MangaSourceParser("SEINAGI", "Seinagi", "en")
internal class Seinagi(context: MangaLoaderContext) :
	FoolSlideParser(context, MangaParserSource.SEINAGI, "reader.seinagi.org.es") {

	override val pagination = false

	override suspend fun getDetails(manga: Manga): Manga = coroutineScope {
		val fullUrl = manga.url.toAbsoluteUrl(domain)
		val testAdultPage = webClient.httpGet(fullUrl).parseHtml()
		val doc = if (testAdultPage.selectFirst("div.info form") != null) {
			webClient.httpPost(fullUrl, "adult=true").parseHtml()
		} else {
			testAdultPage
		}
		val chapters = getChapters(doc)
		val desc = if (doc.selectFirstOrThrow(selectInfo).html().contains("Description")) {
			doc.selectFirstOrThrow(selectInfo).text().substringAfter("Description: ").substringBefore("Readings")
		} else {
			doc.selectFirstOrThrow(selectInfo).text()
		}
		val author = if (doc.selectFirstOrThrow(selectInfo).html().contains("Author")) {
			doc.selectFirstOrThrow(selectInfo).text().substringAfter("Author: ").substringBefore("Art")
		} else {
			null
		}
		manga.copy(
			coverUrl = doc.selectFirst(".thumbnail img")?.src() ?: manga.coverUrl,
			description = desc.nullIfEmpty(),
			authors = author?.nullIfEmpty()?.let { setOf(it) } ?: emptySet(),
			chapters = chapters,
		)
	}
}
