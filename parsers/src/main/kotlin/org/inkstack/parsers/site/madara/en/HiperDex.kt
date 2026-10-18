package org.inkstack.parsers.site.madara.en

import org.inkstack.parsers.MangaLoaderContext
import org.inkstack.parsers.MangaSourceParser
import org.inkstack.parsers.model.ContentType
import org.inkstack.parsers.model.MangaChapter
import org.inkstack.parsers.model.MangaPage
import org.inkstack.parsers.model.MangaParserSource
import org.inkstack.parsers.site.madara.MadaraParser
import org.inkstack.parsers.util.toAbsoluteUrl
import org.inkstack.parsers.util.toRelativeUrl

@MangaSourceParser("HIPERDEX", "HiperToon", "en", ContentType.HENTAI)
internal class HiperDex(context: MangaLoaderContext) :
	MadaraParser(context, MangaParserSource.HIPERDEX, "hiperdex.com", 36) {

	override val listUrl = ""

	override suspend fun getPages(chapter: MangaChapter): List<MangaPage> {
		// Remove ?style=list parameter from chapter URLs
		val absoluteUrl = chapter.url.toAbsoluteUrl(domain)
		val cleanUrl = if (absoluteUrl.contains("?style=list")) {
			absoluteUrl.replace("?style=list", "").replace("&style=list", "")
		} else {
			absoluteUrl
		}
		val relativeCleanUrl = cleanUrl.toRelativeUrl(domain)
		val modifiedChapter = chapter.copy(url = relativeCleanUrl)
		return super.getPages(modifiedChapter)
	}
}
