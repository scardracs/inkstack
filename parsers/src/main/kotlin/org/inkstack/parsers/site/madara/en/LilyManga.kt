package org.inkstack.parsers.site.madara.en

import org.jsoup.nodes.Document
import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.*
import org.inkstack.parsers.site.madara.MadaraParser
import org.inkstack.parsers.util.*

@MangaSourceParser("LILYMANGA", "LilyManga", "en", ContentType.HENTAI)
internal class LilyManga(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.LILYMANGA, "lilymanga.net") {
	override val tagPrefix = "gl-genre/"
	override val listUrl = "ys/"
	override val datePattern = "dd.MM.yyyy"
	override val withoutAjax = true
	override val authorSearchSupported = true

	override fun parseMangaList(doc: Document): List<Manga> {
		val elements = doc.select("div.row.c-tabs-item__content").ifEmpty {
			doc.select("div.page-item-detail")
		}

		if (elements.isEmpty()) {
			return emptyList()
		}

		return elements.map { div ->
			val link = div.selectFirst(".post-title a") ?: div.selectFirstOrThrow("a")
			val href = link.attrAsRelativeUrl("href")
			val summary = div.selectFirst(".tab-summary") ?: div.selectFirst(".item-summary")
			val author = div.selectFirst(".author.meta a, .mg_author a, .mg_artists a")?.ownText()
			val badgeText = div.selectFirst(".manga-title-badges .text")?.text()?.lowercase().orEmpty()
			val statusText = summary?.selectFirst(".mg_status")?.selectFirst(".summary-content")?.ownText()?.lowercase().orEmpty()
			val state = when (badgeText.ifEmpty { statusText }) {
				in ongoing -> MangaState.ONGOING
				in finished, "ended" -> MangaState.FINISHED
				in abandoned -> MangaState.ABANDONED
				in paused -> MangaState.PAUSED
				in upcoming -> MangaState.UPCOMING
				else -> null
			}
			Manga(
				id = generateUid(href),
				url = href,
				publicUrl = href.toAbsoluteUrl(div.host ?: domain),
				coverUrl = div.selectFirst("img")?.src(),
				title = link.text().ifEmpty {
					(summary?.selectFirst("h3, h4") ?: div.selectFirst(".manga-name, .post-title"))?.text().orEmpty()
				},
				altTitles = emptySet(),
				rating = div.selectFirst("span.total_votes")?.ownText()?.toFloatOrNull()?.div(5f) ?: RATING_UNKNOWN,
				tags = summary?.selectFirst(".mg_genres")?.select("a")?.mapNotNullToSet { a ->
					MangaTag(
						key = a.attr("href").removeSuffix('/').substringAfterLast('/'),
						title = a.text().ifEmpty { return@mapNotNullToSet null }.toTitleCase(),
						source = source,
					)
				}.orEmpty(),
				authors = setOfNotNull(author),
				state = state,
				source = source,
				contentRating = if (isNsfwSource) ContentRating.ADULT else null,
			)
		}
	}
}
