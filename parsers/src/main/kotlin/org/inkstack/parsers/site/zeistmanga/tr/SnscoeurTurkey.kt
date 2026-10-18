package org.inkstack.parsers.site.zeistmanga.tr

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.model.MangaTag
import org.inkstack.parsers.site.zeistmanga.ZeistMangaParser
import org.inkstack.parsers.util.mapToSet
import org.inkstack.parsers.util.parseHtml
import org.inkstack.parsers.util.selectFirstOrThrow

@MangaSourceParser("SNSCOEURTURKEY", "SnscoeurTurkey", "tr", ContentType.HENTAI)
internal class SnscoeurTurkey(context: MangaLoaderContext) :
	ZeistMangaParser(context, MangaParserSource.SNSCOEURTURKEY, "snscoeurturkey.blogspot.com") {
	override val sateOngoing: String = "Güncel"
	override val sateFinished: String = "Final"
	override val sateAbandoned: String = "Düzenleniyor"

	override val mangaCategory = "Seriler"

	override suspend fun fetchAvailableTags(): Set<MangaTag> {
		val doc = webClient.httpGet("https://$domain/p/gelismis-arama.html").parseHtml()
		return doc.selectFirstOrThrow("div.filter").select("ul li").mapToSet {
			MangaTag(
				key = it.selectFirstOrThrow("input").attr("value"),
				title = it.selectFirstOrThrow("label").text(),
				source = source,
			)
		}
	}
}
