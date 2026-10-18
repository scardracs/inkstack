package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.model.MangaTag
import org.inkstack.parsers.site.madara.MadaraParser
import org.inkstack.parsers.util.*

@MangaSourceParser("HENTAIXCOMIC", "Hentai x Comic", "en", ContentType.HENTAI)
internal class HentaixComic(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.HENTAIXCOMIC, "hentaixcomic.com", 16) {

	override suspend fun fetchAvailableTags(): Set<MangaTag> {
		val doc = webClient.httpGet("https://$domain/?s=&post_type=wp-manga").parseHtml()
		val set = mutableSetOf<MangaTag>()
		val titles = mutableSetOf<String>()
		doc.select("div.checkbox-group input[type=checkbox]").forEach { input ->
			val key = input.attr("value")
			val title = input.nextElementSibling()?.text()?.toTitleCase()
			if (key.isNotEmpty() && !title.isNullOrEmpty() && titles.add(title.lowercase())) {
				set.add(MangaTag(key = key, title = title, source = source))
			}
		}
		return set
	}
}
