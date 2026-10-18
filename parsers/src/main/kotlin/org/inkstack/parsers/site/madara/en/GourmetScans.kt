package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.Manga
import org.inkstack.parsers.model.MangaListFilter
import org.inkstack.parsers.model.MangaListFilterCapabilities
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.model.MangaTag
import org.inkstack.parsers.model.SortOrder
import org.inkstack.parsers.site.madara.MadaraParser
import org.inkstack.parsers.util.mapNotNullToSet
import org.inkstack.parsers.util.nullIfEmpty
import org.inkstack.parsers.util.oneOrThrowIfMany
import org.inkstack.parsers.util.parseHtml
import org.inkstack.parsers.util.toTitleCase
import org.inkstack.parsers.util.urlEncoded
import java.util.EnumSet

@MangaSourceParser("GOURMETSCANS", "GourmetScans", "en")
internal class GourmetScans(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.GOURMETSCANS, "gourmetsupremacy.com") {
	override val listUrl = "project/"
	override val tagPrefix = "genre/"
	override val stylePage = ""

	override val availableSortOrders: Set<SortOrder> = EnumSet.of(
		SortOrder.RELEVANCE,
		SortOrder.UPDATED,
		SortOrder.POPULARITY,
		SortOrder.NEWEST,
		SortOrder.ALPHABETICAL,
		SortOrder.RATING,
	)

	override val filterCapabilities: MangaListFilterCapabilities
		get() = MangaListFilterCapabilities(
			isSearchSupported = true,
			isYearSupported = true,
		)

	override suspend fun getListPage(page: Int, order: SortOrder, filter: MangaListFilter): List<Manga> {
		val pages = page + 1
		val query = filter.query?.trim().orEmpty()
		val orderBy = order.toOrderBy()
		val url = buildString {
			append("https://")
			append(domain)
			append("/")

			if (query.isNotBlank()) {
				if (pages > 1) {
					append("page/")
					append(pages)
					append("/")
				}
				append("?s=")
				append(query.urlEncoded())
				append("&post_type=wp-manga")
				if (orderBy.isNotEmpty()) {
					append("&m_orderby=")
					append(orderBy)
				}
				return@buildString
			}

			append(
				when {
					filter.year != 0 -> "release-year/${filter.year}/"
					filter.tags.isNotEmpty() -> "$tagPrefix${filter.tags.oneOrThrowIfMany()?.key}/"
					else -> listUrl
				},
			)
			if (pages > 1) {
				append("page/")
				append(pages)
				append("/")
			}
			if (orderBy.isNotEmpty()) {
				append("?m_orderby=")
				append(orderBy)
			}
		}

		return parseMangaList(webClient.httpGet(url).parseHtml())
	}

	override suspend fun getDetails(manga: Manga): Manga {
		val details = super.getDetails(manga)
		return details.copy(
			chapters = details.chapters?.map { chapter ->
				chapter.copy(url = chapter.url.substringBefore("?style=list"))
			},
		)
	}

	override suspend fun fetchAvailableTags(): Set<MangaTag> {
		val doc = webClient.httpGet("https://$domain/$listUrl").parseHtml()
		val keySet = HashSet<String>()
		return doc.select("div.row.genres ul li a").mapNotNullToSet { a ->
			val key = a.attr("href").removeSuffix("/").substringAfterLast('/').nullIfEmpty()
			if (key == null || !keySet.add(key)) {
				return@mapNotNullToSet null
			}
			MangaTag(
				key = key,
				title = a.ownText().trim().toTitleCase(sourceLocale),
				source = source,
			)
		}
	}

	private fun SortOrder.toOrderBy() = when (this) {
		SortOrder.UPDATED -> "modified"
		SortOrder.POPULARITY -> "views"
		SortOrder.NEWEST -> "new-manga"
		SortOrder.ALPHABETICAL -> "alphabet"
		SortOrder.RATING -> "rating"
		else -> ""
	}
}
