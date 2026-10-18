package org.inkstack.parsers.site.zeistmanga.id

import org.jsoup.nodes.Document
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaChapter
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.model.MangaTag
import org.inkstack.parsers.site.zeistmanga.ZeistMangaParser
import org.inkstack.parsers.util.*

@MangaSourceParser("TOONCUBUS", "ToonCubus", "id", ContentType.HENTAI)
internal class ToonCubus(context: MangaLoaderContext) :
	ZeistMangaParser(context, MangaParserSource.TOONCUBUS, "www.tooncubus.top") {

	override suspend fun fetchAvailableTags(): Set<MangaTag> {
		val doc = webClient.httpGet("https://$domain/p/genre-list.html").parseHtml()
		return doc.select(".dzdes-genre ul li a").mapToSet {
			MangaTag(
				key = it.attr("href").removeSuffix("/").substringAfterLast("/"),
				title = it.selectFirstOrThrow("span").text(),
				source = source,
			)
		}
	}

	override suspend fun loadChapters(mangaUrl: String, doc: Document): List<MangaChapter> {
		return doc.selectFirstOrThrow("ul.series-chapterlist").select("div.flexch-infoz")
			.mapChapters(reversed = true) { i, div ->
				val url = div.selectFirstOrThrow("a").attr("href")
				val name = div.selectFirstOrThrow("a span").text()
				MangaChapter(
					id = generateUid(url),
					url = url,
					title = name,
					number = i + 1f,
					volume = 0,
					branch = null,
					uploadDate = 0,
					scanlator = null,
					source = source,
				)
			}
	}
}
