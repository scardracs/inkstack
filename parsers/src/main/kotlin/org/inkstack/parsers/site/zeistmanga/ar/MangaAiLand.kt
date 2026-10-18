package org.inkstack.parsers.site.zeistmanga.ar

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.model.MangaTag
import org.inkstack.parsers.site.zeistmanga.ZeistMangaParser
import org.inkstack.parsers.util.mapToSet
import org.inkstack.parsers.util.parseHtml
import org.inkstack.parsers.util.requireElementById

@MangaSourceParser("MANGAAILAND", "MangaAiLand", "ar")
internal class MangaAiLand(context: MangaLoaderContext) :
	ZeistMangaParser(context, MangaParserSource.MANGAAILAND, "manga-ai-land.blogspot.com") {
	override val sateOngoing: String = "مستمر"
	override val sateFinished: String = "مكتملة"
	override val sateAbandoned: String = "متوقفة"

	override suspend fun fetchAvailableTags(): Set<MangaTag> {
		val doc = webClient.httpGet("https://$domain").parseHtml()
		return doc.requireElementById("LinkList1").select("ul li a").mapToSet {
			MangaTag(
				key = it.attr("href").substringBefore("?").substringAfterLast('/'),
				title = it.text(),
				source = source,
			)
		}
	}
}
