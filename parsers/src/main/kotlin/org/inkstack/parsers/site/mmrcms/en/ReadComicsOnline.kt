package org.inkstack.parsers.site.mmrcms.en

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okhttp3.Interceptor
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentRating
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.Manga
import org.inkstack.parsers.model.MangaChapter
import org.inkstack.parsers.model.MangaListFilter
import org.inkstack.parsers.model.MangaListFilterCapabilities
import org.inkstack.parsers.model.MangaListFilterOptions
import org.inkstack.parsers.model.MangaPage
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.model.MangaState
import org.inkstack.parsers.model.MangaTag
import org.inkstack.parsers.model.RATING_UNKNOWN
import org.inkstack.parsers.model.SortOrder
import org.inkstack.parsers.site.mmrcms.MmrcmsParser
import org.inkstack.parsers.util.attrAsRelativeUrl
import org.inkstack.parsers.util.generateUid
import org.inkstack.parsers.util.json.getStringOrNull
import org.inkstack.parsers.util.json.mapJSONNotNull
import org.inkstack.parsers.util.parseJson
import org.inkstack.parsers.util.urlEncoded
import org.inkstack.parsers.util.mapChapters
import org.inkstack.parsers.util.nullIfEmpty
import org.inkstack.parsers.util.parseHtml
import org.inkstack.parsers.util.parseSafe
import org.inkstack.parsers.util.requireSrc
import org.inkstack.parsers.util.src
import org.inkstack.parsers.util.textOrNull
import org.inkstack.parsers.util.toAbsoluteUrl
import org.inkstack.parsers.util.toRelativeUrl
import java.text.SimpleDateFormat
import java.util.EnumSet
import java.util.Locale

@MangaSourceParser("READCOMICSONLINE", "ReadComicsOnline.ru", "en", ContentType.COMICS)
internal class ReadComicsOnline(context: MangaLoaderContext) :
	MmrcmsParser(context, MangaParserSource.READCOMICSONLINE, "readcomicsonline.ru") {

	private val chapterDateFormat = SimpleDateFormat("d MMM yyyy", Locale.US)

	override val availableSortOrders: Set<SortOrder> = EnumSet.of(
		SortOrder.POPULARITY,
		SortOrder.UPDATED,
	)

	override val filterCapabilities: MangaListFilterCapabilities
		get() = MangaListFilterCapabilities(
			isSearchSupported = true,
		)

	override suspend fun getFilterOptions(): MangaListFilterOptions = MangaListFilterOptions()

	override fun intercept(chain: Interceptor.Chain): Response {
		val request = chain.request()
		val newRequest = request.newBuilder()
			.header("Referer", "https://$domain/")
			.build()
		return chain.proceed(newRequest)
	}

	override suspend fun getListPage(page: Int, order: SortOrder, filter: MangaListFilter): List<Manga> {
		if (!filter.query.isNullOrEmpty()) {
			return search(page, filter.query)
		}
		if (filter.tags.isNotEmpty() || filter.states.isNotEmpty()) {
			return emptyList()
		}
		val sort = when (order) {
			SortOrder.UPDATED -> "latest"
			else -> "views"
		}
		val doc = webClient.httpGet("https://$domain/comic-list?sort=$sort&page=$page").parseHtml()
		return doc.select("div.comic-list-layout .grid > .group").mapNotNull(::parseMangaListItem)
	}

	// The search endpoint is an autocomplete API returning every match at once, without pagination
	private suspend fun search(page: Int, query: String): List<Manga> {
		if (page > 1) {
			return emptyList()
		}
		val url = "https://$domain/search?query=${query.trim().urlEncoded()}"
		val suggestions = webClient.httpGet(url).parseJson().optJSONArray("suggestions") ?: return emptyList()
		return suggestions.mapJSONNotNull { jo ->
			val slug = jo.getStringOrNull("data") ?: return@mapJSONNotNull null
			val href = "/comic/$slug"
			Manga(
				id = generateUid(href),
				title = jo.getStringOrNull("value") ?: slug,
				altTitles = emptySet(),
				url = href,
				publicUrl = jo.getStringOrNull("url") ?: href.toAbsoluteUrl(domain),
				rating = RATING_UNKNOWN,
				contentRating = if (isNsfwSource) ContentRating.ADULT else null,
				coverUrl = guessCover(href, jo.getStringOrNull("cover")),
				tags = emptySet(),
				state = null,
				authors = emptySet(),
				source = source,
			)
		}
	}

	private fun parseMangaListItem(element: Element): Manga? {
		val anchor = element.selectFirst("a.block.text-sm.font-semibold") ?: return null
		val href = anchor.attrAsRelativeUrl("href")
		return Manga(
			id = generateUid(href),
			title = anchor.text(),
			altTitles = emptySet(),
			url = href,
			publicUrl = href.toAbsoluteUrl(element.baseUriHost()),
			rating = RATING_UNKNOWN,
			contentRating = if (isNsfwSource) ContentRating.ADULT else null,
			coverUrl = guessCover(href, element.selectFirst("img")?.src()),
			tags = emptySet(),
			state = null,
			authors = emptySet(),
			source = source,
		)
	}

	override suspend fun getDetails(manga: Manga): Manga = coroutineScope {
		val fullUrl = manga.url.toAbsoluteUrl(domain)
		val doc = webClient.httpGet(fullUrl).parseHtml()
		val chaptersDeferred = async { getChapters(doc, manga.title) }
		manga.copy(
			title = doc.selectFirst("h1.text-2xl")?.textOrNull() ?: manga.title,
			coverUrl = guessCover(manga.url, doc.selectFirst("img.w-full.rounded-xl")?.src()) ?: manga.coverUrl,
			description = doc.selectFirst("p.mt-5.text-sm")?.textOrNull(),
			state = parseState(doc.selectFirst("div.flex.flex-wrap.gap-2 span.rounded-full")?.text()),
			tags = doc.select("dl div:contains(Genres:) a").mapTo(LinkedHashSet()) {
				MangaTag(
					key = it.attr("href").removeSuffix("/").substringAfterLast('/'),
					title = it.text(),
					source = source,
				)
			},
			authors = doc.select("div:has(span:contains(Author:)) > a").mapTo(LinkedHashSet()) { it.text() },
			chapters = chaptersDeferred.await(),
		)
	}

	private fun parseState(value: String?): MangaState? {
		return when (value?.lowercase(Locale.US)) {
			"complete", "completed" -> MangaState.FINISHED
			"ongoing", "on going" -> MangaState.ONGOING
			"dropped", "cancelled", "canceled" -> MangaState.ABANDONED
			else -> null
		}
	}

	private fun getChapters(doc: Document, mangaTitle: String): List<MangaChapter> {
		return doc.select(".overflow-hidden.border-ink-600 > a").mapChapters(reversed = true) { i, element ->
			val href = element.attrAsRelativeUrl("href")
			val chapterName = element.selectFirst(".text-brand-400")?.textOrNull() ?: element.text()
			MangaChapter(
				id = generateUid(href),
				title = cleanChapterName(mangaTitle, chapterName),
				number = i + 1f,
				volume = 0,
				url = href,
				uploadDate = chapterDateFormat.parseSafe(element.selectFirst(".text-slate-500")?.text()),
				source = source,
				scanlator = null,
				branch = null,
			)
		}
	}

	override suspend fun getPages(chapter: MangaChapter): List<MangaPage> {
		val doc = webClient.httpGet(chapter.url.toAbsoluteUrl(domain)).parseHtml()
		return doc.select("#reader-all img").mapIndexed { i, img ->
			val url = img.requireSrc().toRelativeUrl(domain)
			MangaPage(
				id = generateUid(url),
				url = url,
				preview = null,
				source = source,
			)
		}
	}

	private fun cleanChapterName(mangaTitle: String, chapterName: String): String {
		return chapterName
			.removePrefix(mangaTitle)
			.trimStart(' ', '-', ':')
			.nullIfEmpty()
			?: chapterName
	}

	private fun guessCover(mangaUrl: String, imageUrl: String?): String? {
		imageUrl?.takeUnless { it.contains("/cover/cover_missing.", ignoreCase = true) }?.let {
			return it
		}
		val slug = mangaUrl.removeSuffix("/").substringAfterLast('/').nullIfEmpty() ?: return null
		return "https://$domain/uploads/manga/$slug/cover/cover_250x350.jpg"
	}

	private fun Element.baseUriHost(): String {
		return baseUri().substringAfter("://", domain).substringBefore('/').ifEmpty { domain }
	}
}
