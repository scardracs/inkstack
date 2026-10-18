package org.inkstack.parsers.site.madtheme.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.MangaChapter
import org.inkstack.parsers.model.MangaPage
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madtheme.MadthemeParser
import org.inkstack.parsers.util.toAbsoluteUrl
import org.inkstack.parsers.util.generateUid
import org.inkstack.parsers.util.parseHtml

@MangaSourceParser("MANGAFOREST", "MangaForest", "en")
internal class MangaForest(context: MangaLoaderContext) :
	MadthemeParser(context, MangaParserSource.MANGAFOREST, "mangaforest.me") {
	
	private val subDomain = "sb.mbcdn.xyz"

	override suspend fun getPages(chapter: MangaChapter): List<MangaPage> {
		val fullUrl = chapter.url.toAbsoluteUrl(domain)
		val doc = webClient.httpGet(fullUrl).parseHtml()
		val regexPages = Regex("chapImages\\s*=\\s*['\"](.*?)['\"]")
		val pages = doc.select("script").firstNotNullOfOrNull { script ->
			regexPages.find(script.html())?.groupValues?.getOrNull(1)
		}?.split(',')

		return pages?.map { url ->
			val cleanUrl = url.substringAfter("/manga")
			MangaPage(
				id = generateUid(url),
				url = "https://$subDomain/manga$cleanUrl",
				preview = null,
				source = source,
			)
		} ?: emptyList()
	}
}
