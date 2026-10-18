package org.inkstack.parsers.site.zeistmanga.ar

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.model.MangaState
import org.inkstack.parsers.model.MangaTag
import org.inkstack.parsers.site.zeistmanga.ZeistMangaParser
import org.inkstack.parsers.util.*
import java.util.*

@MangaSourceParser("MANGAHUB_LINK", "MangaHub.link", "ar", ContentType.HENTAI)
internal class MangaHub(context: MangaLoaderContext) :
	ZeistMangaParser(context, MangaParserSource.MANGAHUB_LINK, "www.mangahub.link") {

	override val sateOngoing: String = "مستمر"
	override val sateFinished: String = "مكتمل"

	override suspend fun getFilterOptions() = super.getFilterOptions().copy(
		availableStates = EnumSet.of(MangaState.ONGOING, MangaState.FINISHED),
	)

	override suspend fun fetchAvailableTags(): Set<MangaTag> {
		val doc = webClient.httpGet("https://$domain").parseHtml()
		return doc.requireElementById("Genre").select("div.items-center").mapToSet {
			MangaTag(
				key = it.selectFirstOrThrow("input").attr("value"),
				title = it.selectFirstOrThrow("label").text().substringBefore(')').toTitleCase(sourceLocale),
				source = source,
			)
		}
	}
}
